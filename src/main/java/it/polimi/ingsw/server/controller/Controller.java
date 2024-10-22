package it.polimi.ingsw.server.controller;

import it.polimi.ingsw.server.bootstrap.events.ViewReconnected;
import it.polimi.ingsw.server.bootstrap.events.listeners.ViewReconnectedListener;
import it.polimi.ingsw.server.controller.events.MatchEnded;
import it.polimi.ingsw.server.controller.events.listeners.ControllerListener;
import it.polimi.ingsw.server.controller.powerup.Powerup;
import it.polimi.ingsw.server.controller.powerup.PowerupFactory;
import it.polimi.ingsw.server.controller.weapons.Weapon;
import it.polimi.ingsw.server.controller.weapons.WeaponFactory;
import it.polimi.ingsw.server.model.battlefield.SpawnpointBlock;
import it.polimi.ingsw.server.model.battlefield.TurretBlock;
import it.polimi.ingsw.server.model.collections.Deck;
import it.polimi.ingsw.server.model.currency.AmmoCube;
import it.polimi.ingsw.server.model.currency.BonusTile;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.events.*;
import it.polimi.ingsw.server.model.events.listeners.PlayerListener;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.ActionTile;
import it.polimi.ingsw.server.model.player.BasicAction;
import it.polimi.ingsw.server.model.player.CompoundAction;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.weapons.WeaponTile;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.server.view.View;
import it.polimi.ingsw.server.view.events.ViewEvent;
import it.polimi.ingsw.server.view.events.listeners.ViewListener;
import it.polimi.ingsw.shared.messages.ClientApi;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class has the purpose of managing the game flow
 */
public class Controller implements Runnable, PlayerListener, ViewReconnectedListener, ViewListener, AutoCloseable {
    /**
     * Logging utility
     */
    protected final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * The active match
     */
    private Match match;
    /**
     * The list of views
     */
    private List<View> views;

    /**
     * The players of this match
     */
    private List<Player> players;
    /**
     * The weapon map that associates each weapon name with the object
     */
    private final Map<String, Weapon> weaponMap;
    /**
     * The powerup deck
     */
    private Deck<PowerupTile> powerupTileDeck;
    /**
     * Associates each player to its view
     */
    private Map<Player, View> playerViews = new HashMap<>();
    /**
     * The minimum amount of clients
     */
    private int minClients;
    /**
     * A set of listeners for controller events
     */
    private Set<ControllerListener> listeners = new HashSet<>();
    /**
     * Whether or not the match should be closed
     */
    private boolean closed = false;

    /**
     * Constructs a new controller
     *
     * @param match the match to manage
     * @param views the players' views
     * @param minClients the minimum amount of clients for the match
     */
    public Controller(Match match, List<View> views, int minClients) {

        if (views.size() != match.getPlayers().size()) {
            throw new IllegalArgumentException("View number does not match player number");
        }

        this.minClients = minClients;
        this.match = match;
        this.views = views;
        this.players = match.getPlayers();
        views.forEach(view -> this.playerViews.put(view.getPlayer(), view));
        views.forEach(view -> view.addViewListener(this));
        this.players.forEach(player -> player.addPlayerListener(this));
        this.weaponMap = WeaponFactory.createWeaponDictionary(match.getBoard());
        powerupTileDeck = match.getPowerupDeck();
        match.start();
    }

    /**
     * Runs a match, handling all the operations
     */
    @Override
    public void run() {
        for (Player player : players) {
            //Picking up two powerups to choose the spawnpoint
            List<PowerupTile> powerups = Arrays.asList(
                    powerupTileDeck
                            .pick()
                            .orElseThrow(() -> new IllegalStateException("Empty deck")),
                    powerupTileDeck
                            .pick()
                            .orElseThrow(() -> new IllegalStateException("Empty deck"))
            );
            //Discarding the selected powerup
            PowerupTile spawnpoint = selectSpawnpointFromPowerup(powerups, player, playerViews.get(player));
            //Grabbing the other powerup
            player.grabPowerup(powerups.indexOf(spawnpoint) == 0 ? powerups.get(1) : powerups.get(0));
            manageActivePlayerTurn(player, playerViews.get(player));
        }

        Player activePlayer;

        int connectedViews = views.stream().mapToInt(view -> view.isConnected() ? 1 : 0).reduce(0, Integer::sum);

        while (!match.isEnded() && connectedViews >= minClients && !closed) {
            activePlayer = match.getActivePlayer();
            manageActivePlayerTurn(activePlayer, playerViews.get(activePlayer));

            connectedViews = views.stream().mapToInt(view -> view.isConnected() ? 1 : 0).reduce(0, Integer::sum);
        }
        if (!match.isEnded()) {
            match.close();
        }
        logger.info("The match is over");

        for (View view: views) {
            if (view.isConnected()) {
                try {
                    view.close();
                } catch (Exception e) {
                    logger.warning("Couldn't close the view " + e);
                }
            }
        }

        notifyMatchEnded();
    }

