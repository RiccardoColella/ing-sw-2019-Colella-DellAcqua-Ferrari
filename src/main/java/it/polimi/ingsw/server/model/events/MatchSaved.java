package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.Match;

import java.util.EventObject;

public class MatchSaved extends EventObject {
    /**
     *
     *
     * @param match the source object
     */
    public MatchSaved(Match match) {
        super(match);
    }
}
