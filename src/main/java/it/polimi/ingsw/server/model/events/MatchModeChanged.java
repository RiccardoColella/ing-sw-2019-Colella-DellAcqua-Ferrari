package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.match.Match;

/**
 * Event fired when the match mode changes
 */
public class MatchModeChanged extends MatchEvent {

    /**
     * The new match mode
     */
    private final Match.Mode mode;

    /**
     * Constructs a match mode changed event
     *
     * @param match the source object
     * @param mode the new match mode
     */
    public MatchModeChanged(Match match, Match.Mode mode) {
        super(match);
        this.mode = mode;
    }

    /**
     * @return the new match mode
     */
    public Match.Mode getMode() {
        return mode;
    }
}