    /**
     * Notifies that the match is ended
     */
    private void notifyMatchEnded() {
        MatchEnded e = new MatchEnded(this);
        listeners.forEach(l -> l.onMatchEnd(e));
    }

    /**
     * This functions manages how to manage the player's turn
     * @param activePlayer is the player who has to execute his turn
     * @param view is the interface that manages the turn
     */
    private void manageActivePlayerTurn(Player activePlayer, View view) {
        if (view.isConnected()) {
            logger.info("Managing actions...");
            manageActions(activePlayer, view);
            logger.info("No more actions to be managed");
        } else {
            logger.info("View is not connected, skipping player turn...");
        }
        logger.info("Ending turn. Checking for died players...");
        for (Player player : match.endTurn()) {
            //Here we iterate on the dead players returned by match.endTurn()
            logger.info("Player " + player.getColor() + " died.");
            PowerupTile deckPowerup = powerupTileDeck.pick().orElseThrow(() -> new IllegalStateException("Run out of powerups!"));
            List<PowerupTile> playerPowerups = new LinkedList<>(player.getPowerups());
            playerPowerups.add(deckPowerup);
            PowerupTile playerPowerup = selectSpawnpointFromPowerup(playerPowerups, player, playerViews.get(player));
            if (playerPowerup != deckPowerup) {
                player.discardPowerup(playerPowerup);
                player.grabPowerup(deckPowerup);
            }
            player.bringBackToLife();
        }
        match.changeTurn();
    }

    /**
     * This method lets the player choose the spawnpoint at the beginning of the match or when he die
     * @param powerups list of powerups to discard to choose spawnpoint
     * @param player active player
     * @param view the view to be asked
     * @return the selected powerup to be discarded
     */
    private PowerupTile selectSpawnpointFromPowerup(List<PowerupTile> powerups, Player player, Interviewer view){
        //Asking what powerup to discard
        it.polimi.ingsw.shared.datatransferobjects.Powerup chosenPowerup = mandatorySpawnpointSelection(powerups, view, "Select your Spawnpoint");
        PowerupTile discardedPowerup = powerups
                .stream()
                .filter(tile -> tile.getName().equals(chosenPowerup.getName()) && tile.getColor().equals(chosenPowerup.getColor()))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Selected powerup is not available"));
        player.selectSpawnpoint(discardedPowerup);
        logger.info("Selected Spawnpoint " + discardedPowerup.getColor());
        return discardedPowerup;
    }

    /**
     * This function manages the action chosen by the player
     * @param basicActionChosen is the action chosen
     * @param activePlayer is the player who is acting the move
     * @param view is the interface that manages the chosen action
     */
    private void manageChosenAction(BasicAction basicActionChosen, Player activePlayer, Interviewer view){
        switch (basicActionChosen){
            case GRAB:
                logger.info("Managing GRAB move...");
                if (activePlayer.isOnASpawnpoint()){
                    if (!grabOnASpawnpoint(activePlayer, view)){
                        throw new IllegalArgumentException("Grab on a spawnpoint not executed");
                    }
                } else {
                    grabNotOnASpawnpoint(activePlayer);
                }
                break;
            case MOVE:
                logger.info("Managing MOVE move...");
                activePlayer.move(view.select("Which direction do you want to move?", activePlayer.getAvailableDirections(), ClientApi.DIRECTION_QUESTION));
                break;
            case SHOOT:
                logger.info("Managing SHOOT move...");
                executeShoot(activePlayer, view);
                break;
            case RELOAD:
                logger.info("Managing RELOAD move...");
                executeReload(activePlayer, view);
                break;
            default:
                throw new IllegalStateException("No valid action selected!");
        }
    }

