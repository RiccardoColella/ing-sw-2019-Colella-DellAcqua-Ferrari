package it.polimi.ingsw.server.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class uses other classes implementing TargetCalculator in order to cover more complex target position requirements
 */
public class CompoundTargetCalculator implements TargetCalculator {

    /**
     * This property stores a map of the target calculators used and the types of targets for which they are needed
     */
    private Map<TargetCalculator, TargetType> calculators;

    /**
     * This constructors builds a CompoundTargetCalculator given the target calculators it needs
     * @param calculators a Map of the necessary target calculators and the targets for which they should be used
     */
    public CompoundTargetCalculator(Map<TargetCalculator, TargetType> calculators) {
        this.calculators = calculators;
    }

    /**
     * This method returns the groups of Damageable that can be targeted by the Attack solely based on their position relative to the starting point
     * @param startingPoint the Block relative to which the targets should be
     * @param type          the TargetType that should be considered
     * @return a list of the available groups of targets, which will be empty if none are available
     */
    @Override
    public List<List<Damageable>> computeTargets(Block startingPoint, TargetType type) {
        return new ArrayList<>();
    }
}
