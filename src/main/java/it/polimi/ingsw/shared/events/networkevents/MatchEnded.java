package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.shared.datatransferobjects.Player;

import java.util.List;
import java.util.Map;

/**
 * Network event carrying information about a the match ended status
 *
 * @author Carlo Dell'Acqua
 */
public class MatchEnded extends NetworkEvent {

    /**
     * The final ranking
     */
    private final Map<Integer, List<Player>> rankings;
    /**
     * The final scores
     */
    private final Map<String, Integer> scores;

    /**
     * Constructs the match ended event
     *
     * @param rankings the final ranking
     * @param scores the final scores
     */
    public MatchEnded(Map<Integer, List<Player>> rankings, Map<String, Integer> scores) {
        this.scores = scores;
        this.rankings = rankings;
    }

    /**
     * @param playerName the player the caller wants to know the score
     * @return the final score of the passed player
     */
    public int getScore(String playerName) {
        return scores.get(playerName);
    }

    /**
     * @return the final ranking
     */
    public Map<Integer, List<Player>> getRankings() {
        return rankings;
    }

    /**
     * @param p the player to check
     * @return true if the player is the winner of the match
     */
    public boolean isTheWinner(Player p) {
        return rankings.get(1).stream().anyMatch(w -> p.getNickname().equals(w.getNickname()));
    }
}
