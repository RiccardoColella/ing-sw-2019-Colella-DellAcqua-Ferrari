package it.polimi.ingsw.server.model.battlefield;

import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.shared.Direction;

import java.util.*;

/**
 * This class implements the generic block that constitutes the board
 */
public abstract class Block {

    /**
     * This enum represents the possible types of border
     */
    public enum BorderType {
        WALL,
        DOOR,
        NONE
    }


    /**
     * Defines the BorderType of Block's border in every Direction
     */
    private final Map<Direction, BorderType> borders;

    /**
     * List of the player on the block
     */
    private final Set<Player> players;

    /**
     * Block's row in the field
     */
    private final int row;

    /**
     * Block's column in the field
     */
    private final int column;

    /**
     * Class constructor given the position in the board and every BoarderType
     * @param row an int representing the row of the block in the board
     * @param column an int representing the column of the block in the board
     * @param borderNorth BorderType of the northern border
     * @param borderEast BorderType of the eastern border
     * @param borderSouth BorderType of the southern border
     * @param borderWest BorderType of the western border
     */
    public Block(int row, int column, BorderType borderNorth, BorderType borderEast, BorderType borderSouth,
                 BorderType borderWest) {
        this.row = row;
        this.column = column;

        // MAPPING DIRECTIONS
        EnumMap<Direction, BorderType> borderMap = new EnumMap<>(Direction.class);
        borderMap.put(Direction.NORTH,  borderNorth);
        borderMap.put(Direction.EAST,  borderEast);
        borderMap.put(Direction.SOUTH, borderSouth);
        borderMap.put(Direction.WEST,  borderWest);

        this.borders = Collections.unmodifiableMap(borderMap);

        this.players = new HashSet<>();
    }

    /**
     * Gets the BorderType in the direction asked
     * @param direction border investigated
     * @return BorderType in the direction asked
     */
    public BorderType getBorderType(Direction direction) {
        return this.borders.get(direction);
    }

    /**
     * Gets players on the Block
     * @return players on the Block
     */
    public Set<Player> getPlayers() {
        return this.players;
    }


    public int getRow() {
        return this.row;
    }

    public int getColumn() {
        return this.column;
    }

    /**
     * Adds a new player on the Block
     * @param player to be added on the Block
     */
    public void addPlayer(Player player) {
        this.players.add(player);
    }

    /**
     * Removes a player from a Block
     * @param player to be removed from the Block
     */
    public void removePlayer(Player player) {
        this.players.remove(player);
    }

    /**
     * Check if a player is on a Block
     * @param player to be checked
     * @return true if the player is on the Block
     */
    public boolean containsPlayer(Player player) {
        return this.players.contains(player);
    }

    /**
     * Drop an item on this block
     *
     * @param item the item to drop
     */
    public abstract void drop(Droppable item);

    /**
     * Creates a copy of the current block
     *
     * @return the copy
     */
    public abstract Block copy();
}
