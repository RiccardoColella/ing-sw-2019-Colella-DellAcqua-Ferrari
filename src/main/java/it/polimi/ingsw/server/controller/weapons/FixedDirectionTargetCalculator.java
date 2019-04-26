package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.shared.Direction;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
    public Set<Player> computeTargets(Block startingPoint, BasicWeapon weapon) {
        updateDirection(startingPoint, weapon);
        List<Block> possibleBlocks = new LinkedList<>();
        if (this.direction != null) {
            possibleBlocks = evalDirection(this.direction, startingPoint);
            if (!goesThroughWalls) {
                stopAtWalls(this.direction, possibleBlocks);
            }
        } else {
            List<Block> toAdd;
            for (Direction dir : Direction.values()) {
                toAdd = evalDirection(dir, startingPoint);
                if (!goesThroughWalls) {
                    stopAtWalls(dir, toAdd);
                }
                possibleBlocks.addAll(toAdd);
            }
        }
        possibleBlocks.removeIf(block -> block.getPlayers().isEmpty());
        return possibleBlocks.stream().flatMap(block -> block.getPlayers().stream()).collect(Collectors.toSet());
    }

    @Override
    public boolean contains(TargetCalculator calculator) {
        return calculator == this;
    }

    @Override
    public List<TargetCalculator> getSubCalculators() {
        return Collections.singletonList(this);
    }

    private List<Block> evalDirection(Direction dir, Block startingPoint) {
        List<Block> possibleBlocks = new LinkedList<>();
        List<Block> bothDirections;
        switch (dir) {
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
            default:
                throw new EnumConstantNotPresentException(Direction.class, "Unknown direction: " + dir);
        }
        return possibleBlocks;
    }

    /**
     * This method sets the direction of the targets to a new value
     * @param direction the value of Direction that will be used when computing targets, or null to reset it
     */
    public void setDirection(@Nullable Direction direction) {
        this.direction = direction;
    }

    private void updateDirection(Block startingPoint, BasicWeapon weapon) {
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
