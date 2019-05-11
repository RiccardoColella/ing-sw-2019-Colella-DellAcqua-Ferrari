package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.Player;

import java.util.EventObject;
import java.util.List;
import java.util.Map;

public class MatchEnded extends EventObject {
    private final Map<Integer, List<Player>> rankings;

    /**
     *
     * @param match the source object
     * @param rankings the ordered list of player ordered by score descending
     */
    public MatchEnded(Match match, Map<Integer, List<Player>> rankings) {
        super(match);
        this.rankings = rankings;
    }

    public List<Player> getWinner() {
        return rankings.get(1);
    }

    public Map<Integer, List<Player>> getRankings() {
        return rankings;
    }
}
