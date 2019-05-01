package it.polimi.ingsw.server.controller;

import it.polimi.ingsw.server.model.battlefield.SpawnpointBlock;
import it.polimi.ingsw.server.model.currency.AmmoCube;
import it.polimi.ingsw.server.model.currency.BonusTile;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.events.PlayerDamaged;
import it.polimi.ingsw.server.model.events.listeners.PlayerDamagedListener;
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
public class Controller implements Runnable, PlayerDamagedListener {
    /**
     * Logging utility
     */
    protected final Logger logger = Logger.getLogger(this.getClass().getName());

    private Match match;
    private List<View> views;
    private List<Player> players;

    public Controller(Match match, List<View> views) {

        if (views.size() != match.getPlayers().size()) {
            throw new IllegalArgumentException("View number does not match player number");
        }

        this.match = match;
        this.views = views;
        this.players = match.getPlayers();
        this.players.forEach(player -> player.addPlayerDamagedListener(this));
    }

    /**
     * This function
     */
    @Override
    public void run() {
        Player activePlayer;
        for (Player player : players) {
            List<PowerupTile> powerups = Arrays.asList(
                    match
                            .getPowerupDeck()
                            .pick()
                            .orElseThrow(() -> new IllegalStateException("Empty deck")),
                    match
                            .getPowerupDeck()
                            .pick()
                            .orElseThrow(() -> new IllegalStateException("Empty deck"))
            );
            PowerupTile discardedPowerup = views.get(players.indexOf(player)).select("Select Spawnpoint: ", powerups, ClientApi.SPAWNPOINT_QUESTION);
            match.getBoard().getSpawnpoint(discardedPowerup.getColor()).addPlayer(match.getActivePlayer());
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
            //Se il deck è vuoto che succede?
            Optional<PowerupTile> powerupTile = match.getPowerupDeck().pick();
            List<PowerupTile> playerPowerups = new LinkedList<>(player.getPowerups());
            //If the player had at least one powerup in his hand he can choose what power-up to discard
            if (powerupTile.isPresent() && playerPowerups.size() > 1){
                playerPowerups.add(powerupTile.get());
                powerupTile = Optional.of(view.select("Discard a Power-Up to choose where to respawn...", playerPowerups, ClientApi.SPAWNPOINT_QUESTION));
            }
            //Discarded powerup define respawn point. Player is moved to that spawnpoint and reanimated.
            if (powerupTile.isPresent()){
                match.getBoard().teleportPlayer(player, match.getBoard().getSpawnpoint(powerupTile.get().getColor()));
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
                if (activePlayer.isOnASpawnpoint()){
                    grabOnASpawnpoint(activePlayer, view);
                } else {
                    grabNotOnASpawnpoint(activePlayer);
                }
                break;
            case MOVE:
                activePlayer.move(view.select("Which direction do you want to move?", activePlayer.getAvailableDirections(), ClientApi.DIRECTION_QUESTION));
                break;
            case SHOOT:
                executeShoot(activePlayer, view);
                break;
            case RELOAD:
                executeReload(activePlayer, view);
                break;
        }
    }

    /**
     * This funtions manage the action of grabbing if the player is on a spawnpoint
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
            activePlayer.grabAmmoCubes(optionalCard.get().getRewards());
            //Maybe here we should tell the player he got some ammos
            if (optionalCard.get().canPickPowerup()){
                logger.info("Grabbing power-up too...");
                match.getPowerupDeck().pick().ifPresent(activePlayer::grabPowerup);
            }
        } else throw new NullPointerException("Card from bonusDeck while grabbing ammo is Optional.empty()");
    }

    /**
     * This functions manages the action of SHOOT
     * @param activePlayer the player who chose the action
     * @param view the interface that manages the shooting action
     */
    private void executeShoot(Player activePlayer, Interviewer view){
        Optional<WeaponTile> activeWeapon = activePlayer.getActiveWeapon();
        if (activeWeapon.isPresent()){
            WeaponTile weapon = activeWeapon.get();
            if (weapon.isLoaded()) {
                //weapon should shoot... if has targets
            }
        }
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
            //TODO: posso gestire power-Up qui e subito dopo il for
            //per ogni macroazione
            List<BasicAction> playedActions = new LinkedList<>();
            Optional<BasicAction> move;
            do {
                Set<BasicAction> availableActions = candidateBasicActions(playedActions, compoundActions)
                        .stream()
                        .filter(basicAction -> canDo(player, basicAction))
                        .collect(Collectors.toSet());
                move = availableActions.isEmpty() ?
                        Optional.empty() :
                        view.selectOptional("Which move would you like to execute?", availableActions, ClientApi.BASIC_ACTION_QUESTION);
                move.ifPresent(basicAction -> {
                    manageChosenAction(basicAction, player, view);
                    playedActions.add(basicAction);
                });
            } while (move.isPresent());
        }
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
                    List<BasicAction> actions = compoundAction.getActions();
                    for (
                            int i = 0;
                            i < actions.size() - playedActions.size() &&
                                    actions.get(i) == BasicAction.MOVE;
                            i++
                    ){
                        if (actions.subList(i, i+playedActions.size()).equals(playedActions))
                            return  Optional.of(new Tuple<>(compoundAction, i));
                    }
                    return Optional.empty();
                });
    }

    /**
     * This private method calculate the possible BasicActions the player can do
     * @param playedActions is the list of actions done in this turn by theplayers
     * @param compoundActions is the list of possible compound actions the player can do in that particular macro action.
     *                        They can variate from gaming mode to gaming mode or by the state of the player
     * @return a Set of possible basic actions the player can execute
     */
    private Set<BasicAction> candidateBasicActions(List<BasicAction> playedActions, List<CompoundAction> compoundActions){
        return candidateActions(playedActions, compoundActions)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(tuple -> {
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

    @Override
    public void onPlayerDamaged(PlayerDamaged e) {
        //Gestire powerups
    }
}