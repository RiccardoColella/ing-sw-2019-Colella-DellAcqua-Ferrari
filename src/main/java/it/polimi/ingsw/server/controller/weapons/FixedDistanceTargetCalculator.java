package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.battlefield.Direction;
import it.polimi.ingsw.server.model.player.Damageable;
import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.player.Player;

import java.util.*;

import static it.polimi.ingsw.server.model.battlefield.Block.BorderType.WALL;

/**
 * This class is a TargetCalculator that finds targets that is in a fixed range of moves away from the starting point
 */
public class FixedDistanceTargetCalculator implements TargetCalculator {

    /**
     * This property represents the range of moves away the target can be from the starting point
     */
    private final Range range;
    private final Board board;
    /**
     * This constructor sets the range of moves that will be used during computations
     * @param range the range of moves away that will be considered for potential targets
     */
    public FixedDistanceTargetCalculator(Board board, Range range) {
        this.range = range;
        this.board = board;
    }

    /**
     * This method returns the groups of Damageable that can be targeted by the Attack solely based on how many moves away they are from the starting point
     * @param startingPoint the Block relative to which the targets should be
     * @return a list of the available groups of targets, which will be empty if none are available
     */
    @Override
    public Set<Block> computeTargets(Block startingPoint) {
        Set<Block> toCheck = new HashSet<>();
        Set<Block> alreadyChecked = new HashSet<>();
        toCheck.add(startingPoint);
        //getting all the blocks at min distance from starting point, step by step
        for (int i = 0; i < range.getMin(); i++) {
            checkNeighbors(alreadyChecked, toCheck);
        }
        //at this point, toCheck contains all the blocks at the min acceptable distance from startingPoint
        Set<Block> candidates = new HashSet<>(toCheck);
        //if min and max are different, all blocks with a distance smaller than max but greater than min will be added
        for (int distance = range.getMin(); distance < range.getMax(); distance++) {
            checkNeighbors(alreadyChecked, toCheck);
            candidates.addAll(toCheck);
        }
        candidates.removeIf(block -> block.getPlayers().isEmpty());
        return candidates;
    }

    /**
     * This method modifies the two input sets, adding new blocks to the set that needs to be checked and moving the checked blocks to alreadyChecked
     * @param alreadyChecked blocks that have previously been checked
     * @param toCheck the blocks that will be checked
     */
    private void checkNeighbors(Set<Block> alreadyChecked, Set<Block> toCheck) {
        List<Block> neighbors = new LinkedList<>();
        toCheck.forEach(block -> {
            for (Direction dir : Direction.values()) {
                if (block.getBoarderType(dir) != WALL) {
                    board.getBlockNeighbor(block, dir).ifPresent(neighbors::add);
                }
            }
            alreadyChecked.add(block);
        });
        toCheck.clear();
        neighbors.forEach(n -> {
            if (!alreadyChecked.contains(n)) {
                toCheck.add(n);
            }
        });
    }
}
