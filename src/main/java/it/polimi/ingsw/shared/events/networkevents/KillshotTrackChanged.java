package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.utils.Tuple;

import java.util.List;

public class KillshotTrackChanged extends NetworkEvent {

    private final List<Tuple<PlayerColor, Boolean>> killshots;

    public KillshotTrackChanged(List<Tuple<PlayerColor, Boolean>> killshots) {
        this.killshots = killshots;
    }

    public List<Tuple<PlayerColor, Boolean>> getKillshots() {
        return killshots;
    }
}
