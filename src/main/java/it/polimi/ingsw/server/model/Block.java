package it.polimi.ingsw.server.model;
import java.util.Map;
import java.util.List;

/**
 * This class implements the generic block that constitute the board
 */
public abstract class Block {

    /**
     * Defines the BorderType of Block's border in every Direction
     */
    private static Map<Direction, BorderType> borders;

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
     */
    public Block(int row, int column, BorderType borderNorth, BorderType borderEast, BorderType borderSouth,
                 BorderType borderWest) {
        this.row = row;
        this.column = column;
        // MAPPING DIRECTIONS
        this.borders.put(Direction.NORD,  borderNorth);
        this.borders.put(Direction.EAST,  borderEast);
        this.borders.put(Direction.SOUTH, borderSouth);
        this.borders.put(Direction.WEST,  borderWest);
    }

    /**
     * Gets the BorderType in the direction asked
     * @param direction border investigated
     * @return BorderType in the direction asked
     */
    public BorderType getBoarderType(Direction direction){
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
    public void addPlayer(Player player){
        this.players.add(player);
    }

    /**
     * Removes a player from a Block
     * @param player to be removed from the Block
     */
    public void removePlayer(Player player){
        this.players.remove(player);
    }

    /**
     * Check if a player is on a Block
     * @param player to be checked
     * @return true if the player is on the Block
     */
    public boolean containsPlayer(Player player){
        return this.players.contains(player);
    }

}
