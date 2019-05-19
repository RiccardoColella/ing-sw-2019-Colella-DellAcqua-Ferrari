package it.polimi.ingsw.server.controller;

import it.polimi.ingsw.server.bootstrap.events.ViewReconnected;
import it.polimi.ingsw.server.bootstrap.events.listeners.ViewReconnectedListener;
import it.polimi.ingsw.server.controller.powerup.Powerup;
import it.polimi.ingsw.server.controller.powerup.PowerupFactory;
import it.polimi.ingsw.server.controller.weapons.Weapon;
import it.polimi.ingsw.server.controller.weapons.WeaponFactory;
import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.SpawnpointBlock;
import it.polimi.ingsw.server.model.collections.Deck;
import it.polimi.ingsw.server.model.currency.*;
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
import it.polimi.ingsw.shared.messages.ClientApi;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class has the purpose of managing the game flow
 */
public class Controller implements Runnable, PlayerListener, ViewReconnectedListener {
    /**
     * Logging utility
     */
    protected final Logger logger = Logger.getLogger(this.getClass().getName());

    private Match match;
    private List<View> views;
    private List<Player> players;
    private final Map<String, Weapon> weaponMap;
    private Deck<PowerupTile> powerupTileDeck;
    private Map<Player, View> playerViews = new HashMap<>();
    private int minClients;

    public Controller(Match match, List<View> views, int minClients) {

        if (views.size() != match.getPlayers().size()) {
            throw new IllegalArgumentException("View number does not match player number");
        }

        this.minClients = minClients;
        this.match = match;
        this.views = views;
        this.players = match.getPlayers();
        views.forEach(view -> this.playerViews.put(view.getPlayer(), view));
        this.players.forEach(player -> player.addPlayerListener(this));
        this.weaponMap = WeaponFactory.createWeaponDictionary(match.getBoard());
        powerupTileDeck = match.getPowerupDeck();
        for (CurrencyColor currencyColor : CurrencyColor.values()){
            SpawnpointBlock block = match.getBoard().getSpawnpoint(currencyColor);
            for (int i = 0; i < block.getMaxWeapons(); i++) {
                WeaponTile weaponTile = match.getWeaponDeck().pick().orElseThrow(() -> new IllegalStateException("Not enough weapons to build the board"));
                block.drop(weaponTile);
            }
        }
        match.start();
    }

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

