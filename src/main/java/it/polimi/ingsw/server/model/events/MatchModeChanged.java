package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.match.Match;

import java.util.EventObject;

public class MatchModeChanged extends MatchEvent {

    private final Match.Mode mode;

    /**
     *
     * @param match the source object
     * @param mode the new match mode
     */
    public MatchModeChanged(Match match, Match.Mode mode) {
        super(match);
        this.mode = mode;
    }

    public Match.Mode getMode() {
        return mode;
    }
}
