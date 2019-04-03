package it.polimi.ingsw.server.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the gameboard
 */
public class Board {

    private final Block[][] field;

    /**
     * Class constructor
     * @param field the field that will be used in the match
     */
    public Board(Block[][] field){
        this.field = field;
    }

    /**
     * Gets the block in the requested position
     * @param row of the requested Block
     * @param column of the requested Block
     * @return the requested Block
     */
    public Block getBlock(int row, int column){
        // TODO getter with supervision
        return field[row][column];
    }

    /**
     * Tells who is the neighbor of the selected block
     * @param block known
     * @param direction where to look
     * @return the block next to the asked in the queried direction
     */
    public Block getBlockNeighbor(Block block, Direction direction){
        //TODO getter with supervision
        Block neighbor = null;
        switch (direction) {
            case NORD:
                neighbor = field[block.getRow() - 1][block.getColumn()];
                break;
            case EAST:
                neighbor = field[block.getRow()][block.getColumn() + 1];
                break;
            case SOUTH:
                neighbor = field[block.getRow() + 1][block.getColumn()];
                break;
            case WEST:
                neighbor = field[block.getRow()][block.getColumn() - 1];
                break;
        }
        return neighbor;
    }

    /**
     * Tells the list of all visible Blocks from one Block
     * @param block starting Block
     * @return the List of all visible Blocks
     */
    public List<Block> getVisibleBlock(Block block){
        //TODO all function
        return new ArrayList<>();
    }

    /**
     * Tells all the Blocks in the room of the selected Block
     * @param block starting Block
     * @return List of all Blocks in the room of the selected Block
     */
    public List<Block> getRoom(Block block){
        //TODO all function
        return new ArrayList<>();
    }

    /**
     * Tells the row of the selected Block
     * @param block selected Block
     * @return the row of the selected Block
     */
    public int getRow(Block block){
        return block.getRow();
    }

    /**
     * Tells the column of the selected Block
     * @param block selected Block
     * @return the column of the selected Block
     */
    public int getColumn(Block block){
        return block.getColumn();
    }

    /**
     * Move a Player in the selected direction
     * @param player Player to be moved
     * @param direction Direction in which to move
     */
    public void movePlayer(Player player, Direction direction){
        //move player will check if the desired move is possible
    }

    /**
     * Moves a player in the selected Block
     * @param player Player to be moved
     * @param block Destination Block
     */
    public void teleportPlayer(Player player, Block block){
        //teleport player will move the player to the desired block without checking anything
    }



}
