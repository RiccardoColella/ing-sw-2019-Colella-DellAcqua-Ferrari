package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.player.Damageable;
import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.Direction;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a TargetCalculator that finds targets in a straight line from the starting point
 */
public class FixedDirectionTargetCalculator implements TargetCalculator {
    /**
     * This property saves the direction that shall be considered when computing the targets, if null, any straight direction is fine
     */
    private Direction direction;

    /**
     * This constructor does not set a specific direction
     */
    public FixedDirectionTargetCalculator() {
        this.direction = null;
    }

    /**
     * This constructor sets the direction that shall be considered when computing the target
     * @param direction the desired Direction
     */
    public FixedDirectionTargetCalculator(@Nullable Direction direction) {
        this.direction = direction;
    }

    /**
     * This method returns the groups of Damageable that can be targeted by the Attack solely based on their position in a straight direction from the starting point
     * @param startingPoint the Block relative to which the targets should be
     * @param type          the TargetType that should be considered
     * @return a list of the available groups of targets, which will be empty if none are available
     */
    @Override
    public List<List<Damageable>> computeTargets(Block startingPoint, Attack.TargetType type) {
        return new ArrayList<>();
    }

    /**
     * This method sets the direction of the targets to a new value
     * @param direction the value of Direction that will be used when computing targets, or null if all directions should be used
     */
    public void setDirection(@Nullable Direction direction) {
        this.direction = direction;
    }
}
