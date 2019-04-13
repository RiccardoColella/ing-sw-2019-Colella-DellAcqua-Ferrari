package it.polimi.ingsw.server.controller.events;

import it.polimi.ingsw.server.controller.Controller;

import java.util.EventObject;

/**
 * Match ended class from the controller perspective
 */
public class MatchEnded extends EventObject {

    public MatchEnded(Controller source) {
        super(source);
    }

    public Controller getSource() {
        return this.getSource();
    }
}
