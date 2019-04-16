package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.player.Damageable;
import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.Direction;
import it.polimi.ingsw.server.model.player.Player;

import javax.annotation.Nullable;
import java.util.*;

/**
 * This class is a TargetCalculator that finds targets in a straight line from the starting point
 */
public class FixedDirectionTargetCalculator implements TargetCalculator {
    /**
     * This property saves the direction that shall be considered when computing the targets, if null, any straight direction is fine
     */
    private Direction direction;
    private Board board;

    /**
     * This constructor does not set a specific direction
     */
    public FixedDirectionTargetCalculator(Board board) {
        this(board,null);
    }

    /**
     * This constructor sets the direction that shall be considered when computing the target
     * @param direction the desired Direction
     */
    public FixedDirectionTargetCalculator(Board board, @Nullable Direction direction) {
        this.direction = direction;
        this.board = board;
    }

    /**
     * This method returns the groups of Damageable that can be targeted by the Attack solely based on their position in a straight direction from the starting point
     * @param startingPoint the Block relative to which the targets should be
     * @return a list of the available groups of targets, which will be empty if none are available
     */
    @Override
    public Set<Block> computeTargets(Block startingPoint) {
        Set<Block> possibleBlocks = new HashSet<>();
        if (this.direction == null) {
            possibleBlocks.addAll(board.getRow(startingPoint));
            possibleBlocks.addAll(board.getColumn(startingPoint));
        } else {
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
        }
        possibleBlocks.removeIf(block -> block.getPlayers().isEmpty());
        return possibleBlocks;
    }

    /**
     * This method sets the direction of the targets to a new value
     * @param direction the value of Direction that will be used when computing targets, or null if all directions should be used
     */
    public void setDirection(@Nullable Direction direction) {
        this.direction = direction;
    }
}
