package it.polimi.ingsw.server.model.factories;

import it.polimi.ingsw.server.model.*;
import it.polimi.ingsw.server.model.exceptions.UnknownEnumException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
                        new Block[]{
                                null,
                                new TurretBlock(2, 1, BorderType.DOOR, BorderType.NONE, BorderType.WALL, BorderType.WALL),
                                new TurretBlock(2, 2, BorderType.WALL, BorderType.DOOR, BorderType.WALL, BorderType.NONE),
                                new SpawnpointBlock(2, 3, BorderType.NONE, BorderType.WALL, BorderType.WALL, BorderType.DOOR, CoinColor.YELLOW, new LinkedList<>())
                        }
                };
            case BOARD_11_1:
                return new Block[][]{
                        new Block[]{
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
            case BOARD_11_2:
                return new Block[][]{
                        new Block[]{
                                new TurretBlock(0, 0, BorderType.WALL, BorderType.DOOR, BorderType.NONE, BorderType.WALL),
                                new TurretBlock(0, 1, BorderType.WALL, BorderType.NONE, BorderType.DOOR, BorderType.DOOR),
                                new SpawnpointBlock(0, 2, BorderType.WALL, BorderType.WALL, BorderType.DOOR, BorderType.NONE, CoinColor.BLUE, new LinkedList<>()),
                                null
                        },
                        new Block[]{
                                new SpawnpointBlock(1, 0, BorderType.NONE, BorderType.WALL, BorderType.DOOR, BorderType.WALL, CoinColor.RED, new LinkedList<>()),
                                new TurretBlock(1, 1, BorderType.DOOR, BorderType.NONE, BorderType.DOOR, BorderType.WALL),
                                new TurretBlock(1, 2, BorderType.DOOR, BorderType.DOOR, BorderType.WALL, BorderType.NONE),
                                new TurretBlock(1, 3, BorderType.WALL, BorderType.WALL, BorderType.NONE, BorderType.DOOR)
                        },
                        new Block[]{
                                new TurretBlock(2, 0, BorderType.DOOR, BorderType.NONE, BorderType.WALL, BorderType.WALL),
                                new TurretBlock(2, 1, BorderType.DOOR, BorderType.NONE, BorderType.WALL, BorderType.NONE),
                                new TurretBlock(2, 2, BorderType.WALL, BorderType.DOOR, BorderType.WALL, BorderType.NONE),
                                new SpawnpointBlock(2, 3, BorderType.NONE, BorderType.WALL, BorderType.WALL, BorderType.DOOR, CoinColor.YELLOW, new LinkedList<>())
                        }
                };

            case BOARD_12:
                return new Block[][]{
                        new Block[]{
                                new TurretBlock(0, 0, BorderType.WALL, BorderType.DOOR, BorderType.NONE, BorderType.WALL),
                                new TurretBlock(0, 1, BorderType.WALL, BorderType.NONE, BorderType.DOOR, BorderType.DOOR),
                                new SpawnpointBlock(0, 2, BorderType.WALL, BorderType.DOOR, BorderType.DOOR, BorderType.NONE, CoinColor.BLUE, new LinkedList<>()),
                                new TurretBlock(0, 3, BorderType.WALL, BorderType.WALL, BorderType.DOOR, BorderType.DOOR)
                        },
                        new Block[]{
                                new SpawnpointBlock(1, 0, BorderType.NONE, BorderType.WALL, BorderType.DOOR, BorderType.WALL, CoinColor.RED, new LinkedList<>()),
                                new TurretBlock(1, 1, BorderType.DOOR, BorderType.WALL, BorderType.DOOR, BorderType.WALL),
                                new TurretBlock(1, 2, BorderType.DOOR, BorderType.NONE, BorderType.NONE, BorderType.WALL),
                                new TurretBlock(1, 3, BorderType.DOOR, BorderType.WALL, BorderType.NONE, BorderType.NONE)
                        },
                        new Block[]{
                                new TurretBlock(2, 0, BorderType.DOOR, BorderType.NONE, BorderType.WALL, BorderType.WALL),
                                new TurretBlock(2, 1, BorderType.DOOR, BorderType.DOOR, BorderType.WALL, BorderType.NONE),
                                new TurretBlock(2, 2, BorderType.NONE, BorderType.NONE, BorderType.WALL, BorderType.DOOR),
                                new SpawnpointBlock(2, 3, BorderType.NONE, BorderType.WALL, BorderType.WALL, BorderType.NONE, CoinColor.YELLOW, new LinkedList<>())
                        }
                };
            default:
                throw new NotImplementedException();
        }
    }
}