    /**
     * This functions manage the action of grabbing if the player is on a spawnpoint
     * @param activePlayer the player who is grabbing
     * @param view the interface that manage the action of grabbing
     * @return false if something goes wrong
     */
    private boolean grabOnASpawnpoint(Player activePlayer, Interviewer view){
        //choose weapon to be picked up
        List<WeaponTile> affordableWeapons = ((SpawnpointBlock) activePlayer.getBlock())
                .getWeapons()
                .stream()
                .filter(weapon -> PaymentHandler.canAfford(weapon.getAcquisitionCost(), activePlayer))
                .collect(Collectors.toList());
        if (!affordableWeapons.isEmpty()){
            WeaponTile weapon = selectWeaponTile("Which weapon would you like to grab?", affordableWeapons, view);
            //pick up
            logger.info("picking up weapon " + weapon.getName() + "...");
            pickUpWeapon(weapon, activePlayer, view);
        } else return false;
        return true;
    }

    /**
     * This function manages the grabbing action if the player is not on a spawnpoint
     * @param activePlayer the player who is grabbing
     */
    private void grabNotOnASpawnpoint(Player activePlayer){
        logger.info("Grabbing some ammo...");
        BonusTile card = ((TurretBlock)activePlayer.getBlock()).grab().orElseThrow(() -> new IllegalStateException("Grabbing from an empty turret"));
        activePlayer.grabAmmoCubes(card.getRewards());
        match.getBonusDeck().discard(card);
        //Maybe here we should tell the player he got some ammos
        if (card.canPickPowerup() &&
                activePlayer.getPowerups().size() < activePlayer.getConstraints().getMaxPowerupsForPlayer()){
            logger.info("Grabbing power-up too...");
            powerupTileDeck.pick().ifPresent(activePlayer::grabPowerup);
        }
    }

    /**
     * This functions manages the action of SHOOT
     * @param activePlayer the player who chose the action
     * @param view the interface that manages the shooting action
     */
    private void executeShoot(Player activePlayer, Interviewer view){
        List<WeaponTile> playerWeapons = activePlayer.getWeapons();
        List<WeaponTile> loadedPlayerWeapons = playerWeapons
                .stream()
                .filter(tile -> tile.isLoaded() && weaponMap.get(tile.getName()).hasAvailableAttacks(activePlayer))
                .collect(Collectors.toList());
        if (!loadedPlayerWeapons.isEmpty()){
            WeaponTile selectedWeapon = selectWeaponTile("Which weapon do you want to use for shooting?", loadedPlayerWeapons, view);
            activePlayer.setActiveWeapon(selectedWeapon);
            Weapon weapon = weaponMap.get(selectedWeapon.getName());
            weapon.shoot(view, activePlayer);
            activePlayer.unloadActiveWeapon();
            activePlayer.putAwayActiveWeapon();
        } else throw new IllegalStateException("Shoot executed while no weapon is loaded");
    }

    /**
     * This function manages the action of RELOAD one of the weapon of the player
     * @param activePlayer the player who chose the action
     * @param view the interface that choose which weapon to reload and how to pay the reload-cost
     */
    private void executeReload(Player activePlayer, Interviewer view){
        List<WeaponTile> playerWeapons = activePlayer.getWeapons();
        List<WeaponTile> weaponsToReload = new ArrayList<>(playerWeapons);
        List<WeaponTile> weaponsReloadable = weaponsToReload
                .stream()
                .filter(weaponOfPlayer -> PaymentHandler.canAfford(weaponOfPlayer.getReloadCost(), activePlayer) && !weaponOfPlayer.isLoaded())
                .collect(Collectors.toList());
        if (weaponsReloadable.isEmpty()){
            throw new IllegalStateException("Player cannot afford to reload any weapon or all weapons are loaded. The reload shouldn't be selectable");
        } else {
            WeaponTile weaponToReload;
            List<String> weaponsReloadableForView = weaponsReloadable
                    .stream()
                    .map(WeaponTile::getName)
                    .collect(Collectors.toList());
            do {
                Optional<String> selected = view.selectOptional("Which weapon would you like to reload?", weaponsReloadableForView, ClientApi.RELOAD_QUESTION);
                if (selected.isPresent()) {

                    weaponToReload = weaponsReloadable
                            .stream()
                            .filter(w -> w.getName().equals(selected.get()))
                            .findAny()
                            .orElseThrow(() -> new IllegalStateException("Weapon to reload does not exist"));

                    reloadWeapon(weaponToReload, activePlayer, view);

                    weaponsReloadable = weaponsToReload
                            .stream()
                            .filter(weaponOfPlayer -> PaymentHandler.canAfford(weaponOfPlayer.getReloadCost(), activePlayer) && !weaponOfPlayer.isLoaded())
                            .collect(Collectors.toList());
                    weaponsReloadableForView = weaponsReloadable
                            .stream()
                            .map(WeaponTile::getName)
                            .collect(Collectors.toList());
                } else {
                    weaponToReload = null;
                }
            } while (weaponToReload != null && !weaponsReloadableForView.isEmpty());
        }
    }

