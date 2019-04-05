package it.polimi.ingsw.server.model.battlefield;
import it.polimi.ingsw.server.model.player.Player;

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
    private List<Player> players;

    /**
     * Block's row in the field
     */
    private int row;

    /**
     * Block's column in the field
     */
    private int column;

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
        this.borders = new EnumMap<>(Direction.class);
        this.borders.put(Direction.NORTH,  borderNorth);
        this.borders.put(Direction.EAST,  borderEast);
        this.borders.put(Direction.SOUTH, borderSouth);
        this.borders.put(Direction.WEST,  borderWest);

        this.players = new LinkedList<>();
    }

    /**
     * Gets the BorderType in the direction asked
     * @param direction border investigated
     * @return BorderType in the direction asked
     */
    public BorderType getBoarderType(Direction direction) {
        return this.borders.get(direction);
    }

    /**
     * Gets players on the Block
     * @return players on the Block
     */
    public List<Player> getPlayers() {
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

}
