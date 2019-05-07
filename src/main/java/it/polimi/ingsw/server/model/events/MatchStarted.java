package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.match.Match;

import java.util.EventObject;

public class MatchStarted extends EventObject {
    private Match match;
    /**
     * Constructs a prototypical Event.
     *
     * @param match the source object
     */
    public MatchStarted(Match match) {
        super(match);
        this.match = match;
    }


    public Match getMatch() {
        return match;
    }
}
