package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.Player;

import java.util.List;

public class MatchEnded {
    private List<Player> rankings;

    public MatchEnded(List<Player> rankings) {
        this.rankings = rankings;
    }

    public Player getWinner() {
        return rankings.get(0);
    }

    public List<Player> getRankings() {
        return rankings;
    }
}
