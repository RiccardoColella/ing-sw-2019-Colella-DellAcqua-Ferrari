package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.Player;

import java.util.EventObject;
import java.util.List;
import java.util.Map;

/**
 * Event fired on match ended
 */
public class MatchEnded extends EventObject {
    /**
     * The ranking of each player
     */
    private final Map<Integer, List<Player>> rankings;

    /**
     * Constructs a match ended event
     *
     * @param match the source object
     * @param rankings the ordered list of player ordered by score descending
     */
    public MatchEnded(Match match, Map<Integer, List<Player>> rankings) {
        super(match);
        this.rankings = rankings;
    }

    /**
     * @return the winner of the match
     */
    public List<Player> getWinner() {
        return rankings.get(1);
    }

    /**
     * @return the rankings of all players
     */
    public Map<Integer, List<Player>> getRankings() {
        return rankings;
    }
}
