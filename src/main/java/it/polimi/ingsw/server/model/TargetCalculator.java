package it.polimi.ingsw.server.model;

import java.util.List;

/**
 * This interface schematizes all the possible ways to calculate which damageable entities are actual targets
 */
public interface TargetCalculator {

    /**
     * This method returns the groups of Damageable that can be targeted by the Attack solely based on their position relative to the starting point
     * @param startingPoint the Block relative to which the targets should be
     * @param type the TargetType that should be considered
     * @return a list of the available groups of targets, which will be empty if none are available
     */
    List<List<Damageable>> computeTargets(Block startingPoint, TargetType type);

}
