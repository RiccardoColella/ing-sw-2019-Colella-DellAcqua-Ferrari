package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.shared.Direction;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is a TargetCalculator that finds targets in a straight line from the starting point
 *
 * @author Adriana Ferrari, Carlo Dell'Acqua
 */
public class FixedDirectionTargetCalculator implements TargetCalculator {
    /**
     * This property saves the direction that shall be considered when computing the targets, if {@code null} the calculator
     * will consider all directions
     */
    private Direction direction;

    /**
     * The {@code Board} on which targets can be found
     */
    private final Board board;

    /**
     * Whether the calculator should stop looking in a {@code Direction} when a wall is found
     */
    private final boolean goesThroughWalls;

    /**
     * This constructor does not set a specific direction
     *
     * @param board the board on which the calculation will be done
     * @param goesThroughWalls specifies whether or not the calculation should stop when a wall is encountered
     */
    public FixedDirectionTargetCalculator(Board board, boolean goesThroughWalls) {
        this(board, goesThroughWalls,null);
    }

    /**
     * This constructor sets the direction that shall be considered when computing the target
     *
     * @param board the board on which the calculation will be done
     * @param goesThroughWalls specifies whether or not the calculation should stop when a wall is encountered
     * @param direction the desired Direction
     */
    public FixedDirectionTargetCalculator(Board board, boolean goesThroughWalls, @Nullable Direction direction) {
        this.direction = direction;
        this.board = board;
        this.goesThroughWalls = goesThroughWalls;
    }

    /**
     * This method returns the groups of players that can be targeted by the Attack solely based on their position in a straight direction from the starting point
     *
     * @param startingPoint the {@code Block} relative to which the targets should be
     * @return a list of the available groups of targets, which will be empty if none are available
     */
    @Override
    public Set<Player> computeTargets(Block startingPoint, Weapon weapon) {
        updateDirection(startingPoint, weapon);

        Set<Block> blocks = new HashSet<>(Collections.singletonList(startingPoint));

        if (this.direction == null) {
            for (Direction dir : Direction.values()) {
                addBlocks(startingPoint, blocks, dir);
            }
        } else {
            addBlocks(startingPoint, blocks, this.direction);
        }

        return blocks
                .stream()
                .flatMap(block -> block.getPlayers().stream())
                .collect(Collectors.toSet());
    }

    /**
     * Adds the blocks on which potential targets can be found to the {@code Set} {@code blocks} passed as a parameter
     *
     * @param startingPoint the {@code Block} relative to which the targets should be
     * @param blocks the {@code Set} that will be updated
     * @param direction the {@code Direction} to consider
     */
    private void addBlocks(Block startingPoint, Set<Block> blocks, Direction direction) {
        Block neighbor = startingPoint;
        do {
            neighbor = !goesThroughWalls && neighbor.getBorderType(direction) == Block.BorderType.WALL ?
                    null :
                    board.getBlockNeighbor(neighbor, direction).orElse(null);
            if (neighbor != null) {
                blocks.add(neighbor);
            }
        } while (neighbor  != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(TargetCalculator calculator) {
        return calculator == this;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * Updates the {@code Direction} this calculator will consider, if necessary
     *
     * @param startingPoint the starting point of the attack
     * @param weapon the weapon this calculator is used for
     */
    private void updateDirection(Block startingPoint, Weapon weapon) {
        Attack attack;
        if ((attack = needToUpdate(weapon)) != null) {
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

    /**
     * Returns the {@code Attack}, previously executed by {@code weapon}, that has used this calculator
     *
     * @param weapon the weapon that is using this calculator
     * @return the {@code Attack} that has used this calculator, {@code null} if none did
     */
    private @Nullable Attack needToUpdate(Weapon weapon) {
        boolean toSet;
        for (Attack a : weapon.getExecutedAttacks()) {
            for (ActionConfig c : a.getActionConfigs()) {
                toSet = c.getCalculator().map(calculator -> calculator.contains(this) && !weapon.wasHitBy(a).isEmpty()).orElse(false);
                if (toSet) {
                    return a;
                }
            }
        }
        return null;
    }
}
