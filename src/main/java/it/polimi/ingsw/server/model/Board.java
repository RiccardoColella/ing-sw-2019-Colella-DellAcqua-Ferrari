package it.polimi.ingsw.server.model;

import it.polimi.ingsw.server.model.exceptions.UnknownEnumException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This class implements the gameboard
 */
public class Board {

    private final Block[][] field;

    private final Block[][] PRESET_10 = new Block[][] {
        new Block[] {
            new TurretBlock(0, 0, BorderType.WALL, BorderType.NONE, BorderType.DOOR, BorderType.WALL),
            new TurretBlock(0, 1, BorderType.WALL, BorderType.NONE, BorderType.WALL, BorderType.NONE),
            new SpawnpointBlock(0, 2, BorderType.WALL, BorderType.WALL, BorderType.DOOR, BorderType.NONE, CoinColor.BLUE, new LinkedList<>()),
            null
        },
        new Block[]{
            new SpawnpointBlock(1, 0, BorderType.DOOR, BorderType.NONE, BorderType.WALL, BorderType.WALL, CoinColor.RED, new LinkedList<>()),
            new TurretBlock(1, 1, BorderType.WALL, BorderType.NONE, BorderType.DOOR, BorderType.NONE),
            new TurretBlock(1, 2, BorderType.DOOR, BorderType.DOOR, BorderType.WALL, BorderType.NONE),
            new TurretBlock(1, 3, BorderType.WALL, BorderType.WALL, BorderType.NONE, BorderType.DOOR)
        },
        new Block[] {
            null,
            new TurretBlock(2, 1, BorderType.DOOR, BorderType.NONE, BorderType.WALL, BorderType.WALL),
            new TurretBlock(2, 2, BorderType.WALL, BorderType.DOOR, BorderType.WALL, BorderType.NONE),
            new SpawnpointBlock(2, 3, BorderType.NONE, BorderType.WALL, BorderType.WALL, BorderType.DOOR, CoinColor.YELLOW, new LinkedList<>())
        }
    };

    private final Block[][] PRESET_11_1 = new Block[][] {
        new Block[] {
            new TurretBlock(0, 0, BorderType.WALL, BorderType.NONE, BorderType.DOOR, BorderType.WALL),
            new TurretBlock(0, 1, BorderType.WALL, BorderType.NONE, BorderType.WALL, BorderType.NONE),
            new SpawnpointBlock(0, 2, BorderType.WALL, BorderType.DOOR, BorderType.DOOR, BorderType.NONE, CoinColor.BLUE, new LinkedList<>()),
            new TurretBlock(0, 3, BorderType.WALL, BorderType.WALL, BorderType.DOOR, BorderType.DOOR)
        },
        new Block[]{
            new SpawnpointBlock(1, 0, BorderType.DOOR, BorderType.NONE, BorderType.WALL, BorderType.WALL, CoinColor.RED, new LinkedList<>()),
            new TurretBlock(1, 1, BorderType.WALL, BorderType.WALL, BorderType.DOOR, BorderType.NONE),
            new TurretBlock(1, 2, BorderType.DOOR, BorderType.NONE, BorderType.NONE, BorderType.WALL),
            new TurretBlock(1, 3, BorderType.DOOR, BorderType.WALL, BorderType.NONE, BorderType.NONE)
        },
        new Block[]{
            null,
            new TurretBlock(2, 1, BorderType.DOOR, BorderType.DOOR, BorderType.WALL, BorderType.WALL),
            new TurretBlock(2, 2, BorderType.NONE, BorderType.NONE, BorderType.WALL, BorderType.DOOR),
            new SpawnpointBlock(2, 3, BorderType.NONE, BorderType.WALL, BorderType.WALL, BorderType.NONE, CoinColor.YELLOW, new LinkedList<>())
        }
    };

