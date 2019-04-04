package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.Match;
import it.polimi.ingsw.server.model.Player;

import java.util.EventObject;
import java.util.List;

public class MatchEnded extends EventObject {
    private List<Player> rankings;

    /**
     *
     * @param match the source object
     * @param rankings the ordered list of player ordered by score descending
     */
    public MatchEnded(Match match, List<Player> rankings) {
        super(match);
        this.rankings = rankings;
    }

    public Player getWinner() {
        return rankings.get(0);
    }

    public List<Player> getRankings() {
        return rankings;
    }
}
