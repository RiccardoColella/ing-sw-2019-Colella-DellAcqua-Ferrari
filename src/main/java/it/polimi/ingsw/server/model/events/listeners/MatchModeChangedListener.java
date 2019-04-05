package it.polimi.ingsw.server.model.events.listeners;

import it.polimi.ingsw.server.model.events.MatchModeChanged;

import java.util.EventListener;

public interface MatchModeChangedListener extends EventListener {
    void onMatchModeChanged(MatchModeChanged event);
}
