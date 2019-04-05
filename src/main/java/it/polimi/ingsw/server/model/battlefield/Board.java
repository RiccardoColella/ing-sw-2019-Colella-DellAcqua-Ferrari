package it.polimi.ingsw.server.model.battlefield;

import it.polimi.ingsw.server.model.player.Player;

import java.util.LinkedList;
import java.util.List;

/**
 * This class implements the gameboard
 */
public class Board {

    private final Block[][] field;

    /**
     *
     * @param field the matrix containing all the blocks arranged according to the 2D representation of the board
     */
    public Board(Block[][] field) {
        this.field = field;
    }

    /**
     * Gets the block in the requested position
     * @param row of the requested Block
     * @param column of the requested Block
     * @return the requested Block
     */
    public Block getBlock(int row, int column) {
        // TODO getter with supervision
        return field[row][column];
    }

    /**
     * Tells who is the neighbor of the selected block
     * @param block known
     * @param direction where to look
     * @return the block next to the asked in the queried direction
     */
    public Block getBlockNeighbor(Block block, Direction direction) {
        //TODO getter with supervision
        Block neighbor = null;
        switch (direction) {
            case NORTH:
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
            default:
        }
        return neighbor;
    }

    /**
     * Tells the list of all visible Blocks from one Block
     * @param block starting Block
     * @return the List of all visible Blocks
     */
    public List<Block> getVisibleBlock(Block block) {
        List<Block> visibleBlocks = new LinkedList<>();
        if(block.getBoarderType(Direction.NORTH) == Block.BorderType.NONE ||
                block.getBoarderType(Direction.NORTH) == Block.BorderType.DOOR){
            Block toBeAdded = this.getBlockNeighbor(block, Direction.NORTH);
            if(!visibleBlocks.contains(toBeAdded)){
                visibleBlocks.add(toBeAdded);
            }
        }
        if(block.getBoarderType(Direction.EAST) == Block.BorderType.NONE ||
                block.getBoarderType(Direction.EAST) == Block.BorderType.DOOR){
            Block toBeAdded = this.getBlockNeighbor(block, Direction.EAST);
            if(!visibleBlocks.contains(toBeAdded)){
                visibleBlocks.add(toBeAdded);
            }
        }
        if(block.getBoarderType(Direction.SOUTH) == Block.BorderType.NONE ||
                block.getBoarderType(Direction.SOUTH) == Block.BorderType.DOOR){
            Block toBeAdded = this.getBlockNeighbor(block, Direction.SOUTH);
            if(!visibleBlocks.contains(toBeAdded)){
                visibleBlocks.add(toBeAdded);
            }
        }
        if(block.getBoarderType(Direction.WEST) == Block.BorderType.NONE ||
                block.getBoarderType(Direction.WEST) == Block.BorderType.DOOR){
            Block toBeAdded = this.getBlockNeighbor(block, Direction.WEST);
            if(!visibleBlocks.contains(toBeAdded)){
                visibleBlocks.add(toBeAdded);
            }
        }
        int nextBlock = visibleBlocks.indexOf(block) + 1;
        if(visibleBlocks.size() > nextBlock){
            this.getVisibleBlock( visibleBlocks.get(nextBlock) );
        }
        return visibleBlocks;
    }

    /**
     * Tells all the Blocks in the room of the selected Block
     * @param block starting Block
     * @return List of all Blocks in the room of the selected Block
     */
    public List<Block> getRoom(Block block) {
        List<Block> roomsBlock = null;
        if(block.getBoarderType(Direction.NORTH) == Block.BorderType.NONE){
            Block toBeAdded = this.getBlockNeighbor(block, Direction.NORTH);
            if(!roomsBlock.contains(toBeAdded)){
                roomsBlock.add(toBeAdded);
            }
        }
        if(block.getBoarderType(Direction.EAST) == Block.BorderType.NONE){
            Block toBeAdded = this.getBlockNeighbor(block, Direction.EAST);
            if(!roomsBlock.contains(toBeAdded)){
                roomsBlock.add(toBeAdded);
            }
        }
        if(block.getBoarderType(Direction.SOUTH) == Block.BorderType.NONE){
            Block toBeAdded = this.getBlockNeighbor(block, Direction.SOUTH);
            if(!roomsBlock.contains(toBeAdded)){
                roomsBlock.add(toBeAdded);
            }
        }
        if(block.getBoarderType(Direction.WEST) == Block.BorderType.NONE){
            Block toBeAdded = this.getBlockNeighbor(block, Direction.WEST);
            if(!roomsBlock.contains(toBeAdded)){
                roomsBlock.add(toBeAdded);
            }
        }
        int nextBlock = roomsBlock.indexOf(block) + 1;
        if(roomsBlock.size() > nextBlock){
            this.getVisibleBlock( roomsBlock.get(nextBlock) );
        }
        return roomsBlock;
    }

    /**
     * Tells the row of the selected Block
     * @param block selected Block
     * @return the row of the selected Block
     */
    public int getRow(Block block) {
        return block.getRow();
    }

    /**
     * Tells the column of the selected Block
     * @param block selected Block
     * @return the column of the selected Block
     */
    public int getColumn(Block block) {
        return block.getColumn();
    }

    /**
     * Move a Player in the selected direction
     * @param player Player to be moved
     * @param direction Direction in which to move
     */
    public void movePlayer(Player player, Direction direction) {
        //move player will check if the desired move is possible
    }

    /**
     * This method finds the position of a Player in the board
     * @param player Is the player to be searched
     * @return the block on which is positioned the player
     */
    private Block findPlayer(Player player) {
        Block playersBlock = null;
        for(int x = 0; x < 4; x++){
            for(int y = 0; y < 5; y++){
                if ((field[x][y] != null) && field[x][y].containsPlayer(player)) {
                    playersBlock = field[x][y];
                }
            }
        }
        return playersBlock;
    }

    /**
     * teleport player will move the player to the desired block without checking anything
     * @param player is the player who needs to be mooved
     * @param block is the destination block
     */
    public void teleportPlayer(Player player, Block block) {
        Block startingBlock = findPlayer(player);
        try{
            startingBlock.removePlayer(player);
        }
        catch (NullPointerException e){
            System.out.println(e);
        }
        block.addPlayer(player);
    }



}
