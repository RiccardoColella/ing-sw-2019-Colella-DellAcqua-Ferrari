package it.polimi.ingsw.server.model.events.listeners;

import it.polimi.ingsw.server.model.events.MatchEnded;

public interface MatchEndedListener {

    void onMatchEnded(MatchEnded event);
}
