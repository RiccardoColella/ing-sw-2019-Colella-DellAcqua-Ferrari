package it.polimi.ingsw.server.controller.events.listeners;

import it.polimi.ingsw.server.controller.events.MatchEnded;

import java.util.EventListener;

public interface ControllerListener extends EventListener {
    void onMatchEnd(MatchEnded e);
}
