package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.match.Killshot;
import it.polimi.ingsw.server.model.match.Match;

import java.util.List;

/**
 * Event fired when the killshot track changes
 */
public class KillshotTrackChanged extends MatchEvent {

    /**
     * The updated killshot track
     */
    private final List<Killshot> killshots;

    /**
     * Constructs a killshot track changed event
     *
     * @param source the match which contains the killshot track
     * @param killshots the updated killshot track
     */
    public KillshotTrackChanged(Match source, List<Killshot> killshots) {
        super(source);
        this.killshots = killshots;
    }

    /**
     * @return the updated killshot track
     */
    public List<Killshot> getKillshots() {
        return killshots;
    }
}
