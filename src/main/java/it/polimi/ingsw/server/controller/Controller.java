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
import it.polimi.ingsw.server.model.exceptions.UnauthorizedExchangeException;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.*;
import it.polimi.ingsw.server.model.weapons.WeaponTile;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.server.view.View;
import it.polimi.ingsw.shared.messages.ClientApi;
import it.polimi.ingsw.utils.Tuple;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public Controller(Match match, List<View> views) {

        if (views.size() != match.getPlayers().size()) {
            throw new IllegalArgumentException("View number does not match player number");
        }

        this.match = match;
        this.views = views;
        this.players = match.getPlayers();
        this.players.forEach(player -> player.addPlayerListener(this));
        this.weaponMap = WeaponFactory.createWeaponDictionary(match.getBoard());
        powerupTileDeck = match.getPowerupDeck();
        match.start();
    }

    @Override
    public void run() {
        Player activePlayer;
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
            PowerupTile discardedPowerup = selectSpawnpointFromPowerup(powerups, player);
            discardPowerupTile(player, discardedPowerup);
            logger.info("Selected Spawnpoint " + discardedPowerup.getColor());
            //Grabbing the other powerup
            player.grabPowerup(powerups.get(0));
            manageActivePlayerTurn(player, views.get(players.indexOf(player)));
        }

        while (!match.isEnded()) {
            activePlayer = match.getActivePlayer();
            manageActivePlayerTurn(activePlayer, views.get(players.indexOf(activePlayer)));
        }
    }

    /**
     * This functions manages how to manage the player's turn
     * @param activePlayer is the player who has to execute his turn
     * @param view is the interface that manages the turn
     */
    private void manageActivePlayerTurn(Player activePlayer, Interviewer view) {
        logger.info("Managing actions...");
        manageActions(activePlayer, view);
        logger.info("No more actions to be managed");
        logger.info("Ending turn. Checking for died players...");
        for (Player player : match.endTurn()) {
            //Here we iterate on the dead players returned by match.endTurn()
            logger.info("Player " + player.getColor() + " died.");
            Optional<PowerupTile> powerupTile = powerupTileDeck.pick();
            List<PowerupTile> playerPowerups = new LinkedList<>(player.getPowerups());
            //If the player had at least one powerup in his hand he can choose what power-up to discard
            if (powerupTile.isPresent() && playerPowerups.size() > 1){
                playerPowerups.add(powerupTile.get());
                powerupTile = Optional.of(selectSpawnpointFromPowerup(playerPowerups, activePlayer));
            }
            //Discarded powerup define respawn point. Player is moved to that spawnpoint and reanimated.
            if (powerupTile.isPresent()){
                match.getBoard().teleportPlayer(player, match.getBoard().getSpawnpoint(powerupTile.get().getColor()));
                discardPowerupTile(activePlayer, powerupTile.get());
            } else throw new IllegalStateException("Powerup to respawn not found");
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
    private PowerupTile selectSpawnpointFromPowerup(List<PowerupTile> powerups, Player player){
        //Asking what powerup to discard
        List<Tuple<String, CurrencyColor>> powerupsForView = powerups
                .stream()
                .map(powerupTile -> new Tuple<>(powerupTile.getName(), powerupTile.getColor()))
                .collect(Collectors.toList());
        PowerupTile discardedPowerup = powerups.get(powerupsForView.indexOf(views.get(players.indexOf(player)).select("Select Spawnpoint: ", powerupsForView, ClientApi.SPAWNPOINT_QUESTION)));
        match.getBoard().getSpawnpoint(discardedPowerup.getColor()).addPlayer(match.getActivePlayer());
        return discardedPowerup;
    }

    /**
     * This function manages the action chosen by the player
     * @param basicActionChosen is the action chosen
     * @param activePlayer is the player who is acting the move
     * @param view is the interface that manages the chosen action
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
     */
    private boolean grabOnASpawnpoint(Player activePlayer, Interviewer view){
        //choose weapon to be picked up
        SpawnpointBlock block =  (SpawnpointBlock) activePlayer.getBlock();
        List<WeaponTile> availableWeapons = new LinkedList<>(block.getWeapons());
        List<WeaponTile> affordableWeapons = availableWeapons.stream().filter(weapon -> PaymentHandler.canAfford(weapon.getAcquisitionCost(), activePlayer))
                .collect(Collectors.toList());
        if (affordableWeapons.size() > 0){
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
                .filter(WeaponTile::isLoaded)
                .collect(Collectors.toList());
        if (!loadedPlayerWeapons.isEmpty()){
            WeaponTile selectedWeapon = selectWeaponTile("Which weapon do you want to use for shooting?", loadedPlayerWeapons, view);
            Weapon weapon = weaponMap.get(selectedWeapon.getName());
            weapon.shoot(view, activePlayer);
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
                .filter(weaponOfPlayer -> PaymentHandler.canAfford(weaponOfPlayer.getAcquisitionCost(), activePlayer))
                .collect(Collectors.toList());
        if (weaponsReloadable.isEmpty()){
            throw new IllegalArgumentException("All weapons are reloaded. The reload shouldn't be selectable");
        } else {
            List<String> weaponsReloadableForView = weaponsReloadable
                    .stream()
                    .map(WeaponTile::getName)
                    .collect(Collectors.toList());
            WeaponTile weaponToReload = weaponsReloadable.get(weaponsReloadable.indexOf(view.select("Which weapon would you like to reload?", weaponsReloadableForView, ClientApi.RELOAD_QUESTION)));
            reloadWeapon(weaponToReload, activePlayer, view);
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
        if (PaymentHandler.canAfford(acquisitionCost, activePlayer)){
            List<Coin> paymentMethod = PaymentHandler.collectCoins(acquisitionCost, activePlayer, view);
            try {
                activePlayer.grabWeapon(weapon, paymentMethod);
            } catch (UnauthorizedExchangeException e){
                //To pick up the selected weapon the player needs to discard one
                WeaponTile weaponToDiscard = selectWeaponTile("Which weapon do you want to discard?", activePlayer.getWeapons(), view);
                activePlayer.grabWeapon(weapon, paymentMethod, weaponToDiscard);
            }
        }
    }

    /**
     * This method manages the weaponTile selection for a WEAPON_CHOICE_QUESTION
     * @param string string to be sent as question to the client
     * @param weaponTiles selectable weapon tiles
     * @param view view that will manage the selection
     * @return the selected weapon tile
     */
    private WeaponTile selectWeaponTile(String string, List<WeaponTile> weaponTiles, Interviewer view){
        List<String> weaponNames = weaponTiles
                .stream()
                .map(WeaponTile::getName)
                .collect(Collectors.toList());
        return weaponTiles.get(weaponNames.indexOf(view.select(string, weaponNames, ClientApi.WEAPON_CHOICE_QUESTION)));
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
            managePowerupBetweenActions(player, view);
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
        managePowerupBetweenActions(player, view);
    }

    /**
     * This method returns a stream of Optional of Tuples of Compound Actions and an Integer, where the Tuple indicate
     * in the compound actions the possible BasicActions that are available
     * @param playedActions is the list of past played actions in this turn by the player
     * @param compoundActions is the list of CompoundActions the player can play in the macro action. It may depends from the
     *                        different game mode or player situations
     * @return a stream of Optional of Tuples of a Compound Action and an Integer, where the Tuple indicate
     *         in the compound actions the possible BasicActions that are available
     */
    private Stream<Optional<Tuple<CompoundAction, Integer>>> candidateActions(List<BasicAction> playedActions, List<CompoundAction> compoundActions){
        return compoundActions
                .stream()
                .map(compoundAction -> {
                    //Here we take the basic actions from the compound action
                    List<BasicAction> actions = compoundAction.getActions();
                    //Here we select the possible basic action the player may do, checking the composition of the compound actions
                    for (
                            int i = 0;
                            i < actions.size() - playedActions.size() &&
                                    actions.get(i) == BasicAction.MOVE;
                            i++
                    ){
                        //If the previous actions are compatible with the inspected compound action, we select the index of next possible basic action
                        //The method is thought to be continued in candidateBasicActions()
                        if (actions.subList(i, i+playedActions.size()).equals(playedActions))
                            return  Optional.of(new Tuple<>(compoundAction, i));
                    }
                    return Optional.empty();
                });
    }

    /**
     * This private method calculate the possible BasicActions the player can do
     * @param playedActions is the list of actions done in this turn by the players
     * @param compoundActions is the list of possible compound actions the player can do in that particular macro action.
     *                        They can variate from gaming mode to gaming mode or by the state of the player
     * @return a Set of possible basic actions the player can execute
     */
    private Set<BasicAction> candidateBasicActions(List<BasicAction> playedActions, List<CompoundAction> compoundActions){
        return candidateActions(playedActions, compoundActions)
                //We select only the present basic actions
                .filter(Optional::isPresent)
                //And we get them
                .map(Optional::get)
                .map(tuple -> {
                    //we get the basic action
                    int index = tuple.getItem2() + 1;
                    List<BasicAction> compoundAction = tuple.getItem1().getActions();
                    if (compoundAction.get(index) == BasicAction.MOVE) {
                        Set<BasicAction> actionsSet = new HashSet<>();
                        actionsSet.add(BasicAction.MOVE);
                        for (int i = index + 1; i < compoundAction.size(); i++) {
                            if (compoundAction.get(i) != BasicAction.MOVE) {
                                actionsSet.add(compoundAction.get(i));
                                return actionsSet;
                            }
                        }
                        return actionsSet;
                    } else {
                        return new HashSet<>(Collections.singleton(compoundAction.get(index)));
                    }
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
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

    /**
     * This method manages the use of powerups during a player's turn
     * @param activePlayer is the player who is executing the turn
     * @param view is the interface that manages the player's turn
     */
    private void managePowerupBetweenActions(Player activePlayer, Interviewer view){
        Map<String, Powerup>  powerupMap = PowerupFactory.getPowerupMap();
        List<PowerupTile> playerPowerupTiles = activePlayer.getPowerups();
        List<Powerup> playerPowerups = new LinkedList<>();
        Optional<Tuple<String, CurrencyColor>> selectedPowerup;
        for (PowerupTile powerupTile : playerPowerupTiles){
            Powerup powerup = powerupMap.get(powerupTile.getName());

            if (powerup.getTrigger() == Powerup.Trigger.IN_BETWEEN_ACTIONS && PaymentHandler.canAfford(powerup.getCost(), activePlayer)) {
                playerPowerups.add(powerup);
            }
        }
        //If there are available powerups we ask the player if he wants to use some
        if (!playerPowerups.isEmpty()) {

            List<Tuple<String, CurrencyColor>> playerPowerupsVM = playerPowerupTiles.stream()
                    .filter(p -> playerPowerups.stream().anyMatch(powerupController -> powerupController.getName().equals(p.getName())))
                    .map(p -> new Tuple<>(p.getName(), p.getColor()))
                    .collect(Collectors.toList());

            selectedPowerup = view.selectOptional("Do you want to use a powerup?", playerPowerupsVM, ClientApi.POWERUP_QUESTION);
            if (selectedPowerup.isPresent()){
                //If the player wants to use a powerup we select the target
                Powerup powerup = playerPowerups.stream().filter(p -> p.getName().equals(selectedPowerup.get().getItem1())).findFirst().orElseThrow(() -> new IllegalStateException("Powerup not found"));
                Optional<Player> optionalTarget = selectTarget(powerup, activePlayer, view);
                if (optionalTarget.isPresent()){
                    //If the target is present/available we use the powerup
                    PaymentHandler.pay(powerup.getCost(), activePlayer, view);
                    powerup.activate(activePlayer, optionalTarget.get(), view);
                    //After the use, we discard the powerupTile from the player's hand and we put it in the discarded pack
                    discardPowerupTile(activePlayer, powerup);
                }
            }
        }
    }

    @Override
    public void onPlayerDied(PlayerDied event) {
        // Nothing to do here
    }

    @Override
    public void onPlayerDamaged(PlayerDamaged e) {
        manageAttackerPowerups(e.getAttacker(), e.getVictim());
        manageVictimPowerups(e.getVictim(), e.getAttacker());
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
    public void onWeaponReloaded(WeaponEvent e) {
        // Nothing to do here

    }

    @Override
    public void onWeaponUnloaded(WeaponEvent e) {
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

    /**
     * This method manages the powerups of an attacker when an attack is performed
     * @param attacker is the player who is performing the attack
     * @param victim is the player who is receiving the attack
     */
    private void manageAttackerPowerups(Player attacker, Player victim){
        Interviewer attackerView = views.get(players.indexOf(attacker));
        List<PowerupTile> availablePowerupTiles = new LinkedList<>(attacker.getPowerups());
        if (!availablePowerupTiles.isEmpty()){
            //If the player has some powerups I select those that can be used and pay in the current situation
            Map<String, Powerup> powerupMap = PowerupFactory.getPowerupMap();
            List<Powerup> availablePowerups = availablePowerupTiles
                    .stream()
                    .map(powerupTile -> powerupMap.get(powerupTile.getName()))
                    .filter(powerup -> powerup.getTrigger() == Powerup.Trigger.ON_DAMAGE_GIVEN)
                    .filter(powerup -> PaymentHandler.canAfford(powerup.getCost(), attacker))
                    .collect(Collectors.toList());
            //If some powerups can be used in this situation the player is asked if he wants to use some
            while (!availablePowerups.isEmpty()){

                List<Tuple<String, CurrencyColor>> playerPowerupsVM = availablePowerupTiles.stream()
                        .filter(p -> availablePowerups.stream().anyMatch(powerupController -> powerupController.getName().equals(p.getName())))
                        .map(p -> new Tuple<>(p.getName(), p.getColor()))
                        .collect(Collectors.toList());

                Optional<Tuple<String, CurrencyColor>> selectedPowerup = attackerView.selectOptional("Do you want to use a powerup against " + victim.getColor() + "?", playerPowerupsVM, ClientApi.POWERUP_QUESTION);

                if (selectedPowerup.isPresent()){
                    Powerup powerup = availablePowerups.stream().filter(p -> p.getName().equals(selectedPowerup.get().getItem1())).findFirst().orElseThrow(() -> new IllegalStateException("Powerup not found"));
                    PaymentHandler.pay(powerup.getCost(), attacker, attackerView);
                    powerup.activate(attacker, victim, attackerView);
                    discardPowerupTile(attacker, powerup);
                } else availablePowerups.clear();
            }
        }
    }

    /**
     * This method manages the powerups of a victim when an attack is received
     * @param victim is the player who is receiving the attack
     * @param attacker is the player who is performing the attack
     */
    private void manageVictimPowerups(Player victim, Player attacker){
        Interviewer victimView = views.get(players.indexOf(victim));
        List<PowerupTile> availablePowerupTiles = new LinkedList<>(victim.getPowerups());
        if (!availablePowerupTiles.isEmpty()){
            //If the player has some powerups I select those that can be used and pay in the current situation
            Map<String, Powerup> powerupMap = PowerupFactory.getPowerupMap();
            List<Powerup> availablePowerups = availablePowerupTiles
                    .stream()
                    .map(powerupTile -> powerupMap.get(powerupTile.getName()))
                    .filter(powerup -> powerup.getTrigger() == Powerup.Trigger.ON_DAMAGE_RECEIVED)
                    .filter(powerup -> PaymentHandler.canAfford(powerup.getCost(), attacker))
                    .collect(Collectors.toList());
            //If some powerups can be used in this situation the player is asked if he wants to use some
            while (!availablePowerups.isEmpty()){

                List<Tuple<String, CurrencyColor>> playerPowerupsVM = availablePowerupTiles.stream()
                        .filter(p -> availablePowerups.stream().anyMatch(powerupController -> powerupController.getName().equals(p.getName())))
                        .map(p -> new Tuple<>(p.getName(), p.getColor()))
                        .collect(Collectors.toList());

                Optional<Tuple<String, CurrencyColor>> selectedPowerup = victimView.selectOptional("Do you want to use a powerup against " + attacker.getColor() +"?", playerPowerupsVM, ClientApi.POWERUP_QUESTION);
                if (selectedPowerup.isPresent()){
                    Powerup powerup = availablePowerups.stream().filter(p -> p.getName().equals(selectedPowerup.get().getItem1())).findFirst().orElseThrow(() -> new IllegalStateException("Powerup not found"));
                    PaymentHandler.pay(powerup.getCost(), victim, victimView);
                    powerup.activate(attacker, attacker, victimView);
                    discardPowerupTile(victim, powerup);
                } else availablePowerups.clear();
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
    private Optional<Player> selectTarget(Powerup powerup, Player self, Interviewer view){
        Optional<Player> target = Optional.empty();
        switch (powerup.getTarget()) {
            case SELF:
                target = Optional.of(self);
                break;
            case OTHERS:
                List<Player> possibleTargets = new LinkedList<>(match.getPlayers());
                if (powerup.getTargetConstraint() == Powerup.TargetConstraint.VISIBLE) {
                    possibleTargets = possibleTargets
                            .stream()
                            .filter(self::sees)
                            .collect(Collectors.toList());
                }
                if (possibleTargets.isEmpty()) {
                    target = Optional.empty();
                } else {
                    List<String> targetsForView = possibleTargets
                            .stream()
                            .map(player -> player.getPlayerInfo().getNickname())
                            .collect(Collectors.toList());
                    Optional<String> choice = view.selectOptional("Do you want to use the powerup against who?", targetsForView, ClientApi.TARGET_QUESTION);
                    if (choice.isPresent()){
                        target = Optional.of(possibleTargets.get(targetsForView.indexOf(choice.get())));
                    }
                }
                break;
            case ATTACKER:
                throw new IllegalArgumentException("Can't select the target of the given powerup");
            case DAMAGED:
                throw new IllegalArgumentException("Can't select the target of the given powerup");
        }
        return target;
    }

    /**
     * This method manages the discard process of a powerup. Retrieve the powerupTile, discard it from the player's hand
     * and puts it in the discarded powerup pack
     * @param player the player who needs to discard a powerup after the use
     * @param powerup powerup to be discarded
     */
    private void discardPowerupTile(Player player, Powerup powerup){
        //Here we are retrieving the powerupTile corresponding to te powerup
        List<PowerupTile> powerupTiles = player.getPowerups();
        List<Tuple<PowerupTile, Powerup>> tuples = new LinkedList<>();
        Map<String, Powerup>  powerupMap = PowerupFactory.getPowerupMap();
        for (PowerupTile powerupTile : powerupTiles){
            tuples.add(new Tuple<>(powerupTile, powerupMap.get(powerupTile.getName())));
        }
        PowerupTile toBeDiscarded = tuples
                .stream()
                .filter(tuple -> tuple.getItem2().equals(powerup))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Powerup to be discarded is not in player's hand"))
                .getItem1();
        //Here we discard the powerup. We first put the powerup in the discarded pack
        powerupTileDeck.discard(toBeDiscarded);
        //We then say the player to discard the powerup from his hand
        powerupTiles.remove(toBeDiscarded);
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
        player.getPowerups().remove(toBeDiscarded);
    }

    @Override
    public void onViewReconnected(ViewReconnected e) {
        // TODO: Manage client reconnection
    }
}