package it.polimi.ingsw.server.model;

public interface Mover {

    /**
     * This method returns the Range of moves required by the Attack
     * @return the Range of moves that should be made
     */
    Range getStepRange();

}
