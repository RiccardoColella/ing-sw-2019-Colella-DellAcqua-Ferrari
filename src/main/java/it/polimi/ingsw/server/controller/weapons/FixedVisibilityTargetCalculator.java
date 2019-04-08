package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.player.Damageable;
import it.polimi.ingsw.server.model.battlefield.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a TargetCalculator that finds targets that are (or are not) visible from the starting point
 */
public class FixedVisibilityTargetCalculator implements TargetCalculator {
    /**
     * This property states whether the computed targets are the visible ones (true) or non visible ones (false)
     */
    private final boolean visible;

    /**
     * This constructor sets the type of visibility that will be considered
     * @param visible true if targets are the visible ones, false if targets are the non visible ones
     */
    public FixedVisibilityTargetCalculator(boolean visible) {
        this.visible = visible;
    }

    /**
     * This method returns the groups of Damageable that can be targeted by the Attack solely based on whether they can be seen from the starting point
     * @param startingPoint the Block relative to which the targets should be
     * @param type          the TargetType that should be considered
     * @return a list of the available groups of targets, which will be empty if none are available
     */
    @Override
    public List<List<Damageable>> computeTargets(Block startingPoint, Attack.TargetType type) {
        return new ArrayList<>();
    }
}
