package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.player.Player;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public Set<Player> computeTargets(Block startingPoint, Weapon weapon) {
        Set<Block> candidates = board.getReachableBlocks(startingPoint, range);
        candidates.removeIf(block -> block.getPlayers().isEmpty());
        return candidates.stream().flatMap(block -> block.getPlayers().stream()).collect(Collectors.toSet());
    }

    @Override
    public boolean contains(TargetCalculator calculator) {
        return calculator == this;
    }

    @Override
    public List<TargetCalculator> getSubCalculators() {
        return Collections.singletonList(this);
    }

}
