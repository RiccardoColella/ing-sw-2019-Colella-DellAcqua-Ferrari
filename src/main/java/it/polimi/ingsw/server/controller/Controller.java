package it.polimi.ingsw.server.controller;

import it.polimi.ingsw.server.controller.powerup.Powerup;
import it.polimi.ingsw.server.controller.powerup.PowerupFactory;
import it.polimi.ingsw.server.controller.weapons.Weapon;
import it.polimi.ingsw.server.controller.weapons.WeaponFactory;
import it.polimi.ingsw.server.model.battlefield.SpawnpointBlock;
import it.polimi.ingsw.server.model.collections.Deck;
import it.polimi.ingsw.server.model.currency.AmmoCube;
import it.polimi.ingsw.server.model.currency.BonusTile;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.events.PlayerDamaged;
import it.polimi.ingsw.server.model.events.PlayerDied;
import it.polimi.ingsw.server.model.events.PlayerOverkilled;
import it.polimi.ingsw.server.model.events.PlayerReborn;
import it.polimi.ingsw.server.model.events.listeners.PlayerListener;
import it.polimi.ingsw.server.model.exceptions.UnauthorizedExchangeException;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.ActionTile;
import it.polimi.ingsw.server.model.player.BasicAction;
import it.polimi.ingsw.server.model.player.CompoundAction;
import it.polimi.ingsw.server.model.player.Player;
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
public class Controller implements Runnable, PlayerListener {
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
            //Asking what powerup to discard
            PowerupTile discardedPowerup = views.get(players.indexOf(player)).select("Select Spawnpoint: ", powerups, ClientApi.SPAWNPOINT_QUESTION);
            match.getBoard().getSpawnpoint(discardedPowerup.getColor()).addPlayer(match.getActivePlayer());
            //Discarding the selected powerup
            powerups.remove(discardedPowerup);
            powerupTileDeck.discard(discardedPowerup);
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
        manageAction(activePlayer, view);
        logger.info("No more actions to be managed");
        logger.info("Ending turn. Checking for died players...");
        for (Player player : match.endTurn()) {
            logger.info("Player " + player.getColor() + " died.");
            Optional<PowerupTile> powerupTile = powerupTileDeck.pick();
            List<PowerupTile> playerPowerups = new LinkedList<>(player.getPowerups());
            //If the player had at least one powerup in his hand he can choose what power-up to discard
            if (powerupTile.isPresent() && playerPowerups.size() > 1){
                playerPowerups.add(powerupTile.get());
                powerupTile = Optional.of(view.select("Discard a Power-Up to choose where to respawn...", playerPowerups, ClientApi.SPAWNPOINT_QUESTION));
            }
            //Discarded powerup define respawn point. Player is moved to that spawnpoint and reanimated.
            if (powerupTile.isPresent()){
                match.getBoard().teleportPlayer(player, match.getBoard().getSpawnpoint(powerupTile.get().getColor()));
                powerupTileDeck.discard(powerupTile.get());
            } else throw new IllegalStateException("Powerup to respawn not found");
            player.bringBackToLife();
        }
        match.changeTurn();
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
                    grabOnASpawnpoint(activePlayer, view);
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
     */
    private void grabOnASpawnpoint(Player activePlayer, Interviewer view){
        //choose weapon to be picked up
        SpawnpointBlock block =  (SpawnpointBlock) activePlayer.getBlock();
        List<WeaponTile> availableWeapons = new LinkedList<>(block.getWeapons());
        List<WeaponTile> affordableWeapons = availableWeapons.stream().filter(weapon -> PaymentHandler.canAfford(weapon.getAcquisitionCost(), activePlayer))
                .collect(Collectors.toList());
        WeaponTile weapon = view.select("Which weapon would you like to grab?",
                affordableWeapons, ClientApi.WEAPON_CHOICE_QUESTION);
        //pick up
        logger.info("picking up weapon " + weapon.getName() + "...");
        pickUpWeapon(weapon, activePlayer, view);
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
                powerupTileDeck.pick().ifPresent( powerup -> {
                    activePlayer.grabPowerup(powerup);
                    powerupTileDeck.discard(powerup);
                });
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
            WeaponTile selectedWeapon = view.select("Which weapon do you want to use for shooting?", loadedPlayerWeapons, ClientApi.WEAPON_CHOICE_QUESTION);
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
            //TODO: notify player all weapons are reloaded
        } else {
            WeaponTile weaponToReload = view.select("Which weapon would you like to reload?", weaponsReloadable, ClientApi.RELOAD_QUESTION);
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
                WeaponTile weaponToDiscard = view.select("Which weapon do you want to discard?",
                        activePlayer.getWeapons(), ClientApi.WEAPON_CHOICE_QUESTION);
                activePlayer.grabWeapon(weapon, paymentMethod, weaponToDiscard);
            }
        }
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
     * Returns true if the player can do the given action
     * @param player the player who wants to execute the action
     * @param action the action that needs to be executed
     * @return true if the player can execute the action
     */
    private boolean canDo(Player player, BasicAction action){
        boolean returnValue = true;
        switch (action){
            case MOVE:
                break;
            case GRAB:
                if (player.isOnASpawnpoint()) {
                    SpawnpointBlock block = (SpawnpointBlock) player.getBlock();
                    List<WeaponTile> availableWeapons = new LinkedList<>(block.getWeapons());
                    List<WeaponTile> affordableWeapons = availableWeapons.stream().filter(weapon -> PaymentHandler.canAfford(weapon.getAcquisitionCost(), player))
                            .collect(Collectors.toList());
                    returnValue = !affordableWeapons.isEmpty();
                } else returnValue = true;
                break;
            case SHOOT:
                Optional<WeaponTile> activeWeapon = player.getActiveWeapon();
                if (!activeWeapon.isPresent() || !activeWeapon.get().isLoaded()){
                    returnValue = false;
                }
                break;
            case RELOAD:
                break;
        }
        return returnValue;
    }

    /**
     * This function manage the player's actions during player's turn
     * @param player is the player who is currently player
     * @param view is the interface who decides how to manage the player
     */
    private void manageAction(Player player, Interviewer view){
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
                        .filter(basicAction -> canDo(player, basicAction))
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
     * This method manages the use of powerups during a player's turn
     * @param activePlayer is the player who is executing the turn
     * @param view is the interface that manages the player's turn
     */
    private void managePowerupBetweenActions(Player activePlayer, Interviewer view){
        Map<String, Powerup>  powerupMap = PowerupFactory.getPowerupMap();
        List<PowerupTile> playerPowerupTiles = activePlayer.getPowerups();
        List<Powerup> playerPowerups = new LinkedList<>();
        List<Tuple<PowerupTile, Powerup>> tuplesPlayerPowerups = new LinkedList<>();
        Optional<Powerup> selectedPowerup;
        for (PowerupTile powerupTile : playerPowerupTiles){
            playerPowerups.add(powerupMap.get(powerupTile.getName()));
            tuplesPlayerPowerups.add(new Tuple<>(powerupTile, powerupMap.get(powerupTile.getName())));
        }
        //We select all the powerups a player has
        playerPowerups = playerPowerups
                .stream()
                //Checking it can be used between actions
                .filter(powerup -> powerup.getTrigger() == Powerup.Trigger.IN_BETWEEN_ACTIONS)
                //And checking its use can be paid
                .filter(powerup -> PaymentHandler.canAfford(powerup.getCost(), activePlayer))
                .collect(Collectors.toList());
        //If there are available powerups we ask the player if he wants to use some
        if (!playerPowerups.isEmpty()){
            selectedPowerup = view.selectOptional("Do you want to use a powerup?", playerPowerups, ClientApi.POWERUP_QUESTION);
            if (selectedPowerup.isPresent()){
                //If the player wants to use a powerup we select the target
                Powerup powerup = selectedPowerup.get();
                Optional<Player> optionalTarget = selectTarget(powerup, activePlayer, view);
                if (optionalTarget.isPresent()){
                    //If the target is present/available we use the powerup
                    PaymentHandler.pay(powerup.getCost(), activePlayer, view);
                    powerup.activate(activePlayer, optionalTarget.get(), view);
                    //After the use, we put the powerupTile in the discarded Deck
                    PowerupTile toBeDiscarded = tuplesPlayerPowerups.get(playerPowerups.indexOf(powerup)).getItem1();
                    powerupTileDeck.discard(toBeDiscarded);
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
    public void onPlayerReborn(PlayerReborn event) {
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
            Map<String, Powerup> powerupMap = PowerupFactory.getPowerupMap();
            List<Powerup> availablePowerups = availablePowerupTiles
                    .stream()
                    .map(powerupTile -> powerupMap.get(powerupTile.getName()))
                    .filter(powerup -> powerup.getTrigger() == Powerup.Trigger.ON_DAMAGE_GIVEN)
                    .filter(powerup -> PaymentHandler.canAfford(powerup.getCost(), attacker))
                    .collect(Collectors.toList());
            while (!availablePowerups.isEmpty()){
                Optional<Powerup> selectedPowerup = attackerView.selectOptional("Do you want to use a powerup?", availablePowerups, ClientApi.POWERUP_QUESTION);
                if (selectedPowerup.isPresent()){
                    Powerup powerup = selectedPowerup.get();
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
            Map<String, Powerup> powerupMap = PowerupFactory.getPowerupMap();
            List<Powerup> availablePowerups = availablePowerupTiles
                    .stream()
                    .map(powerupTile -> powerupMap.get(powerupTile.getName()))
                    .filter(powerup -> powerup.getTrigger() == Powerup.Trigger.ON_DAMAGE_RECEIVED)
                    .filter(powerup -> PaymentHandler.canAfford(powerup.getCost(), attacker))
                    .collect(Collectors.toList());
            while (!availablePowerups.isEmpty()){
                Optional<Powerup> selectedPowerup = victimView.selectOptional("Do you want to use a powerup?", availablePowerups, ClientApi.POWERUP_QUESTION);
                if (selectedPowerup.isPresent()){
                    Powerup powerup = selectedPowerup.get();
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
                Set<Player> possibleTargets = new HashSet<>(match.getPlayers());
                if (powerup.getTargetConstraint() == Powerup.TargetConstraint.VISIBLE) {
                    possibleTargets = possibleTargets
                            .stream()
                            .filter(self::sees)
                            .collect(Collectors.toSet());
                }
                if (possibleTargets.isEmpty()) {
                    target = Optional.empty();
                } else target = view.selectOptional("Do you want to use the powerup against who?", possibleTargets, ClientApi.TARGET_QUESTION);
                break;
            case ATTACKER:
                throw new IllegalArgumentException("Can't select the target of the given powerup");
            case DAMAGED:
                throw new IllegalArgumentException("Can't select the target of the given powerup");
        }
        return target;
    }

    private void discardPowerupTile(Player player, Powerup powerup){
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
        powerupTileDeck.discard(toBeDiscarded);
        //Maybe here the player should discard the powerup from his hand...
    }
}