    /**
     * This function picks up the weapon for the given player, managing the pickup-cost
     * @param weapon weapon to be picked up
     * @param activePlayer player who wants to pick up the weapon
     * @param view the interface that manage how to pay the pick-up cost
     */
    private void pickUpWeapon(WeaponTile weapon, Player activePlayer, Interviewer view){
        List<AmmoCube> acquisitionCost = weapon.getAcquisitionCost();
        List<Coin> paymentMethod = PaymentHandler.collectCoins(acquisitionCost, activePlayer, view);
        if (activePlayer.getWeapons().size() == activePlayer.getConstraints().getMaxWeaponsForPlayer()) {
            WeaponTile weaponToDiscard = selectWeaponTile("Which weapon do you want to discard?", activePlayer.getWeapons(), view);
            activePlayer.grabWeapon(weapon, paymentMethod, weaponToDiscard);
        } else {
            activePlayer.grabWeapon(weapon, paymentMethod);
            Optional<WeaponTile> newWeapon = match.getWeaponDeck().pick();
            newWeapon.ifPresent(weaponTile -> {
                activePlayer.getBlock().drop(weaponTile);
            });
        }
    }

    /**
     * This method manages the weaponTile selection for a WEAPON_CHOICE_QUESTION
     * @param question string to be sent as question to the client
     * @param weaponTiles selectable weapon tiles
     * @param view view that will manage the selection
     * @return the selected weapon tile
     */
    private WeaponTile selectWeaponTile(String question, List<WeaponTile> weaponTiles, Interviewer view){
        List<String> weaponNames = weaponTiles
                .stream()
                .map(WeaponTile::getName)
                .collect(Collectors.toList());
        return weaponTiles.get(weaponNames.indexOf(view.select(question, weaponNames, ClientApi.WEAPON_CHOICE_QUESTION)));
    }

    /**
     * This function reloads the given weapon, managing the reload cost
     * @param weapon weapon that needs to be reloaded
     * @param activePlayer is the player who has to reload a weapon
     * @param view is the interface that decides what coin to use to reload the weapon
     */
    private void reloadWeapon(WeaponTile weapon, Player activePlayer, Interviewer view){
        List<AmmoCube> reloadCost = weapon.getReloadCost();
        if (PaymentHandler.canAfford(reloadCost, activePlayer)){
            List<Coin> paymentMethod = PaymentHandler.collectCoins(reloadCost, activePlayer, view);
            activePlayer.reload(weapon, paymentMethod);
        }
    }

