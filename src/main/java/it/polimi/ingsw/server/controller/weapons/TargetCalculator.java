package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.player.Player;

import java.util.List;
import java.util.Set;

/**
 * This interface schematizes all the possible ways to calculate which damageable entities are actual targets
 *
 * @author Adriana Ferrari
 */
public interface TargetCalculator {

    /**
     * This method returns the groups of {@code Player} that can be targeted by the Attack solely based on their position relative to the starting point
     *
     * @param startingPoint the {@code Block} relative to which the targets should be
     * @param weapon the {@code Weapon} that will be used to shoot
     * @return a list of the available groups of targets, which will be empty if none are available
     */
    Set<Player> computeTargets(Block startingPoint, Weapon weapon);

    /**
     * Determines whether another {@code TargetCalculator} is contained in this calculator
     *
     * @param calculator the {@code TargetCalculator} that might be contained in this
     * @return {@code true} if this calculator contains the given calculator, false otherwise
     */
    boolean contains(TargetCalculator calculator);

    /**
     * Gets the list of calculators contained in this calculator
     *
     * @return a list of {@code TargetCalculator}
     */
    List<TargetCalculator> getSubCalculators();
}
