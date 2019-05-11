package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.match.Killshot;
import it.polimi.ingsw.server.model.match.Match;

import java.util.List;

public class KillshotTrackChanged extends MatchEvent {

    private final List<Killshot> killshots;

    public KillshotTrackChanged(Match source, List<Killshot> killshots) {
        super(source);
        this.killshots = killshots;
    }

    public List<Killshot> getKillshots() {
        return killshots;
    }
}
