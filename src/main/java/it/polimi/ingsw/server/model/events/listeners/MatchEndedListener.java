package it.polimi.ingsw.server.model.events.listeners;

import it.polimi.ingsw.server.model.events.MatchEnded;

import java.util.EventListener;

public interface MatchEndedListener extends EventListener {

    void onMatchEnded(MatchEnded event);
}
