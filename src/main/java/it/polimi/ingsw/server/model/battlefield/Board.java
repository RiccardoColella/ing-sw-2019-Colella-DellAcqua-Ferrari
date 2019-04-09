package it.polimi.ingsw.server.model.battlefield;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.Player;

import java.util.*;
import java.util.List;

/**
 * This class implements the game board
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

    int getRowLenght(){
        return field.length;
    }

    int getColumnLenght(){
        return field[0].length;
    }

    /**
     * Gets the block in the requested position
     * @param row of the requested Block
     * @param column of the requested Block
     * @return the requested Block
     */
    public Optional<Block> getBlock(int row, int column) {
        if (row >= 0 && row < field.length && column>= 0 && column < field[0].length){
            return Optional.ofNullable(field[row][column]);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Tells who is the neighbor of the selected block
     * @param block known
     * @param direction where to look
     * @return the block next to the asked in the queried direction
     */
    public Optional<Block> getBlockNeighbor(Block block, Direction direction) {
        int r = block.getRow();
        int c = block.getColumn();
        switch (direction) {
            case NORTH:
                r--;
                break;
            case EAST:
                c++;
                break;
            case SOUTH:
                r++;
                break;
            case WEST:
                c--;
                break;
            default:
                throw new IllegalArgumentException();
        }
        if (r >= 0 && r < field.length && c >= 0 && c < field[0].length){
            return Optional.ofNullable(field[r][c]);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns true if in that direction there is a door
     * @param direction to observe
     * @param block starting position
     * @return true if in that direction there is a door
     */
    private boolean otherRoom(Direction direction, Block block){
        return block.getBoarderType(direction) == Block.BorderType.DOOR;
    }

    /**
     * Returns true if the block in that direction is in the same room.
     * @param direction to observe
     * @param block starting position
     * @return true if the block in that direction is in the same room.
     */
    private boolean sameRoom(Direction direction, Block block){
        Block.BorderType borderType = block.getBoarderType(direction);
        return borderType == Block.BorderType.NONE;
    }

    private boolean canMove(Direction direction, Block block){
        return sameRoom(direction, block) || otherRoom(direction, block);
    }

    /**
     * Tells the list of all visible Blocks from one Block
     * @param block starting Block
     * @return the List of all visible Blocks
     */
    public Set<Block> getVisibleBlocks(Block block) {
        Set<Block> visibleBlocks = new HashSet<>(getRoom(block));
        Arrays.stream(Direction.values())
                .filter(direction -> otherRoom(direction, block))
                .forEach(direction -> {
                    Optional<Block> toAdd = getBlockNeighbor(block, direction);
                    toAdd.ifPresent(block1 -> visibleBlocks.addAll(getRoom(block1)));
                });
        return visibleBlocks;
    }

    /**
     * Tells all the Blocks in the room of the selected Block
     * @param block starting Block
     * @return List of all Blocks in the room of the selected Block
     */
    public Set<Block> getRoom(Block block) {
        Set<Block> roomsBlock = new HashSet<>();
        roomsBlock.add(block);
        Queue<Block> queue = new LinkedList<>();
        queue.add(block);
        while(!queue.isEmpty()){
            Block controlledBlock = queue.remove();
            Arrays.stream(Direction.values())
                    .filter(direction -> sameRoom(direction, controlledBlock) && !roomsBlock.contains(getBlockNeighbor(controlledBlock, direction).orElse(null)))
                    .forEach(direction -> {
                        Optional<Block> toAdd = getBlockNeighbor(controlledBlock, direction);
                        if (toAdd.isPresent() && !roomsBlock.contains(toAdd.get())){
                            roomsBlock.add(toAdd.get());
                            queue.add(toAdd.get());
                        }
                    });
        }
        return roomsBlock;
    }

    /**
     * Returns the list of all blocks on the same row of the selected block
     * @param block selected Block
     * @return the list of all blocks on the same row of the selected block
     */
    public List<Block> getRow(Block block) {
        List<Block> row = new ArrayList<>();
        int r = block.getRow();
        if (r < field[r].length){
            for (Block blockInSameRow : field[r]){
                if (blockInSameRow != null){
                    row.add(blockInSameRow);
                }
            }
        } else throw new IllegalStateException();
        return row;
    }

    /**
     * Tells the column of the selected Block
     * @param block selected Block
     * @return the column of the selected Block
     */
    public List<Block> getColumn(Block block) {
        List<Block> column = new ArrayList<>();
        int c = block.getColumn();
        for (Block[] blocks : field) {
            Block toAdd = blocks[c];
            if (toAdd != null) {
                column.add(toAdd);
            }
        }
        return column;
    }

    /**
     * Move a Player in the selected direction
     * @param player Player to be moved
     * @param direction Direction in which to move
     */
    public void movePlayer(Player player, Direction direction) {
        //move player will check if the desired move is possible
        Optional<Block> position = findPlayer(player);
        Optional<Block> nextPosition;
        if (position.isPresent() && canMove(direction, position.get())){
            nextPosition = getBlockNeighbor(position.get(), direction);
            if (nextPosition.isPresent()){
                position.get().removePlayer(player);
                nextPosition.get().addPlayer(player);
            }
        }
    }

    /**
     * This method finds the position of a Player in the board
     * @param player Is the player to be searched
     * @return the block on which is positioned the player
     */
    public Optional<Block> findPlayer(Player player) {
        Optional<Block> blockWithPlayer = Optional.empty();
        for (Block[] blocks : field) {
            for (Block block : blocks) {
                if ((block != null) && block.containsPlayer(player)) {
                    if (!blockWithPlayer.isPresent()){
                        blockWithPlayer = Optional.of(block);
                    } else throw new IllegalStateException();
                }
            }
        }
        return blockWithPlayer;
    }

    /**
     * teleport player will move the player to the desired block without checking anything
     * @param player is the player who needs to be moved
     * @param block is the destination block
     */
    public void teleportPlayer(Player player, Block block) {
        Optional<Block> playerBlock = findPlayer(player);
        if (playerBlock.isPresent()){
            playerBlock.get().removePlayer(player);
        } else throw new NullPointerException();
        block.addPlayer(player);
    }

    public SpawnpointBlock getSpawnpoint(CurrencyColor color){
        for (Block[] blocks : field){
            for (Block block : blocks){
                if (block instanceof SpawnpointBlock && ((SpawnpointBlock) block).getColor() == color){
                    return (SpawnpointBlock) block;
                }
            }
        } throw new IllegalStateException();
    }

}
