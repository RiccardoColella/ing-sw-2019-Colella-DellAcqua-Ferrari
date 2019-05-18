package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.shared.datatransferobjects.Player;

import java.util.List;
import java.util.Map;

public class MatchEnded extends NetworkEvent {


    private final Map<Integer, List<Player>> rankings;

    public MatchEnded(Map<Integer, List<Player>> rankings) {
        this.rankings = rankings;
    }

    public Map<Integer, List<Player>> getRankings() {
        return rankings;
    }
}
