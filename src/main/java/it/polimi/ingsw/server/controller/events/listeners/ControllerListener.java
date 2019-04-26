package it.polimi.ingsw.server.controller.events.listeners;

import it.polimi.ingsw.server.controller.events.MatchEnded;

import java.util.EventListener;

/**
 * This interface represents a listener for the main Controller
 *
 * @author Carlo Dell'Acqua
 */
public interface ControllerListener extends EventListener {
    void onMatchEnd(MatchEnded e);
}
