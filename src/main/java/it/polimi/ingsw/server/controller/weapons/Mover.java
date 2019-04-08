package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.controller.weapons.Range;

/**
 * This interface is implemented by the attacks that can move the player itself or one of the targets
 */
public interface Mover {
    /**
     * This method returns the Range of moves required by the Attack
     * @return the Range of moves that should be made
     */
    Range getStepRange();

}
