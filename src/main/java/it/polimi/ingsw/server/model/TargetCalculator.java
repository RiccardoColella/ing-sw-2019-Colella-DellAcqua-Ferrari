package it.polimi.ingsw.server.model;

import java.util.List;

public interface TargetCalculator {

    /**
     * This method returns the groups of Damageable that can be targeted by the Attack solely based on their position
     * @return a list of the available groups of targets, which will be empty if none are available
     */
    List<List<Damageable>> computeTargets();

}
