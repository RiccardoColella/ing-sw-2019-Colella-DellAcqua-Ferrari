package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.match.Match;

import java.util.EventObject;

public class MatchEvent extends EventObject {
    public MatchEvent(Match match) {
        super(match);
    }

    public Match getMatch() {
        return (Match)source;
    }
}
