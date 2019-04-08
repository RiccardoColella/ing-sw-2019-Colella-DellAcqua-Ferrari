package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.Damageable;
import it.polimi.ingsw.server.model.battlefield.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a TargetCalculator that finds targets that is in a fixed range of moves away from the starting point
 */
public class FixedDistanceTargetCalculator implements TargetCalculator {

    /**
     * This property represents the range of moves away the target can be from the starting point
     */
    private final Range range;

    /**
     * This constructor sets the range of moves that will be used during computations
     * @param range the range of moves away that will be considered for potential targets
     */
    public FixedDistanceTargetCalculator(Range range) {
        this.range = range;
    }

    /**
     * This method returns the groups of Damageable that can be targeted by the Attack solely based on how many moves away they are from the starting point
     * @param startingPoint the Block relative to which the targets should be
     * @param type          the TargetType that should be considered
     * @return a list of the available groups of targets, which will be empty if none are available
     */
    @Override
    public List<List<Damageable>> computeTargets(Block startingPoint, Attack.TargetType type) {
        return new ArrayList<>();
    }
}
