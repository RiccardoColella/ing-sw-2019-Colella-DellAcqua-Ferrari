package it.polimi.ingsw.server.model.battlefield;

import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.MatchFactory;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.shared.Direction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    /**
     * NOTES ABOUT VARIABLE NAMING:
     * Elements in tests have been named saying what they are, what board they refer to, and where in the board
     * they should be.
     * For this reason, for example: board3 is a board built using the BOARD_3 preset.
     * Blocks are so named in the following mode: block_BOARDNUM_ROWNUM_COLUMNUM. This means that:
     * block_1_2_3 is a block in board1, on row 2 and column 3.
     * row_1_2, instead, means the row 2 (starting from 0) of board1
     */

    private Board board1;
    private Board board2;
    private Board board3;
    private Board board4;

    private List<Player> players = new LinkedList<>();

    /**
     * Creating boards and player to be used for tests
     */
    @BeforeEach
    void setUp() {
        //Creating boards
        board1 = BoardFactory.create(BoardFactory.Preset.BOARD_1);
        board2 = BoardFactory.create(BoardFactory.Preset.BOARD_2);
        board3 = BoardFactory.create(BoardFactory.Preset.BOARD_3);
        board4 = BoardFactory.create(BoardFactory.Preset.BOARD_4);
        this.players = MatchFactory.create(
                IntStream
                        .range(0, 5)
                        .boxed()
                        .map(i -> "Player" + i)
                        .collect(Collectors.toList()),
                BoardFactory.Preset.BOARD_1,
                1,
                Match.Mode.STANDARD
        ).getPlayers();

    }

    /**
     * This test covers the method getBlock() in the following situations:
     * - getting a block non present in the board
     * - getting a block in row -1, in position column.size e row.size (out of bounds)
     * - getting some block that should exist
     */
    @Test
    void getBlock() {
        //Testing exceptions
        //Trying to select a non existing block
        assertFalse(board2.getBlock(2,0).isPresent(), "1. Error with a non present block in BOARD_2");

        //Testing bounds
        assertFalse(board2.getBlock(-1, 2).isPresent(), "2. Error in bound exception.");
        assertFalse(board2.getBlock(0,board2.getColumnLength()).isPresent(), "3a. Error in bound exception.");
        assertFalse(board2.getBlock(board2.getRowLength(), 0).isPresent(), "3b. Error in bound exception.");

        //Trying to select some existing blocks
        assertTrue(board2.getBlock(1,1).isPresent(), "4. Error in picking block in BOARD_2.");
        assertTrue(board3.getBlock(1,1).isPresent(), "5. Error in picking block in BOARD_3.");

    }

    /**
     * This test covers the method getBlockNeighbor() in the following situations:
     * - getting a block out of bounds
     * - getting an existing block
     * - getting a non existing block in the board
     * - getting some others existing blocks and out of bounds blocks on another board
     */
    @Test
    void getBlockNeighbor() {
        //Tests on BOARD_1
        //Getting some Optional<block> containing blocks expected as result
        Optional<Block> block_1_0_0 = board1.getBlock(0,0);
        Optional<Block> block_1_0_1 = board1.getBlock(0,1);
        Optional<Block> block_1_0_2 = board1.getBlock(0,2);
        Optional<Block> block_1_2_3 = board1.getBlock(2,3);
        Optional<Block> block_1_1_3 = board1.getBlock(1,3);
        //Trying to get an out-of-bounds block
        block_1_0_0.ifPresent(block -> assertFalse(board1.getBlockNeighbor(block, Direction.NORTH).isPresent(), "1. Error in bound exception."));
        //Trying to get an existing block
        block_1_0_0.ifPresent(block -> assertEquals(block_1_0_1, board1.getBlockNeighbor(block, Direction.EAST), "2. Error in EAST direction picking with BOARD_1."));
        //Trying to get a non-existing block
        block_1_0_2.ifPresent(block -> assertFalse(board1.getBlockNeighbor(block, Direction.EAST).isPresent(), "3. Error in a non present block in BOARD_1."));

        //Trying to get an existing block
        block_1_1_3.ifPresent(block -> assertEquals(block_1_2_3, board1.getBlockNeighbor(block, Direction.SOUTH), "4. Error in SOUTH direction picking with BOARD_1."));

        //Tests on BOARD_4
        //Getting some Optional<block> containing blocks expected as result
        Optional<Block> block_4_0_0 = board4.getBlock(0,0);
        Optional<Block> block_4_0_2 = board4.getBlock(0,2);
        Optional<Block> block_4_0_3 = board4.getBlock(0,3);
        Optional<Block> block_4_1_0 = board4.getBlock(1,0);
        //Trying to get an existing block
        block_4_0_2.ifPresent(block -> assertEquals(block_4_0_3, board4.getBlockNeighbor(block, Direction.EAST), "5. Error in EAST direction picking with BOARD_4."));
        //Trying to get an existing block
        block_4_0_0.ifPresent(block -> assertEquals(block_4_1_0, board4.getBlockNeighbor(block, Direction.SOUTH), "6. Error in SOUTH direction picking with BOARD_4."));
        //Trying to get an out-of-bounds block
        block_4_0_0.ifPresent(block -> assertFalse(board4.getBlockNeighbor(block, Direction.NORTH).isPresent(), "7. Error in bound exception."));
        block_4_0_0.ifPresent(block -> assertFalse(board4.getBlockNeighbor(block, Direction.WEST).isPresent(), "8. Error in bound exception."));
    }

    /**
     * This test covers the method getVisibleBlocks() in the following situations:
     * - getting a complex visible blocks's list
     * - getting a another complex visible blocks's list
     */
    @Test
    void getVisibleBlocks() {
        //Test on BOARD_4
        //Getting some Optional<block> containing blocks expected as result
        Optional<Block> block_4_0_3 = board4.getBlock(0,3);
        //Checking getBlock went ok
        assertTrue(block_4_0_3.isPresent());
        // Creating Set variable that will contain the expected result
        Set<Block> asserted = new HashSet<>(board4.getRoom(block_4_0_3.get()));
        //Getting some block that will be part of the expected result
        Optional<Block> nearRoom1 = board4.getBlockNeighbor(block_4_0_3.get(), Direction.SOUTH);
        Optional<Block> nearRoom2 = board4.getBlockNeighbor(block_4_0_3.get(), Direction.WEST);
        //Checking getBlockNeighbor went ok
        assertTrue(nearRoom1.isPresent());
        assertTrue(nearRoom2.isPresent());
        //Building expected result
        asserted.addAll(board4.getRoom(nearRoom1.get()));
        asserted.addAll(board4.getRoom(nearRoom2.get()));
        //Checking a complex computation
        assertEquals(asserted, board4.getVisibleBlocks(block_4_0_3.get()));

        //Test on BOARD_1
        //Getting some Optional<block> containing blocks expected as result
        Optional<Block> block_1_1_2 = board1.getBlock(1,2);
        //Checking getBlock went ok
        assertTrue(block_1_1_2.isPresent());
        //Cleaning Set that will be reused as expected result from previously test
        asserted.clear();
        //Building the expected result
        asserted.addAll(board1.getRoom(block_1_1_2.get()));
        nearRoom1 = board1.getBlockNeighbor(block_1_1_2.get(), Direction.NORTH);
        nearRoom2 = board1.getBlockNeighbor(block_1_1_2.get(), Direction.EAST);
        assertTrue(nearRoom1.isPresent());
        assertTrue(nearRoom2.isPresent());
        asserted.addAll(board1.getRoom(nearRoom1.get()));
        asserted.addAll(board1.getRoom(nearRoom2.get()));
        //Checking another complex computation
        assertEquals(asserted, board1.getVisibleBlocks(block_1_1_2.get()));
    }

    /**
     * This test covers the method getRoom() in the following situations:
     * - getting a room from different starting blocks passed as parameter
     * - getting a single-block room
     * - getting a complex room
     */
    @Test
    void getRoom() {
        //Tests on BOARD_1
        //Getting some Optional<block> containing blocks expected as result
        Optional<Block> block_1_2_1 = board1.getBlock(2,1);
        Optional<Block> block_1_2_2 = board1.getBlock(2,2);
        // Creating Set variable that will contain the expected result
        Set<Block> roomWhite1 = new HashSet<>();
        //Building the expected result
        block_1_2_1.ifPresent(roomWhite1::add);
        block_1_2_2.ifPresent(roomWhite1::add);
        //trying to select a room of 2 blocks passing one block
        block_1_2_2.ifPresent(block -> assertEquals(roomWhite1, board1.getRoom(Objects.requireNonNull(block)), "1. Error in getting white room in BOARD 1 part1of2."));
        //trying to select the room of 2 blocks passing the other block
        block_1_2_1.ifPresent(block -> assertEquals(roomWhite1, board1.getRoom(block), "2. Error in getting white room in BOARD 1 part2of2."));

        //Tests on BOARD_2
        //Getting some Optional<block> containing blocks expected as result
        Optional<Block> block_2_0_3 = board2.getBlock(0,3);
        // Creating Set variable that will contain the expected result
        Set<Block> roomGreen2 = new HashSet<>();
        //Testing a single-block room
        block_2_0_3.ifPresent(roomGreen2::add);
        block_2_0_3.ifPresent(block -> assertEquals(roomGreen2, board2.getRoom(block), "3. Error in getting green room in BOARD_2."));

        //Getting some Optional<block> containing blocks expected as result
        Optional<Block> block_2_1_2 = board2.getBlock(1,2);
        Optional<Block> block_2_1_3 = board2.getBlock(1,3);
        Optional<Block> block_2_2_2 = board2.getBlock(2,2);
        Optional<Block> block_2_2_3 = board2.getBlock(2,3);
        // Creating Set variable that will contain the expected result
        Set<Block> roomYellow2 = new HashSet<>();
        //Adding blocks to the expected result
        block_2_1_2.ifPresent(roomYellow2::add);
        block_2_1_3.ifPresent(roomYellow2::add);
        block_2_2_2.ifPresent(roomYellow2::add);
        block_2_2_3.ifPresent(roomYellow2::add);
        //Testing a more complex room
        block_2_1_2.ifPresent(block -> assertEquals(roomYellow2, board2.getRoom(block), "4. Error in getting yellow room in BOARD_2."));
    }

    /**
     * This test covers the method getRow() in the following situations:
     * - getting a row with a null block in the board
     * - getting a full row
     */
    @Test
    void getRow() {
        //Getting some Optional<block> containing blocks expected as result
        Optional<Block> block_1_0_0 = board1.getBlock(0,0);
        Optional<Block> block_1_0_1 = board1.getBlock(0,1);
        Optional<Block> block_1_0_2 = board1.getBlock(0,2);
        // Creating List variable that will contain the expected result
        List<Block> row0InBoard1 = new ArrayList<>();
        //Building the expected result
        block_1_0_0.ifPresent(row0InBoard1::add);
        block_1_0_1.ifPresent(row0InBoard1::add);
        block_1_0_2.ifPresent(row0InBoard1::add);
        //Testing a row with a null block
        block_1_0_1.ifPresent(block -> assertEquals(row0InBoard1, board1.getRow(block)));
        block_1_0_2.ifPresent(block -> assertEquals(row0InBoard1, board1.getRow(block)));

        //Getting some Optional<block> containing blocks expected as result
        Optional<Block> block_1_1_0 = board1.getBlock(1,0);
        Optional<Block> block_1_1_1 = board1.getBlock(1,1);
        Optional<Block> block_1_1_2 = board1.getBlock(1,2);
        Optional<Block> block_1_1_3 = board1.getBlock(1,3);
        //Building the expected result
        List<Block> row1InBoard1 = new ArrayList<>();
        block_1_1_0.ifPresent(row1InBoard1::add);
        block_1_1_1.ifPresent(row1InBoard1::add);
        block_1_1_2.ifPresent(row1InBoard1::add);
        block_1_1_3.ifPresent(row1InBoard1::add);
        //Testing a complete room
        block_1_1_2.ifPresent(block -> assertEquals(row1InBoard1, board1.getRow(block)));
    }

    /**
     * This test covers the method getColumn() in the following situations:
     * - getting a column with a null block in the board
     * - getting a full column
     */
    @Test
    void getColumn() {
        //Testing using board1
        //Getting some Optional<block> containing blocks expected as result
        Optional<Block> block_1_0_0 = board1.getBlock(0,0);
        Optional<Block> block_1_1_0 = board1.getBlock(1,0);
        //Building the expected result
        List<Block> column0InBoard1 = new ArrayList<>();
        block_1_0_0.ifPresent(column0InBoard1::add);
        block_1_1_0.ifPresent(column0InBoard1::add);
        //Testing a column with a null block
        block_1_1_0.ifPresent(block -> assertEquals(column0InBoard1, board1.getColumn(block)));
        //Testing the same result from a different block
        block_1_0_0.ifPresent(block -> assertEquals(column0InBoard1, board1.getColumn(block)));

        //Getting some Optional<block> containing blocks expected as result
        Optional<Block> block_1_0_1 = board1.getBlock(0,1);
        Optional<Block> block_1_1_1 = board1.getBlock(1,1);
        Optional<Block> block_1_2_1 = board1.getBlock(2,1);
        //Building the expected result
        List<Block> column1InBoard1 = new ArrayList<>();
        block_1_0_1.ifPresent(column1InBoard1::add);
        block_1_1_1.ifPresent(column1InBoard1::add);
        block_1_2_1.ifPresent(column1InBoard1::add);
        //Testing a complete column
        block_1_2_1.ifPresent(block -> assertEquals(column1InBoard1, board1.getColumn(block)));
    }

    /**
     * This test covers the method movePlayer() in the following situations:
     * - moving player with an invalid move (destination out-of-bounds)
     * - moving player with some valid moves
     * - moving player with many players on the board
     * - moving player who is present in more than one position in the board
     */
    @Test
    void movePlayer() {
        //Testing null block on BOARD_1
        //Getting some Optional<block> containing blocks expected as result
        Optional<Block> block_1_0_2 = board1.getBlock(0,2);
        Optional<Block> block_1_1_2 = board1.getBlock(1,2);
        //Declaring some players to be used during the tests
        Player player0 = players.get(0);
        //Adding the player to a block
        block_1_0_2.ifPresent(block -> block.addPlayer(player0));
        //Checking player is correctly positioned
        block_1_0_2.ifPresent(block -> assertEquals(1, block.getPlayers().size(), "Error: player0 seems not in position"));
        //Executing movePlayer with an out-of-bounds direction
        board1.movePlayer(player0,Direction.EAST);
        //Checking player not been moved
        block_1_0_2.ifPresent(block -> assertEquals(1, block.getPlayers().size(), "Error: player0 should not be moved in a null block"));

        //Testing valid move on BOARD_1
        //Executing movePlayer with a valid direction
        board1.movePlayer(player0, Direction.SOUTH);
        //Checking player is really been moved
        block_1_0_2.ifPresent(block -> assertEquals(0, block.getPlayers().size(), "Error: player0 should be moved"));
        block_1_1_2.ifPresent(block -> assertEquals(1, block.getPlayers().size(), "Block not correctly configured!"));
        //Removing player from the block
        block_1_1_2.ifPresent(block -> block.removePlayer(player0));

        //Testing boarder on BOARD_1
        //Getting some Optional<block> containing blocks expected as result
        Optional<Block> block_1_1_3 = board1.getBlock(1,3);
        Optional<Block> block_1_2_3 = board1.getBlock(2,3);
        //Adding a player to the board
        block_1_1_3.ifPresent(block -> block.addPlayer(player0));
        block_1_1_3.ifPresent(block -> assertEquals(1, block.getPlayers().size(), "Error: player0 seems not in position"));
        //Moving the player on the board giving an invalid direction
        board1.movePlayer(player0,Direction.EAST);
        block_1_1_3.ifPresent(block -> assertEquals(1, block.getPlayers().size(), "Error: player0 should not be moved over the boarder"));
        //Moving player on board giving a valid direction
        board1.movePlayer(player0, Direction.SOUTH);
        block_1_1_3.ifPresent(block -> assertEquals(0, block.getPlayers().size(), "Error: player0 should be moved"));
        block_1_2_3.ifPresent(block -> assertEquals(1, block.getPlayers().size(), "Error: player0 should be moved here!"));
        //Removing player from block
        block_1_2_3.ifPresent(block -> block.removePlayer(player0));

        //Testing multiple players on BOARD_1
        //Checking corrected setup
        block_1_2_3.ifPresent(block -> assertEquals(0,block.getPlayers().size()));
        //Adding many players to a block
        for (Player i : players){
            block_1_2_3.ifPresent(block -> block.addPlayer(i));
        }
        block_1_2_3.ifPresent(block -> assertEquals(players.size(), block.getPlayers().size(), "Scenario not well configured"));
        //Moving a single player
        int x = 3;
        board1.movePlayer(players.get(x), Direction.NORTH);
        //Building expected result
        Set<Player> assertion = new HashSet<>();
        for (int i = 0; i < players.size(); i++){
            if (i != x) {
                assertion.add(players.get(i));
            }
        }
        //Checking movePlayer
        block_1_2_3.ifPresent(block -> assertTrue(block.getPlayers().containsAll(assertion)));
        block_1_1_3.ifPresent(block -> assertEquals(1, block.getPlayers().size()));
        block_1_1_3.ifPresent(block -> assertTrue(block.getPlayers().contains(players.get(x))));
        //Removing players from the board
        board1.movePlayer(players.get(x), Direction.SOUTH);
        block_1_2_3.ifPresent(block -> assertEquals(players.size(), block.getPlayers().size()));
        for (Player i : players){
            block_1_2_3.ifPresent(block -> block.removePlayer(i));
        }
        block_1_2_3.ifPresent(block -> assertEquals(0, block.getPlayers().size()));
    }

    /**
     * This test covers the method findPlayer() in the following situations:
     * - finding a single player on the board
     * - finding a player not alone in the board
     * - finding a player not alone on his block
     * - finding a player who is present in more than one position (IllegalStateException)
     */
    @Test
    void findPlayer() {
        int i = 4;
        int k = 0;
        //Checking find player on an empty board
        Optional<Block> block0 = board2.findPlayer(players.get(0));
        assertEquals(Optional.empty(), block0);

        //Trying to find a single player on the board
        //Getting some Optional<block> containing blocks expected as result
        Optional<Block> block_4_2_2 = board4.getBlock(2,2);
        //Adding a player on the board
        block_4_2_2.ifPresent(block -> block.addPlayer(players.get(i)));
        //Testing findPlayer with a single player on the board
        assertEquals(block_4_2_2, board4.findPlayer(players.get(i)));

        //Trying to find a player not alone in the board
        //Getting some Optional<block> containing blocks expected as result
        Optional<Block> block_4_0_0 = board4.getBlock(0,0);
        //Adding another player in another block to the board
        block_4_0_0.ifPresent(block -> block.addPlayer(players.get(k)));
        //Testing 2 player on different blocks
        assertEquals(block_4_2_2, board4.findPlayer(players.get(i)));
        block_4_0_0.ifPresent(block -> block.removePlayer(players.get(k)));

        //testing to find 2nd player on same block
        block_4_2_2.ifPresent(block -> block.addPlayer(players.get(k)));
        assertEquals(block_4_2_2, board4.findPlayer(players.get(k)));
        //Cleaning scenario
        block_4_2_2.ifPresent(block -> block.removePlayer(players.get(k)));
        block_4_2_2.ifPresent(block -> block.removePlayer(players.get(i)));

        //testing illegal state (2 times same player on board)
        Player playerUbiquitous = players.get(3);
        block_4_2_2.ifPresent(block -> block.addPlayer(playerUbiquitous));
        block_4_0_0.ifPresent(block -> block.addPlayer(playerUbiquitous));
        assertThrows(IllegalStateException.class, () -> board4.findPlayer(playerUbiquitous));
        //Cleaning board
        block_4_0_0.ifPresent(block -> block.removePlayer(playerUbiquitous));
        block_4_2_2.ifPresent(block -> block.removePlayer(playerUbiquitous));
    }

    /**
     * This test covers the method teleportPlayer() in the following situations:
     * - teleporting a single player on the board
     * - teleporting a player not alone on the board
     * - teleporting a player not present on the board
     */
    @Test
    void teleportPlayer() {
        int i = 2;
        int k = 1;
        int j = 0;
        //Trying to teleport a single player on the board
        //Getting some Optional<block> containing blocks expected as result
        Optional<Block> block_4_2_2 = board4.getBlock(2,2);
        Optional<Block> block_4_0_0 = board4.getBlock(0,0);
        //Adding the player to the board
        block_4_2_2.ifPresent(block -> block.addPlayer(players.get(i)));
        //Executing teleportPlayer
        block_4_0_0.ifPresent(destinationBlock -> board4.teleportPlayer(players.get(i), destinationBlock));
        //Checking result
        assertEquals(block_4_0_0, board4.findPlayer(players.get(i)));

        //Trying to teleport a player not alone in the board
        //Adding another player to the board
        block_4_2_2.ifPresent(block -> block.addPlayer(players.get(k)));
        //Executing teleportPlayer
        block_4_2_2.ifPresent(destinationBlock -> board4.teleportPlayer(players.get(i), destinationBlock));
        //Checking result
        assertEquals(block_4_2_2, board4.findPlayer(players.get(i)));
        assertEquals(block_4_2_2, board4.findPlayer(players.get(k)));

        //testing illegal state (player non present on board)
        assertTrue(block_4_0_0.isPresent());
        assertThrows(NullPointerException.class, () -> board4.teleportPlayer(players.get(j), block_4_0_0.get()));
        //Cleaning scenario
        block_4_2_2.ifPresent(block -> block.removePlayer(players.get(i)));
        block_4_2_2.ifPresent(block -> block.removePlayer(players.get(k)));
    }
}