        while (!match.isEnded() && connectedViews >= minClients) {
            activePlayer = match.getActivePlayer();
            manageActivePlayerTurn(activePlayer, playerViews.get(activePlayer));

            connectedViews = views.stream().mapToInt(view -> view.isConnected() ? 1 : 0).reduce(0, Integer::sum);
        }
        logger.info("The match is over");
    }

    /**
     * This functions manages how to manage the player's turn
     * @param activePlayer is the player who has to execute his turn
     * @param view is the interface that manages the turn
     */
    private void manageActivePlayerTurn(Player activePlayer, Interviewer view) {
        if (playerViews.get(activePlayer).isConnected()) {
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
            PowerupTile playerPowerup = selectSpawnpointFromPowerup(playerPowerups, activePlayer, view);
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
     * @param pickedTurredBlocks list of all turret blocks where the player has just picked up in the current turn
     */
    private void manageChosenAction(BasicAction basicActionChosen, Player activePlayer, Interviewer view, List<Block> pickedTurredBlocks){
        switch (basicActionChosen){
            case GRAB:
                logger.info("Managing GRAB move...");
                if (activePlayer.isOnASpawnpoint()){
                    if (!grabOnASpawnpoint(activePlayer, view)){
                        throw new IllegalArgumentException("Grab on a spawnpoint not executed");
                    }
                } else {
                    grabNotOnASpawnpoint(activePlayer);
                    pickedTurredBlocks.add(activePlayer.getBlock());
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
        Optional<BonusTile> optionalCard = match.getBonusDeck().pick();
        if (optionalCard.isPresent()){
            BonusTile card = optionalCard.get();
            activePlayer.grabAmmoCubes(card.getRewards());
            match.getBonusDeck().discard(card);
            //Maybe here we should tell the player he got some ammos
            if (card.canPickPowerup() &&
                    activePlayer.getPowerups().size() < activePlayer.getConstraints().getMaxPowerupsForPlayer()){
                logger.info("Grabbing power-up too...");
                powerupTileDeck.pick().ifPresent(activePlayer::grabPowerup);
            }

        } else throw new NullPointerException("Card from bonusDeck while grabbing ammo is Optional.empty()");
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
            throw new IllegalArgumentException("Player cannot afford to reload any weapon or all weapons are loaded. The reload shouldn't be selectable");
        } else {
            WeaponTile weaponToReload;
            List<String> weaponsReloadableForView = weaponsReloadable
                    .stream()
                    .map(WeaponTile::getName)
                    .collect(Collectors.toList());
            do {
                Optional<String> selected = view.selectOptional("Which weapon would you like to reload?", weaponsReloadableForView, ClientApi.RELOAD_QUESTION);
                if (selected.isPresent()) {
                    weaponsReloadableForView.remove(selected.get());
                    weaponToReload = weaponsReloadable
                            .stream()
                            .filter(w -> w.getName().equals(selected.get()))
                            .findAny()
                            .orElseThrow(() -> new IllegalStateException("Weapon to reload does not exist"));
                    reloadWeapon(weaponToReload, activePlayer, view);
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
        SpawnpointBlock spawnpointBlock = (SpawnpointBlock) activePlayer.getBlock();
        spawnpointBlock.grabWeapon(weapon);
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
    private void manageActions(Player player, Interviewer view){
        List<Block> pickedTurretBlocks = new LinkedList<>();
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
                        .filter(basicAction -> canPerformAction(player, basicAction, pickedTurretBlocks))
                        .collect(Collectors.toSet());
                //Here we ask the view to choose a basic action
                move = availableActions.isEmpty() ?
                        Optional.empty() :
                        view.selectOptional("Which move would you like to execute?", availableActions, ClientApi.BASIC_ACTION_QUESTION);
                //Here we execute that basic action
                move.ifPresent(basicAction -> {
                    manageChosenAction(basicAction, player, view, pickedTurretBlocks);
                    playedActions.add(basicAction);
                });
            } while (move.isPresent());
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
     * @param pickedTurretBlocks list of all Turret Blocks from which the player has picked-up in the current turn
     * @return true if the player can execute the action
     */
    private boolean canPerformAction(Player player, BasicAction action, List<Block> pickedTurretBlocks){
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
                    return !pickedTurretBlocks.contains(player.getBlock()) && !player.isFullOfAmmoAndPowerups();
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

    @Override
    public void onPlayerDied(PlayerDied event) {
        // Nothing to do here
    }

    @Override
    public void onPlayerDamaged(PlayerDamaged e) {
        managePowerups(e.getAttacker(), e.getVictim(), Powerup.Trigger.ON_DAMAGE_GIVEN, "Do you want to use a powerup against " + e.getVictim().getPlayerInfo().getNickname() + "?");
        managePowerups(e.getVictim(), e.getAttacker(), Powerup.Trigger.ON_DAMAGE_RECEIVED, "Do you want to use a powerup against " + e.getAttacker().getPlayerInfo().getNickname() + "?");
    }

    @Override
    public void onPlayerOverkilled(PlayerOverkilled event) {
        // Nothing to do here
    }

    @Override
    public void onPlayerReborn(PlayerEvent event) {
        // Nothing to do here
    }

    @Override
    public void onPlayerBoardFlipped(PlayerEvent e) {
        // Nothing to do here

    }

    @Override
    public void onPlayerTileFlipped(PlayerEvent e) {
        // Nothing to do here

    }

    @Override
    public void onWeaponReloaded(PlayerWeaponEvent e) {
        // Nothing to do here

    }

    @Override
    public void onWeaponUnloaded(PlayerWeaponEvent e) {
        // Nothing to do here

    }

    @Override
    public void onWeaponPicked(WeaponExchanged e) {
        // Nothing to do here

    }

    @Override
    public void onWeaponDropped(WeaponExchanged e) {
        // Nothing to do here

    }

    @Override
    public void onWalletChanged(PlayerWalletChanged e) {
        // Nothing to do here

    }

    @Override
    public void onHealthChanged(PlayerEvent e) {
        // Nothing to do here

    }

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

    @Override
    public void onViewReconnected(ViewReconnected e) {
        // TODO: Manage client reconnection
    }

    @Override
    public void onPowerupDiscarded(PowerupExchange e){
        // nothing to do here

    }

    @Override
    public void onPowerupGrabbed(PowerupExchange e){
        // nothing to do here

    }

    @Override
    public void onSpawnpointChosen(SpawnpointChoiceEvent e) {
        // nothing to do here
    }

    private Optional<it.polimi.ingsw.shared.datatransferobjects.Powerup> optionalPowerupSelection(List<PowerupTile> powerups, Interviewer view, String message) {
        List<it.polimi.ingsw.shared.datatransferobjects.Powerup> playerPowerupsVM = powerups.stream()
                .map(p -> new it.polimi.ingsw.shared.datatransferobjects.Powerup(p.getName(), p.getColor()))
                .collect(Collectors.toList());

        return view.selectOptional(message, playerPowerupsVM, ClientApi.POWERUP_QUESTION);
    }

    private it.polimi.ingsw.shared.datatransferobjects.Powerup mandatorySpawnpointSelection(List<PowerupTile> powerups, Interviewer view, String message) {
        List<it.polimi.ingsw.shared.datatransferobjects.Powerup> playerPowerupsVM = powerups.stream()
                .filter(p -> powerups.stream().anyMatch(powerupController -> powerupController.getName().equals(p.getName())))
                .map(p -> new it.polimi.ingsw.shared.datatransferobjects.Powerup(p.getName(), p.getColor()))
                .collect(Collectors.toList());

        return view.select(message, playerPowerupsVM, ClientApi.SPAWNPOINT_QUESTION);
    }
}