package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.shared.datatransferobjects.Player;

import java.util.List;
import java.util.Map;

public class MatchEnded extends NetworkEvent {


    private final Map<Integer, List<Player>> rankings;
    private final Map<String, Integer> scores;

    public MatchEnded(Map<Integer, List<Player>> rankings, Map<String, Integer> scores) {
        this.scores = scores;
        this.rankings = rankings;
    }

    public int getScore(String playerName) {
        return scores.get(playerName);
    }

    public Map<Integer, List<Player>> getRankings() {
        return rankings;
    }

    public boolean isTheWinner(Player p) {
        return rankings.get(1).stream().anyMatch(w -> p.getNickname().equals(w.getNickname()));
    }
}
