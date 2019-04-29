package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.shared.Direction;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is a TargetCalculator that finds targets in a straight line from the starting point
 */
public class FixedDirectionTargetCalculator implements TargetCalculator {
    /**
     * This property saves the direction that shall be considered when computing the targets, if null the calculator is unset
     */
    private Direction direction;
    private final Board board;
    private final boolean goesThroughWalls;

    /**
     * This constructor does not set a specific direction
     */
    public FixedDirectionTargetCalculator(Board board, boolean goesThroughWalls) {
        this(board, goesThroughWalls,null);
    }

    /**
     * This constructor sets the direction that shall be considered when computing the target
     * @param direction the desired Direction
     */
    public FixedDirectionTargetCalculator(Board board, boolean goesThroughWalls, @Nullable Direction direction) {
        this.direction = direction;
        this.board = board;
        this.goesThroughWalls = goesThroughWalls;
    }

    /**
     * This method returns the groups of Damageable that can be targeted by the Attack solely based on their position in a straight direction from the starting point
     * @param startingPoint the Block relative to which the targets should be
     * @return a list of the available groups of targets, which will be empty if none are available
     */
    @Override
    public Set<Player> computeTargets(Block startingPoint, Weapon weapon) {
        updateDirection(startingPoint, weapon);

        Set<Block> blocks = new HashSet<>(Collections.singletonList(startingPoint));

        if (this.direction == null) {
            for (Direction dir : Direction.values()) {
                addBlocks(startingPoint, blocks, goesThroughWalls, dir);
            }
        } else {
            addBlocks(startingPoint, blocks, goesThroughWalls, this.direction);
        }

        return blocks
                .stream()
                .flatMap(block -> block.getPlayers().stream())
                .collect(Collectors.toSet());
    }

    private void addBlocks(Block startingPoint, Set<Block> blocks, boolean ignoreWalls, Direction direction) {
        Block neighbor = startingPoint;
        do {
            neighbor = !ignoreWalls && neighbor.getBorderType(direction) == Block.BorderType.WALL ?
                    null :
                    board.getBlockNeighbor(neighbor, direction).orElse(null);
            if (neighbor != null) {
                blocks.add(neighbor);
            }
        } while (neighbor  != null);
    }

    @Override
    public boolean contains(TargetCalculator calculator) {
        return calculator == this;
    }

    @Override
    public List<TargetCalculator> getSubCalculators() {
        return Collections.singletonList(this);
    }

    /**
     * This method sets the direction of the targets to a new value
     * @param direction the value of Direction that will be used when computing targets, or null to reset it
     */
    public void setDirection(@Nullable Direction direction) {
        this.direction = direction;
    }

    private void updateDirection(Block startingPoint, Weapon weapon) {
        boolean toSet = false;
        Attack attack = null;
        for (Attack a : weapon.getExecutedAttacks()) {
            for (ActionConfig c : a.getActionConfigs()) {
                toSet = c.getCalculator().map(calculator -> calculator.contains(this) && !weapon.wasHitBy(a).isEmpty()).orElse(false);
                if (toSet) {
                    attack = a;
                    break;
                }
            }
        }
        if (toSet) {
            Set<Player> hitByThat = weapon.wasHitBy(attack);
            Player previous = hitByThat.iterator().next();
            if (previous.getBlock().getRow() - startingPoint.getRow() == 0) {
                if (previous.getBlock().getColumn() > startingPoint.getColumn()) {
                    this.direction = Direction.EAST;
                } else {
                    this.direction = Direction.WEST;
                }
            } else {
                if (previous.getBlock().getRow() > startingPoint.getRow()) {
                    this.direction = Direction.SOUTH;
                } else {
                    this.direction = Direction.NORTH;
                }
            }
        }
    }
}
