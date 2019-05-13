package it.polimi.ingsw.server.model.battlefield;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.events.PlayerMoved;
import it.polimi.ingsw.server.model.events.listeners.BoardListener;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.utils.Range;

import java.util.*;

import static it.polimi.ingsw.server.model.battlefield.Block.BorderType.*;

/**
 * This class implements the game board
 */
public class Board {

    private final Block[][] field;
    private final Set<BoardListener> listeners = new HashSet<>();

    /**
     * @param field the matrix containing all the blocks arranged according to the 2D representation of the board
     */
    public Board(Block[][] field) {
        this.field = field;
    }

    int getRowLength(){
        return field.length;
    }

    int getColumnLength(){
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
        return block.getBorderType(direction) == Block.BorderType.DOOR;
    }

    /**
     * Returns true if the block in that direction is in the same room.
     * @param direction to observe
     * @param block starting position
     * @return true if the block in that direction is in the same room.
     */
    private boolean sameRoom(Direction direction, Block block){
        Block.BorderType borderType = block.getBorderType(direction);
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
                notifyPlayerMoved(player, nextPosition.get());
            }
        }
    }

    private void notifyPlayerMoved(Player player, Block destination) {
        PlayerMoved e = new PlayerMoved(this, player, destination);
        listeners.forEach(l -> l.onPlayerMoved(e));
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

        notifyPlayerTeleported(player, block);
    }

    private void notifyPlayerTeleported(Player player, Block block) {
        PlayerMoved e = new PlayerMoved(this, player, block);
        listeners.forEach(l -> l.onPlayerTeleported(e));
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

    /**
     * Copies method needed to create a new Board with the same field
     *
     * @return a new Board created with the same field
     */
    public Board copy() {

        Block[][] fieldCopy = new Block[field.length][field[0].length];

        for (int r = 0; r < field.length; r++) {
            for (int c = 0; c < field[0].length; c++) {
                fieldCopy[r][c] = field[r][c] == null ? null : field[r][c].copy();
            }
        }

        return new Board(fieldCopy);
    }

    public Set<Block> getBlocks() {
        Set<Block> blocks = new HashSet<>();
        for (Block[] row : field) {
            for (Block block : row) {
                if (block != null) {
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }

    public Set<Block> getReachableBlocks(Block startingPoint, Range range) {
        Set<Block> toCheck = new HashSet<>();
        Set<Block> alreadyChecked = new HashSet<>();
        toCheck.add(startingPoint);
        //getting all the blocks at min distance from starting point, step by step
        for (int i = 0; i < range.getMin(); i++) {
            checkNeighbors(alreadyChecked, toCheck);
        }
        //at this point, toCheck contains all the blocks at the min acceptable distance from startingPoint
        Set<Block> candidates = new HashSet<>(toCheck);
        //if min and max are different, all blocks with a distance smaller than max but greater than min will be added
        for (int distance = range.getMin(); distance < range.getMax(); distance++) {
            checkNeighbors(alreadyChecked, toCheck);
            candidates.addAll(toCheck);
        }
        return candidates;
    }

    /**
     * This method modifies the two input sets, adding new blocks to the set that needs to be checked and moving the checked blocks to alreadyChecked
     * @param alreadyChecked blocks that have previously been checked
     * @param toCheck the blocks that will be checked
     */
    private void checkNeighbors(Set<Block> alreadyChecked, Set<Block> toCheck) {
        List<Block> neighbors = new LinkedList<>();
        toCheck.forEach(block -> {
            for (Direction dir : Direction.values()) {
                if (block.getBorderType(dir) != WALL) {
                    this.getBlockNeighbor(block, dir).ifPresent(neighbors::add);
                }
            }
            alreadyChecked.add(block);
        });
        toCheck.clear();
        neighbors.forEach(n -> {
            if (!alreadyChecked.contains(n)) {
                toCheck.add(n);
            }
        });
    }

    /**
     * This methods returns a list of all directions in which a player can move
     * @param block starting block is the starting position
     * @return list of all possible directions
     */
    public List<Direction> getAvailableDirections(Block block){
        List<Direction> availableDirections = new ArrayList<>();
        for (Direction direction : Direction.values()){
            if (block.getBorderType(direction) == NONE ||
                    block.getBorderType(direction) == DOOR){
                availableDirections.add(direction);
            }
        }
        return availableDirections;
    }

    public boolean isOnASpawnpoint(Block block){
        return block instanceof SpawnpointBlock;
    }


    public void addBoardListener(BoardListener l) {
        listeners.add(l);
    }
}
