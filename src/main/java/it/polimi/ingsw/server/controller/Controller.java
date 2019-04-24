package it.polimi.ingsw.server.controller;

import it.polimi.ingsw.server.model.battlefield.SpawnpointBlock;
import it.polimi.ingsw.server.model.currency.BonusTile;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.currency.PowerupTile;
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

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class has the purpose of managing the game flow
 */
public class Controller implements Runnable {

    private Logger logger = Logger.getLogger(Controller.class.getName());
    private Match match;
    private List<View> views;
    private List<Player> players;

    public Controller(Match match, List<View> views) {

        if (views.size() != match.getPlayers().size()) {
            throw new IllegalArgumentException("SocketView number does not match player number");
        }

        this.match = match;
        this.views = views;
        this.players = match.getPlayers();
    }

    @Override
    public void run() {
        Player activePlayer;
        for (Player player : players) {
            List<PowerupTile> powerups = Arrays.asList(match.getPowerupDeck().pickUnsafe(), match.getPowerupDeck().pickUnsafe());
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
        List<BasicAction> executedActions = new LinkedList<>();
        Set<BasicAction> possibleBasicActions = getPossibleActions(activePlayer, executedActions);
        while (!possibleBasicActions.isEmpty()){
            logger.info("Choosing action");
            possibleBasicActions = getPossibleActions(activePlayer, executedActions);
            BasicAction chosenAction = view.select("Select action: ", possibleBasicActions, ClientApi.BASIC_ACTION_QUESTION);
            manageChosenAction(chosenAction, activePlayer, view);
        }
        logger.info("No more actions available. Changing turn");

        match.endTurn();
        match.changeTurn();
    }

    private void manageChosenAction(BasicAction basicActionChosen, Player activePlayer, Interviewer view){

        switch (basicActionChosen){
            case GRAB:
                if (activePlayer.isOnASpawnpoint()){
                    //choose weapon to be picked up
                    SpawnpointBlock block =  (SpawnpointBlock) activePlayer.getBlock();
                    List<Weapon> affordableWeapons = new LinkedList<>(block.getWeapons());
                    affordableWeapons = affordableWeapons.stream().filter(weapon -> {
                        Treasury bill = new Treasury(weapon.getAcquisitionCost(), activePlayer, view);
                        return bill.canAfford();
                    }).collect(Collectors.toList());
                    Weapon weapon = view.select("Which weapon you'd like to grab?",
                            affordableWeapons, ClientApi.WEAPON_CHOICE_QUESTION);
                    //pick up
                    pickUpWeapon(weapon, activePlayer, view);
                } else {
                    logger.info("Grabbing some ammos...");
                    Optional<BonusTile> optionalCard = match.getBonusDeck().pick();
                    if (optionalCard.isPresent()){
                        activePlayer.grabAmmoCubes(optionalCard.get().getRewards());
                        if (optionalCard.get().canPickPowerup()){
                            logger.info("Grabbing power-up too...");
                            match.getPowerupDeck().pick().ifPresent(activePlayer::grabPowerup);
                        }
                    } else throw new NullPointerException("Card from bonusDeck is Optional.empty()");
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
        Treasury bill = new Treasury(weapon.getAcquisitionCost(), activePlayer, view);
        List<Coin> paymentMethod = bill.selectPaymentMethod();
        if (bill.canAfford()){
            try {
                activePlayer.grabWeapon(weapon, paymentMethod);
            } catch (UnauthorizedExchangeException e){
                Weapon weaponToDiscarde = view.select("Which weapon you want to discarde?",
                        activePlayer.getWeapons(), ClientApi.WEAPON_CHOICE_QUESTION);
                activePlayer.grabWeapon(weapon, paymentMethod, weaponToDiscarde);
            }
        }
    }

    private Set<BasicAction> getPossibleActions(Player player, List<BasicAction> previousActions){
        //TODO: everything
        ActionTile actionTile = player.getAvailableMacroActions();
        Set<BasicAction> possibleBasicActions = new HashSet<>();
        for (CompoundAction compoundAction : actionTile.getCompoundActions().get(0)){
            possibleBasicActions.add(compoundAction.getActions().get(0));
        }

        return possibleBasicActions;
    }
}
