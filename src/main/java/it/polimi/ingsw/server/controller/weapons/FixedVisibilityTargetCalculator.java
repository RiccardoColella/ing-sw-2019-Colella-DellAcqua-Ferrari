package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.player.Player;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is a TargetCalculator that finds targets that are (or are not) visible from the starting point
 */
public class FixedVisibilityTargetCalculator implements TargetCalculator {
    /**
     * This property states whether the computed targets are the visible ones (true) or non visible ones (false)
     */
    private final boolean visible;
    private final Board board;
    /**
     * This constructor sets the type of visibility that will be considered
     * @param visible true if targets are the visible ones, false if targets are the non visible ones
     */
    public FixedVisibilityTargetCalculator(Board board, boolean visible) {
        this.visible = visible;
        this.board = board;
    }

    /**
     * This method returns the groups of Damageable that can be targeted by the Attack solely based on whether they can be seen from the starting point
     * @param startingPoint the Block relative to which the targets should be
     * @return a list of the available groups of targets, which will be empty if none are available
     */
    @Override
    public Set<Player> computeTargets(Block startingPoint, Weapon weapon) {
        if (visible) {
            return board.getVisibleBlocks(startingPoint).stream().flatMap(block -> block.getPlayers().stream()).collect(Collectors.toSet());
        } else {
            Set<Block> nonVisible = board.getBlocks();
            nonVisible.removeIf(block -> board.getVisibleBlocks(startingPoint).contains(block));
            return nonVisible.stream().flatMap(block -> block.getPlayers().stream()).collect(Collectors.toSet());
        }
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
