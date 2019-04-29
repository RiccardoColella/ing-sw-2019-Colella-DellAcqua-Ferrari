package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.player.Player;

import java.util.List;
import java.util.Set;

/**
 * This class uses other classes implementing TargetCalculator in order to cover more complex target position requirements
 */
public class CompoundTargetCalculator implements TargetCalculator {

    /**
     * This property stores a map of the target calculators used and the types of targets for which they are needed
     */
    private List<TargetCalculator> calculators;

    /**
     * This constructors builds a CompoundTargetCalculator given the target calculators it needs
     * @param calculators the necessary target calculators
     */
    public CompoundTargetCalculator(List<TargetCalculator> calculators) {
        this.calculators = calculators;
    }

    /**
     * This method returns the groups of Damageable that can be targeted by the Attack solely based on their position relative to the starting point
     * @param startingPoint the Block relative to which the targets should be
     * @return a list of the available groups of targets, which will be empty if none are available
     */
    @Override
    public Set<Player> computeTargets(Block startingPoint, Weapon weapon) {
        Set<Player> potentialBlocks = calculators.get(0).computeTargets(startingPoint, weapon);
        calculators
            .stream()
            .map(targetCalculator -> targetCalculator.computeTargets(startingPoint, weapon))
            .forEach(potentialBlocks::retainAll);
        return potentialBlocks;
    }

    @Override
    public boolean contains(TargetCalculator calculator) {
        return calculator == this || this.calculators.stream().anyMatch(c -> c.contains(calculator));
    }

    @Override
    public List<TargetCalculator> getSubCalculators() {
        return calculators;
    }
}
