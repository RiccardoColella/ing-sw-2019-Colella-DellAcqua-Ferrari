package it.polimi.ingsw.server.controller.events;

import it.polimi.ingsw.server.controller.Controller;

import java.util.EventObject;

/**
 * Match ended class from the controller perspective
 *
 * @author Carlo Dell'Acqua
 */
public class MatchEnded extends EventObject {

    /**
     * Constructs a match ended object
     *
     * @param source the controller which is notifying the end
     */
    public MatchEnded(Controller source) {
        super(source);
    }

}
