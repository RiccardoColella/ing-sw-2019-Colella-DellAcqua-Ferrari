package it.polimi.ingsw.server.controller;

import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.view.View;

import java.util.Arrays;
import java.util.List;

/**
 * This class has the purpose of managing the game flow
 */
public class Controller implements Runnable {

    private Match match;
    private List<View> views;

    public Controller(Match match, List<View> views) {

        if (views.size() != match.getPlayers().size()) {
            throw new IllegalArgumentException("SocketView number does not match player number");
        }

        this.match = match;
        this.views = views;
    }

    @Override
    public void run() {
        for (int i = 0; i < match.getPlayers().size(); i++) {
            List<PowerupTile> powerups = Arrays.asList(match.getPowerupDeck().pickUnsafe(), match.getPowerupDeck().pickUnsafe());
            PowerupTile discardedPowerup = views.get(i).chooseSpawnpoint(powerups);
            match.getBoard().getSpawnpoint(discardedPowerup.getColor()).addPlayer(match.getActivePlayer());
            manageActivePlayerTurn();
        }

        while (!match.isEnded()) {
            manageActivePlayerTurn();
        }
    }

    public void manageActivePlayerTurn() {
        // TODO: everything
        match.endTurn();
        match.changeTurn();
    }
}