    /**
     * This function manage the player's actions during player's turn
     * @param player is the player who is currently player
     * @param view is the interface who decides how to manage the player
     */
    private void manageActions(Player player, View view){
        ActionTile tile = player.getAvailableMacroActions();
        for (List<CompoundAction> compoundActions : tile.getCompoundActions()) {
            managePowerups(player, null, Powerup.Trigger.IN_BETWEEN_ACTIONS, "Do you want to use a powerup?");
            //per every macro action
            List<BasicAction> playedActions = new LinkedList<>();
            Optional<BasicAction> move;
            do {
                //Here we select the possible basic actions the player can do
                Set<BasicAction> availableActions = candidateBasicActions(playedActions, compoundActions)
                        .stream()
                        .filter(basicAction -> canPerformAction(player, basicAction))
                        .collect(Collectors.toSet());
                //Here we ask the view to choose a basic action
                move = availableActions.isEmpty() ?
                        Optional.empty() :
                        view.selectOptional("Which move would you like to execute?", availableActions, ClientApi.BASIC_ACTION_QUESTION);
                //Here we execute that basic action
                move.ifPresent(basicAction -> {
                    manageChosenAction(basicAction, player, view);
                    playedActions.add(basicAction);
                });
            } while (move.isPresent() && view.isConnected());
        }
        managePowerups(player, null, Powerup.Trigger.IN_BETWEEN_ACTIONS, "Do you want to use a powerup?");
    }
    /**
     * This private method calculate the possible BasicActions the player can do
     * @param playedActions is the list of actions done in this turn by the players
     * @param compoundActions is the list of possible compound actions the player can do in that particular macro action.
     *                        They can variate from gaming mode to gaming mode or by the state of the player
     * @return a Set of possible basic actions the player can execute
     */
    private Set<BasicAction> candidateBasicActions(List<BasicAction> playedActions, List<CompoundAction> compoundActions){

        Set<BasicAction> basicActions = new HashSet<>();

        compoundActions
                .forEach(compoundAction -> {
                    //Here we take the basic actions from the compound action
                    List<BasicAction> actions = compoundAction.getActions();

                    List<BasicAction> filledPlayedActions = new LinkedList<>(playedActions);

                    boolean compatible = false;

                    while (filledPlayedActions.size() < actions.size()) {
                        if (actions.subList(0, filledPlayedActions.size()).equals(filledPlayedActions)) {
                            compatible = true;
                            break;
                        } else {
                            filledPlayedActions.add(0, BasicAction.MOVE);
                        }
                    }

                    if (compatible) {
                        basicActions.add(actions.get(filledPlayedActions.size()));

                        if (actions.get(filledPlayedActions.size()) == BasicAction.MOVE) {
                            for (int i = filledPlayedActions.size() + 1; i < actions.size(); i++) {
                                if (actions.get(i) != BasicAction.MOVE) {
                                    basicActions.add(actions.get(i));
                                    break;
                                }
                            }
                        }
                    }
                });

        return basicActions;
    }


    /**
     * Returns true if the player can do the given action
     * @param player the player who wants to execute the action
     * @param action the action that needs to be executed
     * @return true if the player can execute the action
     */
    private boolean canPerformAction(Player player, BasicAction action){
        switch (action) {
            case MOVE:
                //A player can always perform a move
                return true;
            case SHOOT:
                //A player can perform a shoot if he has some weapons reloaded and the reloaded weapons have some available attacks
                return player.getWeapons()
                        .stream()
                        .filter(WeaponTile::isLoaded)
                        .map(weaponTile -> weaponMap.get(weaponTile.getName()))
                        .anyMatch(weapon -> weapon.hasAvailableAttacks(player));
            case GRAB:
                //A player can perform a grab if: ...
                if (player.isOnASpawnpoint()){
                    //if he is on a spawnpoint, there should be some affordable weapons
                    SpawnpointBlock playerBlock = (SpawnpointBlock) player.getBlock();
                    return playerBlock.getWeapons()
                            .stream()
                            .anyMatch(weapon -> PaymentHandler.canAfford(weapon.getAcquisitionCost(), player));
                } else {
                    //if he is on a turret block he shouldn't have just picked up from that block in the present turn and
                    //he should not be full of ammo and powerups
                    return ((TurretBlock)player.getBlock()).getBonusTile().isPresent() && !player.isFullOfAmmoAndPowerups();
                }
            case RELOAD:
                //A player can reload if he has some weapons to reload and he can afford the reload cost
                return player.getWeapons()
                        .stream()
                        .anyMatch(weaponTile -> !weaponTile.isLoaded() &&
                                PaymentHandler.canAfford(weaponTile.getReloadCost(), player));
        }
        throw new IllegalArgumentException("Action cannot be checked if performable");
    }

    /**
     * The controller does not react to this event
     *
     * @param e the event corresponding to the player's death
     */
    @Override
    public void onPlayerDied(PlayerDied e) {
        // Nothing to do here
    }

    /**
     * Damages can cause player reactions, this listener manage some powerup triggers
     *
     * @param e this parameter contains info about the attacker and the damaged player
     */
    @Override
    public void onPlayerDamaged(PlayerDamaged e) {
        managePowerups(e.getAttacker(), e.getVictim(), Powerup.Trigger.ON_DAMAGE_GIVEN, "Do you want to use a powerup against " + e.getVictim().getPlayerInfo().getNickname() + "?");
        managePowerups(e.getVictim(), e.getAttacker(), Powerup.Trigger.ON_DAMAGE_RECEIVED, "Do you want to use a powerup against " + e.getAttacker().getPlayerInfo().getNickname() + "?");
    }

