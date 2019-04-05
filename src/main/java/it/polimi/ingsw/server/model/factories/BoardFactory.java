package it.polimi.ingsw.server.model.factories;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.battlefield.SpawnpointBlock;
import it.polimi.ingsw.server.model.battlefield.TurretBlock;
import it.polimi.ingsw.server.model.currency.CurrencyColor;

import java.util.LinkedList;

public class BoardFactory {

    /**
     * This enum represents the possible configurations the board can have
     */
    public enum Preset {
        BOARD_10,
        BOARD_11_1,
        BOARD_11_2,
        BOARD_12
    }

    public static Board create(Preset preset) {
        return new Board(presetGenerator(preset));
    }

    private static Block[][] presetGenerator(Preset preset) {
        switch (preset) {
            case BOARD_10:
                return new Block[][]{
                        new Block[]{
                                new TurretBlock(0, 0, Block.BorderType.WALL, Block.BorderType.NONE, Block.BorderType.DOOR, Block.BorderType.WALL),
                                new TurretBlock(0, 1, Block.BorderType.WALL, Block.BorderType.NONE, Block.BorderType.WALL, Block.BorderType.NONE),
                                new SpawnpointBlock(0, 2, Block.BorderType.WALL, Block.BorderType.WALL, Block.BorderType.DOOR, Block.BorderType.NONE, CurrencyColor.BLUE, new LinkedList<>()),
                                null
                        },
                        new Block[]{
                                new SpawnpointBlock(1, 0, Block.BorderType.DOOR, Block.BorderType.NONE, Block.BorderType.WALL, Block.BorderType.WALL, CurrencyColor.RED, new LinkedList<>()),
                                new TurretBlock(1, 1, Block.BorderType.WALL, Block.BorderType.NONE, Block.BorderType.DOOR, Block.BorderType.NONE),
                                new TurretBlock(1, 2, Block.BorderType.DOOR, Block.BorderType.DOOR, Block.BorderType.WALL, Block.BorderType.NONE),
                                new TurretBlock(1, 3, Block.BorderType.WALL, Block.BorderType.WALL, Block.BorderType.NONE, Block.BorderType.DOOR)
                        },
                        new Block[]{
                                null,
                                new TurretBlock(2, 1, Block.BorderType.DOOR, Block.BorderType.NONE, Block.BorderType.WALL, Block.BorderType.WALL),
                                new TurretBlock(2, 2, Block.BorderType.WALL, Block.BorderType.DOOR, Block.BorderType.WALL, Block.BorderType.NONE),
                                new SpawnpointBlock(2, 3, Block.BorderType.NONE, Block.BorderType.WALL, Block.BorderType.WALL, Block.BorderType.DOOR, CurrencyColor.YELLOW, new LinkedList<>())
                        }
                };
            case BOARD_11_1:
                return new Block[][]{
                        new Block[]{
                                new TurretBlock(0, 0, Block.BorderType.WALL, Block.BorderType.NONE, Block.BorderType.DOOR, Block.BorderType.WALL),
                                new TurretBlock(0, 1, Block.BorderType.WALL, Block.BorderType.NONE, Block.BorderType.WALL, Block.BorderType.NONE),
                                new SpawnpointBlock(0, 2, Block.BorderType.WALL, Block.BorderType.DOOR, Block.BorderType.DOOR, Block.BorderType.NONE, CurrencyColor.BLUE, new LinkedList<>()),
                                new TurretBlock(0, 3, Block.BorderType.WALL, Block.BorderType.WALL, Block.BorderType.DOOR, Block.BorderType.DOOR)
                        },
                        new Block[]{
                                new SpawnpointBlock(1, 0, Block.BorderType.DOOR, Block.BorderType.NONE, Block.BorderType.WALL, Block.BorderType.WALL, CurrencyColor.RED, new LinkedList<>()),
                                new TurretBlock(1, 1, Block.BorderType.WALL, Block.BorderType.WALL, Block.BorderType.DOOR, Block.BorderType.NONE),
                                new TurretBlock(1, 2, Block.BorderType.DOOR, Block.BorderType.NONE, Block.BorderType.NONE, Block.BorderType.WALL),
                                new TurretBlock(1, 3, Block.BorderType.DOOR, Block.BorderType.WALL, Block.BorderType.NONE, Block.BorderType.NONE)
                        },
                        new Block[]{
                                null,
                                new TurretBlock(2, 1, Block.BorderType.DOOR, Block.BorderType.DOOR, Block.BorderType.WALL, Block.BorderType.WALL),
                                new TurretBlock(2, 2, Block.BorderType.NONE, Block.BorderType.NONE, Block.BorderType.WALL, Block.BorderType.DOOR),
                                new SpawnpointBlock(2, 3, Block.BorderType.NONE, Block.BorderType.WALL, Block.BorderType.WALL, Block.BorderType.NONE, CurrencyColor.YELLOW, new LinkedList<>())
                        }
                };
            case BOARD_11_2:
                return new Block[][]{
                        new Block[]{
                                new TurretBlock(0, 0, Block.BorderType.WALL, Block.BorderType.DOOR, Block.BorderType.NONE, Block.BorderType.WALL),
                                new TurretBlock(0, 1, Block.BorderType.WALL, Block.BorderType.NONE, Block.BorderType.DOOR, Block.BorderType.DOOR),
                                new SpawnpointBlock(0, 2, Block.BorderType.WALL, Block.BorderType.WALL, Block.BorderType.DOOR, Block.BorderType.NONE, CurrencyColor.BLUE, new LinkedList<>()),
                                null
                        },
                        new Block[]{
                                new SpawnpointBlock(1, 0, Block.BorderType.NONE, Block.BorderType.WALL, Block.BorderType.DOOR, Block.BorderType.WALL, CurrencyColor.RED, new LinkedList<>()),
                                new TurretBlock(1, 1, Block.BorderType.DOOR, Block.BorderType.NONE, Block.BorderType.DOOR, Block.BorderType.WALL),
                                new TurretBlock(1, 2, Block.BorderType.DOOR, Block.BorderType.DOOR, Block.BorderType.WALL, Block.BorderType.NONE),
                                new TurretBlock(1, 3, Block.BorderType.WALL, Block.BorderType.WALL, Block.BorderType.NONE, Block.BorderType.DOOR)
                        },
                        new Block[]{
                                new TurretBlock(2, 0, Block.BorderType.DOOR, Block.BorderType.NONE, Block.BorderType.WALL, Block.BorderType.WALL),
                                new TurretBlock(2, 1, Block.BorderType.DOOR, Block.BorderType.NONE, Block.BorderType.WALL, Block.BorderType.NONE),
                                new TurretBlock(2, 2, Block.BorderType.WALL, Block.BorderType.DOOR, Block.BorderType.WALL, Block.BorderType.NONE),
                                new SpawnpointBlock(2, 3, Block.BorderType.NONE, Block.BorderType.WALL, Block.BorderType.WALL, Block.BorderType.DOOR, CurrencyColor.YELLOW, new LinkedList<>())
                        }
                };

            case BOARD_12:
                return new Block[][]{
                        new Block[]{
                                new TurretBlock(0, 0, Block.BorderType.WALL, Block.BorderType.DOOR, Block.BorderType.NONE, Block.BorderType.WALL),
                                new TurretBlock(0, 1, Block.BorderType.WALL, Block.BorderType.NONE, Block.BorderType.DOOR, Block.BorderType.DOOR),
                                new SpawnpointBlock(0, 2, Block.BorderType.WALL, Block.BorderType.DOOR, Block.BorderType.DOOR, Block.BorderType.NONE, CurrencyColor.BLUE, new LinkedList<>()),
                                new TurretBlock(0, 3, Block.BorderType.WALL, Block.BorderType.WALL, Block.BorderType.DOOR, Block.BorderType.DOOR)
                        },
                        new Block[]{
                                new SpawnpointBlock(1, 0, Block.BorderType.NONE, Block.BorderType.WALL, Block.BorderType.DOOR, Block.BorderType.WALL, CurrencyColor.RED, new LinkedList<>()),
                                new TurretBlock(1, 1, Block.BorderType.DOOR, Block.BorderType.WALL, Block.BorderType.DOOR, Block.BorderType.WALL),
                                new TurretBlock(1, 2, Block.BorderType.DOOR, Block.BorderType.NONE, Block.BorderType.NONE, Block.BorderType.WALL),
                                new TurretBlock(1, 3, Block.BorderType.DOOR, Block.BorderType.WALL, Block.BorderType.NONE, Block.BorderType.NONE)
                        },
                        new Block[]{
                                new TurretBlock(2, 0, Block.BorderType.DOOR, Block.BorderType.NONE, Block.BorderType.WALL, Block.BorderType.WALL),
                                new TurretBlock(2, 1, Block.BorderType.DOOR, Block.BorderType.DOOR, Block.BorderType.WALL, Block.BorderType.NONE),
                                new TurretBlock(2, 2, Block.BorderType.NONE, Block.BorderType.NONE, Block.BorderType.WALL, Block.BorderType.DOOR),
                                new SpawnpointBlock(2, 3, Block.BorderType.NONE, Block.BorderType.WALL, Block.BorderType.WALL, Block.BorderType.NONE, CurrencyColor.YELLOW, new LinkedList<>())
                        }
                };
            default:
                throw new UnsupportedOperationException();
        }
    }
}
