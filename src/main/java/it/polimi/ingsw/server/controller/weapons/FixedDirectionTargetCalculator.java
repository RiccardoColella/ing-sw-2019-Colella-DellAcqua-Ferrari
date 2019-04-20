package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.server.model.player.Player;

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
    public Set<Player> computeTargets(Block startingPoint) {
        if (this.direction == null) {
            throw new IllegalStateException("Calculator is unset");
        }
        List<Block> possibleBlocks = new LinkedList<>();
        List<Block> bothDirections;
        switch (this.direction) {
            case NORTH:
                bothDirections = board.getColumn(startingPoint);
                possibleBlocks.addAll(bothDirections.subList(0, bothDirections.indexOf(startingPoint) + 1));
                break;
            case EAST:
                bothDirections = board.getRow(startingPoint);
                possibleBlocks.addAll(bothDirections.subList(bothDirections.indexOf(startingPoint), bothDirections.size()));
                break;
            case SOUTH:
                bothDirections = board.getColumn(startingPoint);
                possibleBlocks.addAll(bothDirections.subList(bothDirections.indexOf(startingPoint), bothDirections.size()));
                break;
            case WEST:
                bothDirections = board.getRow(startingPoint);
                possibleBlocks.addAll(bothDirections.subList(0, bothDirections.indexOf(startingPoint) + 1));
                break;
        }
        if (!goesThroughWalls) {
            stopAtWalls(this.direction, possibleBlocks);
        }
        possibleBlocks.removeIf(block -> block.getPlayers().isEmpty());
        return possibleBlocks.stream().flatMap(block -> block.getPlayers().stream()).collect(Collectors.toSet());
    }

    /**
     * This method sets the direction of the targets to a new value
     * @param direction the value of Direction that will be used when computing targets, or null to reset it
     */
    public void setDirection(@Nullable Direction direction) {
        this.direction = direction;
    }

    private void stopAtWalls(Direction direction, List<Block> possibleBlocks) {
        boolean wallFound = false;
        List<Block> toRemove = new LinkedList<>();
        if (this.direction == Direction.NORTH || this.direction == Direction.EAST) {
            Collections.reverse(possibleBlocks);
        }
        for (Block block : possibleBlocks) {
            if (wallFound) {
                toRemove.add(block);
            } else if (block.getBorderType(direction) == Block.BorderType.WALL) {
                wallFound = true;
            }
        }
        possibleBlocks.removeAll(toRemove);
    }
}