    /**
     * The controller does not react to this event
     *
     * @param e the event corresponding to the player's death
     */
    @Override
    public void onPlayerOverkilled(PlayerOverkilled e) {
        // Nothing to do here
    }

    /**
     * The controller does not react to this event
     *
     * @param e the event corresponding to the player's rebirth
     */
    @Override
    public void onPlayerReborn(PlayerEvent e) {
        // Nothing to do here
    }

    /**
     * The controller does not react to this event
     *
     * @param e the event corresponding to the player's board flipping
     */
    @Override
    public void onPlayerBoardFlipped(PlayerEvent e) {
        // Nothing to do here

    }

    /**
     * The controller does not react to this event
     *
     * @param e the event corresponding to the player's tile flipping
     */
    @Override
    public void onPlayerTileFlipped(PlayerEvent e) {
        // Nothing to do here

    }

    /**
     * The controller does not react to this event
     *
     * @param e the event corresponding to the player reloading a weapon
     */
    @Override
    public void onWeaponReloaded(PlayerWeaponEvent e) {
        // Nothing to do here

    }

    /**
     * The controller does not react to this event
     *
     * @param e the event corresponding to the player unloading a weapon
     */
    @Override
    public void onWeaponUnloaded(PlayerWeaponEvent e) {
        // Nothing to do here

    }

    /**
     * The controller does not react to this event
     *
     * @param e the event corresponding to the player picking up a weapon
     */
    @Override
    public void onWeaponPicked(WeaponExchanged e) {
        // Nothing to do here

    }

    /**
     * The controller does not react to this event
     *
     * @param e the event corresponding to the player dropping a weapon
     */
    @Override
    public void onWeaponDropped(WeaponExchanged e) {
        // Nothing to do here

    }

    /**
     * The controller does not react to this event
     *
     * @param e the event corresponding to the player's wallet changing
     */
    @Override
    public void onWalletChanged(PlayerWalletChanged e) {
        // Nothing to do here

    }

    /**
     * The controller does not react to this event
     *
     * @param e the event corresponding to the player's health changing
     */
    @Override
    public void onHealthChanged(PlayerEvent e) {
        // Nothing to do here

    }

    /**
     * Finds the available powerup given a player and the trigger
     *
     * @param ownedPowerupTiles the powerup tile
     * @param trigger the trigger
     * @param owner the player who owns the powerup tile
     * @return a list of available powerups
     */
    private List<Powerup> findAvailablePowerups(List<PowerupTile> ownedPowerupTiles, Powerup.Trigger trigger, Player owner) {
        List<Powerup> availablePowerups = ownedPowerupTiles
                .stream()
                .map(powerupTile -> PowerupFactory.getPowerupMap().get(powerupTile.getName()))
                .filter(powerup -> powerup.getTrigger().equals(trigger))
                // + 1 needed to avoid paying a powerup with itself
                .filter(powerup -> PaymentHandler.canAfford(powerup.getCost() + 1, owner))
                .collect(Collectors.toList());
        boolean seesSomeone = players.stream().anyMatch(player -> player != owner && owner.sees(player));
        boolean canHitOthers = players.stream().anyMatch(player -> player != owner && match.getBoard().findPlayer(player).isPresent());
        availablePowerups.removeIf(powerup -> (powerup.getTargetConstraint() == Powerup.TargetConstraint.VISIBLE && !seesSomeone) || powerup.getTarget() == Powerup.Target.OTHERS && !canHitOthers);
        return availablePowerups;
    }

