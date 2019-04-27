package it.polimi.ingsw.server.controller;

import it.polimi.ingsw.server.model.battlefield.SpawnpointBlock;
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
import it.polimi.ingsw.server.model.weapons.Weapon;
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

    private void manageActivePlayerTurn(Player activePlayer, Interviewer view) {
        logger.info("Managing actions...");
        manageAction(activePlayer, view);
        logger.info("No more actions to be managed");
        logger.info("Ending turn. Checking for died players...");
        for (Player player : match.endTurn()) {
            logger.info("Player " + player.getColor() + " died.");
            //Prendi un powerUp dal deck
            //prendi i powerUp del giocatore
            //Offrirgli la possibilità di scartarne 1 (se la dim > 1 )
            //con il powerup scartato ditermina il punto di resiscitazioine.
            //Prendo il player, lo metto sullo spawnpoint (teleport) e chiamo BringBacjToLife
            player.bringBackToLife();
        }
        match.changeTurn();
    }

    private void manageChosenAction(BasicAction basicActionChosen, Player activePlayer, Interviewer view){

        switch (basicActionChosen){
            case GRAB:
                if (activePlayer.isOnASpawnpoint()){
                    //choose weapon to be picked up
                    SpawnpointBlock block =  (SpawnpointBlock) activePlayer.getBlock();
                    List<Weapon> availableWeapons = new LinkedList<>(block.getWeapons());
                    List<Weapon> affordableWeapons = availableWeapons.stream().filter(weapon -> {
                        Treasury bill = new Treasury(weapon.getAcquisitionCost(), activePlayer);
                        return bill.canAfford();
                    }).collect(Collectors.toList());
                    Weapon weapon = view.select("Which weapon would you like to grab?",
                            affordableWeapons, ClientApi.WEAPON_CHOICE_QUESTION);
                    //pick up
                    logger.info("picking up weapon " + weapon.getName() + "...");
                    pickUpWeapon(weapon, activePlayer, view);
                } else {
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
                break;
            case MOVE:
                activePlayer.move(view.select("Which direction do you want to move?", activePlayer.getAvailableDirections(), ClientApi.DIRECTION_QUESTION));
                break;
            case SHOOT:
                break;
            case RELOAD:
                break;
        }
    }

    private void pickUpWeapon(Weapon weapon, Player activePlayer, Interviewer view){
        Treasury bill = new Treasury(weapon.getAcquisitionCost(), activePlayer);
        List<Coin> paymentMethod = bill.selectPaymentMethod(view);
        if (bill.canAfford()){
            try {
                activePlayer.grabWeapon(weapon, paymentMethod);
            } catch (UnauthorizedExchangeException e){
                Weapon weaponToDiscard = view.select("Which weapon do you want to discard?",
                        activePlayer.getWeapons(), ClientApi.WEAPON_CHOICE_QUESTION);
                activePlayer.grabWeapon(weapon, paymentMethod, weaponToDiscard);
            }
        }
    }

    private boolean canDo(Player player, BasicAction action){
        boolean returnValue = true;
        switch (action){
            case MOVE:
                break;
            case GRAB:
                if (player.isOnASpawnpoint()) {
                    SpawnpointBlock block = (SpawnpointBlock) player.getBlock();
                    List<Weapon> availableWeapons = new LinkedList<>(block.getWeapons());
                    List<Weapon> affordableWeapons = availableWeapons.stream().filter(weapon -> {
                        Treasury bill = new Treasury(weapon.getAcquisitionCost(), player);
                        return bill.canAfford();
                    }).collect(Collectors.toList());
                    returnValue = !affordableWeapons.isEmpty();
                } else returnValue = true;
                break;
            case SHOOT:
                Optional<Weapon> activeWeapon = player.getActiveWeapon();
                if (!activeWeapon.isPresent() || !activeWeapon.get().isLoaded()){
                    returnValue = false;
                }
                break;
            case RELOAD:
                break;
        }
        return returnValue;
    }

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

    public Set<BasicAction> candidateBasicActions(List<BasicAction> playedActions, List<CompoundAction> compoundActions){
        Stream<Optional<Tuple<CompoundAction, Integer>>> candidateActions = compoundActions
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
        return candidateActions
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