    private final Block[][] PRESET_11_2 = new Block[][] {
        new Block[] {
            new TurretBlock(0, 0, BorderType.WALL, BorderType.DOOR, BorderType.NONE, BorderType.WALL),
            new TurretBlock(0, 1, BorderType.WALL, BorderType.NONE, BorderType.DOOR, BorderType.DOOR),
            new SpawnpointBlock(0, 2, BorderType.WALL, BorderType.WALL, BorderType.DOOR, BorderType.NONE, CoinColor.BLUE, new LinkedList<>()),
            null
        },
        new Block[] {
            new SpawnpointBlock(1, 0, BorderType.NONE, BorderType.WALL, BorderType.DOOR, BorderType.WALL, CoinColor.RED, new LinkedList<>()),
            new TurretBlock(1, 1, BorderType.DOOR, BorderType.NONE, BorderType.DOOR, BorderType.WALL),
            new TurretBlock(1, 2, BorderType.DOOR, BorderType.DOOR, BorderType.WALL, BorderType.NONE),
            new TurretBlock(1, 3, BorderType.WALL, BorderType.WALL, BorderType.NONE, BorderType.DOOR)
        },
        new Block[] {
            new TurretBlock(2, 0, BorderType.DOOR, BorderType.NONE, BorderType.WALL, BorderType.WALL),
            new TurretBlock(2, 1, BorderType.DOOR, BorderType.NONE, BorderType.WALL, BorderType.NONE),
            new TurretBlock(2, 2, BorderType.WALL, BorderType.DOOR, BorderType.WALL, BorderType.NONE),
            new SpawnpointBlock(2, 3, BorderType.NONE, BorderType.WALL, BorderType.WALL, BorderType.DOOR, CoinColor.YELLOW, new LinkedList<>())
        }
    };

    private final Block[][] PRESET_12 = new Block[][] {
        new Block[] {
            new TurretBlock(0, 0, BorderType.WALL, BorderType.DOOR, BorderType.NONE, BorderType.WALL),
            new TurretBlock(0, 1, BorderType.WALL, BorderType.NONE, BorderType.DOOR, BorderType.DOOR),
            new SpawnpointBlock(0, 2, BorderType.WALL, BorderType.DOOR, BorderType.DOOR, BorderType.NONE, CoinColor.BLUE, new LinkedList<>()),
            new TurretBlock(0, 3, BorderType.WALL, BorderType.WALL, BorderType.DOOR, BorderType.DOOR)
        },
        new Block[] {
            new SpawnpointBlock(1, 0, BorderType.NONE, BorderType.WALL, BorderType.DOOR, BorderType.WALL, CoinColor.RED, new LinkedList<>()),
            new TurretBlock(1, 1, BorderType.DOOR, BorderType.WALL, BorderType.DOOR, BorderType.WALL),
            new TurretBlock(1, 2, BorderType.DOOR, BorderType.NONE, BorderType.NONE, BorderType.WALL),
            new TurretBlock(1, 3, BorderType.DOOR, BorderType.WALL, BorderType.NONE, BorderType.NONE)
        },
        new Block[] {
            new TurretBlock(2, 0, BorderType.DOOR, BorderType.NONE, BorderType.WALL, BorderType.WALL),
            new TurretBlock(2, 1, BorderType.DOOR, BorderType.DOOR, BorderType.WALL, BorderType.NONE),
            new TurretBlock(2, 2, BorderType.NONE, BorderType.NONE, BorderType.WALL, BorderType.DOOR),
            new SpawnpointBlock(2, 3, BorderType.NONE, BorderType.WALL, BorderType.WALL, BorderType.NONE, CoinColor.YELLOW, new LinkedList<>())
        }
    };

    /**
     * Class constructor
     * @param preset the field preset that will be used in the match
     */
    public Board(BoardPreset preset) throws UnknownEnumException {
        switch (preset) {
            case BOARD_10:
                this.field = PRESET_10;
                break;
            case BOARD_11_1:
                this.field = PRESET_11_1;
                break;
            case BOARD_11_2:
                this.field = PRESET_11_2;
                break;
            case BOARD_12:
                this.field = PRESET_12;
                break;
            default:
                throw new UnknownEnumException("Enum value " + preset + " is not an acceptable value when building a Board");
        }
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
        //TODO all function
        return new ArrayList<>();
    }

    /**
     * Tells all the Blocks in the room of the selected Block
     * @param block starting Block
     * @return List of all Blocks in the room of the selected Block
     */
    public List<Block> getRoom(Block block) {
        //TODO all function
        return new ArrayList<>();
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
     * Moves a player in the selected Block
     * @param player Player to be moved
     * @param block Destination Block
     */
    public void teleportPlayer(Player player, Block block) {
        //teleport player will move the player to the desired block without checking anything
    }



}