    /**
     * Intersect the powerup tiles owned by the player and the available ones by name
     *
     * @param ownedPowerupTiles the player's powerups
     * @param availablePowerups the available powerups
     * @return the intersection of the two list
     */
    private List<PowerupTile> filterPowerupTiles(List<PowerupTile> ownedPowerupTiles, List<Powerup> availablePowerups) {
        List<String> availableTypes = availablePowerups
                .stream()
                .map(Powerup::getName)
                .collect(Collectors.toList());
        return ownedPowerupTiles
                .stream()
                .filter(tile -> availableTypes.contains(tile.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Manages the powerup related actions
     *
     * @param powerupOwner owner of the powerups
     * @param powerupTarget the target of the powerup effect
     * @param trigger the trigger of the powerup
     * @param message the message to send to the player
     */
    private void managePowerups(Player powerupOwner, @Nullable Player powerupTarget, Powerup.Trigger trigger, String message) {
        Interviewer interviewer = playerViews.get(powerupOwner);
        List<PowerupTile> ownedTiles = new LinkedList<>(powerupOwner.getPowerups());
        List<Powerup> availablePowerups = findAvailablePowerups(ownedTiles, trigger, powerupOwner);
        ownedTiles = filterPowerupTiles(ownedTiles, availablePowerups);
        while (!ownedTiles.isEmpty()) {
            Optional<it.polimi.ingsw.shared.datatransferobjects.Powerup> selected = optionalPowerupSelection(ownedTiles, interviewer, message);
            if (selected.isPresent()) {
                Powerup chosenPowerup = availablePowerups
                        .stream()
                        .filter(p -> p.getName().equals(selected.get().getName()))
                        .findAny()
                        .orElseThrow(() -> new IllegalStateException("Powerup " + selected.get().getName() + " was not available"));
                PowerupTile chosenTile = ownedTiles
                        .stream()
                        .filter(p -> p.getName().equals(selected.get().getName()) && p.getColor().equals(selected.get().getColor()))
                        .findAny()
                        .orElseThrow(() -> new IllegalStateException("PowerupTile " + selected.get().getName() + " does not exist"));
                PaymentHandler.pay(chosenPowerup.getCost(), powerupOwner, interviewer, chosenTile);
                discardPowerupTile(powerupOwner, chosenTile);
                if (powerupTarget == null) {
                    chosenPowerup.activate(powerupOwner, selectTarget(chosenPowerup, powerupOwner, interviewer), interviewer);
                } else {
                    chosenPowerup.activate(powerupOwner, powerupTarget, interviewer);
                }
                ownedTiles = new LinkedList<>(powerupOwner.getPowerups());
                availablePowerups = findAvailablePowerups(ownedTiles, trigger, powerupOwner);
                ownedTiles = filterPowerupTiles(ownedTiles, availablePowerups);
            } else {
                ownedTiles.clear();
            }
        }

    }


    /**
     * This method selects the target for SELF or OTHERS target types
     * @param powerup the Powerup that needs to know his targets
     * @param self the Player who is activating the powerup
     * @param view the interface that will select the target if needed
     * @return the optional of the target, if available. Optional.Empty() otherwise
     */
    private Player selectTarget(Powerup powerup, Player self, Interviewer view){
        switch (powerup.getTarget()) {
            case SELF:
                return self;
            case OTHERS:
                List<Player> possibleTargets = new LinkedList<>(match.getPlayers());
                if (powerup.getTargetConstraint() == Powerup.TargetConstraint.VISIBLE) {
                    possibleTargets = possibleTargets.stream().filter(player -> player != self && self.sees(player)).collect(Collectors.toList());
                } else if (powerup.getTargetConstraint() == Powerup.TargetConstraint.NONE) {
                    possibleTargets = possibleTargets.stream().filter(player -> player != self && match.getBoard().findPlayer(player).isPresent()).collect(Collectors.toList());
                }
                List<String> targetsForView = possibleTargets
                        .stream()
                        .map(player -> player.getPlayerInfo().getNickname())
                        .collect(Collectors.toList());
                String choice = view.select("Who do you want to use the powerup against?", targetsForView, ClientApi.TARGET_QUESTION);
                return possibleTargets
                        .stream()
                        .filter(t -> t.getPlayerInfo().getNickname().equals(choice))
                        .findAny()
                        .orElseThrow(() -> new IllegalStateException("No target was selected"));
            default:
                throw new IllegalArgumentException("Can't select the target of the given powerup");
        }
    }

    /**
     * This method manages the discard process of a powerup. Discard the powerup from the player's hand
     * and puts it in the discarded powerup pack
     * @param player player who needs to discard the powerup
     * @param toBeDiscarded powerup to be discarded
     */
    private void discardPowerupTile(Player player, PowerupTile toBeDiscarded){
        //Here we discard the powerup. We first put the powerup in the discarded pack
        powerupTileDeck.discard(toBeDiscarded);
        //We then say the player to discard the powerup from his hand
        player.discardPowerup(toBeDiscarded);
    }

    /**
     * Manages the view reconnection on an active match
     *
     * @param e the view reconnected event object
     */
    @Override
    public void onViewReconnected(ViewReconnected e) {
        Optional<View> oldView = views.stream().filter(view -> !view.isConnected() && view.getNickname().equals(e.getView().getNickname())).findAny();
        if (oldView.isPresent()) {
            e.consume();

            e.getView().setPlayer(oldView.get().getPlayer());
            e.getView().addViewListener(this);
            views.set(views.indexOf(oldView.get()), e.getView());
            playerViews.put(oldView.get().getPlayer(), e.getView());

            views.forEach(view -> {
                if (e.getView() != view) {
                    view.addViewListener(e.getView());
                    e.getView().addViewListener(view);
                }
            });
            match.getPlayers().forEach(player -> {
                player.addPlayerListener(e.getView());
            });
            match.addMatchListener(e.getView());
            match.getBoard().addBoardListener(e.getView());

            e.getView().setReady(match);
        }
    }

    /**
     * The controller does not react to this event
     *
     * @param e the event corresponding to the player discarding a powerup
     */
    @Override
    public void onPowerupDiscarded(PowerupExchange e){
        // nothing to do here

    }

    /**
     * The controller does not react to this event
     *
     * @param e the event corresponding to the player grabbing a powerup
     */
    @Override
    public void onPowerupGrabbed(PowerupExchange e){
        // nothing to do here

    }

    /**
     * The controller does not react to this event
     *
     * @param e the event corresponding to the player choosing a spawnpoint
     */
    @Override
    public void onSpawnpointChosen(SpawnpointChoiceEvent e) {
        // nothing to do here
    }

    /**
     * Ask the view to select a powerup or none
     *
     * @param powerups the list of powerup to choose from
     * @param view the view to ask
     * @param message the message to present to the player
     * @return the player selection
     */
    private Optional<it.polimi.ingsw.shared.datatransferobjects.Powerup> optionalPowerupSelection(List<PowerupTile> powerups, Interviewer view, String message) {
        List<it.polimi.ingsw.shared.datatransferobjects.Powerup> playerPowerupsVM = powerups.stream()
                .map(p -> new it.polimi.ingsw.shared.datatransferobjects.Powerup(p.getName(), p.getColor()))
                .collect(Collectors.toList());

        return view.selectOptional(message, playerPowerupsVM, ClientApi.POWERUP_QUESTION);
    }

    /**
     * Asks the player to select a spawnpoint
     *
     * @param powerups the powerups to choose from
     * @param view the view to ask
     * @param message the message to show to the player
     * @return the player selection
     */
    private it.polimi.ingsw.shared.datatransferobjects.Powerup mandatorySpawnpointSelection(List<PowerupTile> powerups, Interviewer view, String message) {
        List<it.polimi.ingsw.shared.datatransferobjects.Powerup> playerPowerupsVM = powerups.stream()
                .filter(p -> powerups.stream().anyMatch(powerupController -> powerupController.getName().equals(p.getName())))
                .map(p -> new it.polimi.ingsw.shared.datatransferobjects.Powerup(p.getName(), p.getColor()))
                .collect(Collectors.toList());

        return view.select(message, playerPowerupsVM, ClientApi.SPAWNPOINT_QUESTION);
    }

    /**
     * When a view disconnects this listener handles the controller related operations
     *
     * @param e the event corresponding to the view disconnection
     */
    @Override
    public void onViewDisconnected(ViewEvent e) {
        e.getView().removeViewListener(this);
        new Thread(() -> {
            try {
                e.getView().close();
            } catch (Exception ex) {
                logger.warning("Couldn't close view " + ex);
            }
        }).start();
    }

    /**
     * The controller does not react to this event
     *
     * @param e the event corresponding to the view being ready
     */
    @Override
    public void onViewReady(ViewEvent e) {
        // Nothing to do here
    }

    /**
     * Adds a controller listener
     *
     * @param l the listener to add
     */
    public void addListener(ControllerListener l) {
        listeners.add(l);
    }

    /**
     * Signals to close the currently active match and controller
     */
    public void close() {
        closed = true;
    }
}