package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.shared.viewmodels.Player;

public class MatchModeChanged extends NetworkEvent {


    private final Match.Mode mode;

    public MatchModeChanged(Match.Mode mode) {
        this.mode = mode;
    }

    public Match.Mode getMode() {
        return mode;
    }
}
