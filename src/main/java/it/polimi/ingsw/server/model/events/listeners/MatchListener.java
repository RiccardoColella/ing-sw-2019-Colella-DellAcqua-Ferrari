package it.polimi.ingsw.server.model.events.listeners;

import it.polimi.ingsw.server.model.events.MatchEnded;
import it.polimi.ingsw.server.model.events.MatchModeChanged;
import it.polimi.ingsw.server.model.events.MatchStarted;

import java.util.EventListener;

public interface MatchListener extends EventListener {


    void onMatchEnded(MatchEnded event);

    void onMatchModeChanged(MatchModeChanged event);

    void onMatchStarted(MatchStarted event);
}
