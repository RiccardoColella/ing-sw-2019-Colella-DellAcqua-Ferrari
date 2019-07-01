package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.utils.Tuple;

import java.util.List;

/**
 * Network event carrying information about the killshot track
 *
 * @author Carlo Dell'Acqua
 */
public class KillshotTrackChanged extends NetworkEvent {

    /**
     * Killshot track representation
     */
    private final List<Tuple<PlayerColor, Boolean>> killshots;

    /**
     * Constructs a killshot track event
     *
     * @param killshots the killshot track representation
     */
    public KillshotTrackChanged(List<Tuple<PlayerColor, Boolean>> killshots) {
        this.killshots = killshots;
    }

    /**
     * @return the killshot track representation
     */
    public List<Tuple<PlayerColor, Boolean>> getKillshots() {
        return killshots;
    }
}
