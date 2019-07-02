package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.match.Match;

import java.util.EventObject;

/**
 * A generic match event
 */
public class MatchEvent extends EventObject {
    /**
     * Constructs a generic match event
     *
     * @param match the match to which this event is related
     */
    public MatchEvent(Match match) {
        super(match);
    }

    /**
     * @return the match to which this event is related
     */
    public Match getMatch() {
        return (Match)source;
    }
}
