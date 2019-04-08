package it.polimi.ingsw.server.model.battlefield;

import it.polimi.ingsw.server.model.factories.BoardFactory;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.server.model.player.PlayerInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board10;
    private Board board11a;
    private Board board11b;
    private Board board12;

    private List<PlayerInfo> playerInfos = new LinkedList<>();
    private List<Player> players = new LinkedList<>();

    @BeforeEach
    void setUp() {
        board10 = BoardFactory.create(BoardFactory.Preset.BOARD_1);
        board11a = BoardFactory.create(BoardFactory.Preset.BOARD_2);
        board11b = BoardFactory.create(BoardFactory.Preset.BOARD_3);
        board12 = BoardFactory.create(BoardFactory.Preset.BOARD_4);
        for (int i = 0; i < 5; i++) {
            playerInfos.add(new PlayerInfo("Player" + i, PlayerColor.values()[i]));
        }
        for (int i = 0; i < 5; i++){
            players.add(new Player(playerInfos.get(i)));
        }
    }

    @Test
    void getBlock() {
        Optional<Block> block_11a_1_1 = board11a.getBlock(1,1);
        Optional<Block> block_11b_1_1 = board11b.getBlock(1,1);
        assertFalse(board11a.getBlock(2,0).isPresent(), "1. Error with a non present block in BOARD_2");
        assertFalse(board11a.getBlock(-1, 2).isPresent(), "2. Error in bound exception.");
        assertFalse(board11a.getBlock(0,4).isPresent(), "3. Error in bound exception.");
        assertEquals(block_11a_1_1, board11a.getBlock(1,1), "4. Error in picking block in BOARD_2.");
        assertEquals(block_11b_1_1, board11b.getBlock(1,1), "5. Error in picking block in BOARD_3.");

    }

    @Test
    void getBlockNeighbor() {
        //Tests on BOARD_1
        Optional<Block> block_10_0_1 = board10.getBlock(0,1);
        Optional<Block> block_10_2_3 = board10.getBlock(2,3);
        Block toUseForTest = null;
        if (board10.getBlock(0,0).isPresent()) toUseForTest = board10.getBlock(0, 0).get();
        assertNotNull(toUseForTest);
        assertFalse(board10.getBlockNeighbor(toUseForTest, Direction.NORTH).isPresent(), "1. Error in bound exception.");
        assertEquals(block_10_0_1, board10.getBlockNeighbor(toUseForTest, Direction.EAST), "2. Error in EAST direction picking with BOARD_1.");
        if (board10.getBlock(0,2).isPresent()) toUseForTest = board10.getBlock(0, 2).get();
        assertFalse(board10.getBlockNeighbor(toUseForTest, Direction.EAST).isPresent(), "3. Error in a non present block in BOARD_1.");
        if (board10.getBlock(1,3).isPresent()) toUseForTest = board10.getBlock(1, 3).get();
        assertEquals(block_10_2_3, board10.getBlockNeighbor(toUseForTest, Direction.SOUTH), "4. Error in SOUTH direction picking with BOARD_1.");
        //Tests on Board_12
        Optional<Block> block_12_0_3 = board12.getBlock(0,3);
        Optional<Block> block_12_1_0 = board12.getBlock(1,0);
        if (board10.getBlock(0,2).isPresent()) toUseForTest = board10.getBlock(0, 2).get();
        assertEquals(block_12_0_3, board12.getBlockNeighbor(toUseForTest, Direction.EAST), "5. Error in EAST direction picking with BOARD_4.");
        if (board10.getBlock(0,0).isPresent()) toUseForTest = board10.getBlock(0, 0).get();
        assertEquals(block_12_1_0, board12.getBlockNeighbor(toUseForTest, Direction.SOUTH), "6. Error in SOUTH direction picking with BOARD_4.");
        assertFalse(board12.getBlockNeighbor(toUseForTest, Direction.NORTH).isPresent(), "7. Error in bound exception.");
        assertFalse(board12.getBlockNeighbor(toUseForTest, Direction.WEST).isPresent(), "8. Error in bound exception.");
    }

    @Test
    void getVisibleBlocks() {
        Optional<Block> block_12_0_3 = board12.getBlock(0,3);
        assertTrue(block_12_0_3.isPresent());
        Set<Block> asserted = new HashSet<>(board12.getRoom(block_12_0_3.get()));
        Optional<Block> nearRoom1 = board12.getBlockNeighbor(block_12_0_3.get(), Direction.SOUTH);
        Optional<Block> nearRoom2 = board12.getBlockNeighbor(block_12_0_3.get(), Direction.WEST);
        assertTrue(nearRoom1.isPresent());
        assertTrue(nearRoom2.isPresent());
        asserted.addAll(board12.getRoom(nearRoom1.get()));
        asserted.addAll(board12.getRoom(nearRoom2.get()));
        assertEquals(asserted, board12.getVisibleBlocks(block_12_0_3.get()));

        Optional<Block> block_10_1_2 = board10.getBlock(1,2);
        assertTrue(block_10_1_2.isPresent());
        asserted.clear();
        assertTrue(asserted.isEmpty());
        asserted.addAll(board10.getRoom(block_10_1_2.get()));
        nearRoom1 = board10.getBlockNeighbor(block_10_1_2.get(), Direction.NORTH);
        nearRoom2 = board10.getBlockNeighbor(block_10_1_2.get(), Direction.EAST);
        assertTrue(nearRoom1.isPresent());
        assertTrue(nearRoom2.isPresent());
        asserted.addAll(board10.getRoom(nearRoom1.get()));
        asserted.addAll(board10.getRoom(nearRoom2.get()));
        assertEquals(asserted, board10.getVisibleBlocks(block_10_1_2.get()));
    }

    @Test
    void getRoom() {
        Optional<Block> block_10_2_1 = board10.getBlock(2,1);
        Optional<Block> block_10_2_2 = board10.getBlock(2,2);
        Set<Block> roomWhite10 = new HashSet<>();
        Block toUseForTest = null;
        if (block_10_2_1.isPresent()) toUseForTest = block_10_2_1.get();
        roomWhite10.add(toUseForTest);
        if (block_10_2_2.isPresent()) toUseForTest = block_10_2_2.get();
        roomWhite10.add(toUseForTest);
        assertEquals(roomWhite10, board10.getRoom(Objects.requireNonNull(toUseForTest)), "1. Error in getting white room in BOARD 10 part2of2.");
        if (block_10_2_1.isPresent()) toUseForTest = block_10_2_1.get();
        assertEquals(roomWhite10, board10.getRoom(toUseForTest), "2. Error in getting white room in BOARD 10 part1of2.");

        Optional<Block> block_11a_0_3 = board11a.getBlock(0,3);
        Set<Block> roomGreen11a = new HashSet<>();
        if (block_11a_0_3.isPresent()) toUseForTest = block_11a_0_3.get();
        roomGreen11a.add(toUseForTest);
        assertEquals(roomGreen11a, board11a.getRoom(toUseForTest), "3. Error in getting green room in BOARD_2.");

        Optional<Block> block_11a_1_2 = board11a.getBlock(1,2);
        Optional<Block> block_11a_1_3 = board11a.getBlock(1,3);
        Optional<Block> block_11a_2_2 = board11a.getBlock(2,2);
        Optional<Block> block_11a_2_3 = board11a.getBlock(2,3);
        Set<Block> roomYellow11a = new HashSet<>();
        if (block_11a_1_2.isPresent()) toUseForTest = block_11a_1_2.get();
        roomYellow11a.add(toUseForTest);
        if (block_11a_1_3.isPresent()) toUseForTest = block_11a_1_3.get();
        roomYellow11a.add(toUseForTest);
        if (block_11a_2_2.isPresent()) toUseForTest = block_11a_2_2.get();
        roomYellow11a.add(toUseForTest);
        if (block_11a_2_3.isPresent()) toUseForTest = block_11a_2_3.get();
        roomYellow11a.add(toUseForTest);
        if (block_11a_1_2.isPresent()) toUseForTest = block_11a_1_2.get();
        Set<Block> actual = board11a.getRoom(toUseForTest);
        assertEquals(roomYellow11a, actual, "4. Error in getting yellow room in BOARD_2.");

    }

    @Test
    void getRow() {
        Optional<Block> block_10_0_0 = board10.getBlock(0,0);
        Optional<Block> block_10_0_1 = board10.getBlock(0,1);
        Optional<Block> block_10_0_2 = board10.getBlock(0,2);
        List<Block> row_10_0 = new ArrayList<>();
        Block toUseForTest = null;
        if (block_10_0_0.isPresent()) toUseForTest = block_10_0_0.get();
        row_10_0.add(toUseForTest);
        if (block_10_0_1.isPresent()) toUseForTest = block_10_0_1.get();
        row_10_0.add(toUseForTest);
        if (block_10_0_2.isPresent()) toUseForTest = block_10_0_2.get();
        row_10_0.add(toUseForTest);
        if (block_10_0_1.isPresent()) toUseForTest = block_10_0_1.get();
        assertNotNull(toUseForTest);
        assertEquals(row_10_0, board10.getRow(toUseForTest));
        if (block_10_0_2.isPresent()) toUseForTest = block_10_0_2.get();
        assertEquals(row_10_0, board10.getRow(toUseForTest));

        Optional<Block> block_10_1_0 = board10.getBlock(1,0);
        Optional<Block> block_10_1_1 = board10.getBlock(1,1);
        Optional<Block> block_10_1_2 = board10.getBlock(1,2);
        Optional<Block> block_10_1_3 = board10.getBlock(1,3);
        List<Block> row_10_1 = new ArrayList<>();
        if (block_10_1_0.isPresent()) toUseForTest = block_10_1_0.get();
        row_10_1.add(toUseForTest);
        if (block_10_1_1.isPresent()) toUseForTest = block_10_1_1.get();
        row_10_1.add(toUseForTest);
        if (block_10_1_2.isPresent()) toUseForTest = block_10_1_2.get();
        row_10_1.add(toUseForTest);
        if (block_10_1_3.isPresent()) toUseForTest = block_10_1_3.get();
        row_10_1.add(toUseForTest);
        if (block_10_1_2.isPresent()) toUseForTest = block_10_1_2.get();
        assertEquals(row_10_1, board10.getRow(toUseForTest));
    }

    @Test
    void getColumn() {
        Optional<Block> block_10_0_0 = board10.getBlock(0,0);
        Optional<Block> block_10_1_0 = board10.getBlock(1,0);
        List<Block> column_10_0 = new ArrayList<>();
        Block toUseForTest = null;
        if (block_10_0_0.isPresent()) toUseForTest = block_10_0_0.get();
        column_10_0.add(toUseForTest);
        if (block_10_1_0.isPresent()) toUseForTest = block_10_1_0.get();
        column_10_0.add(toUseForTest);
        assertNotNull(toUseForTest);
        assertEquals(column_10_0, board10.getColumn(toUseForTest));
        if (block_10_0_0.isPresent()) toUseForTest = block_10_0_0.get();
        assertEquals(column_10_0, board10.getColumn(toUseForTest));

        Optional<Block> block_10_0_1 = board10.getBlock(0,1);
        Optional<Block> block_10_1_1 = board10.getBlock(1,1);
        Optional<Block> block_10_2_1 = board10.getBlock(2,1);
        List<Block> column_10_1 = new ArrayList<>();
        if (block_10_0_1.isPresent()) toUseForTest = block_10_0_1.get();
        column_10_1.add(toUseForTest);
        if (block_10_1_1.isPresent()) toUseForTest = block_10_1_1.get();
        column_10_1.add(toUseForTest);
        if (block_10_2_1.isPresent()) toUseForTest = block_10_2_1.get();
        column_10_1.add(toUseForTest);
        assertEquals(column_10_1, board10.getColumn(toUseForTest));
    }

    @Test
    void movePlayer() {
        //Testing null block on BOARD_10
        Optional<Block> block_10_0_2 = board10.getBlock(0,2);
        Optional<Block> block_10_1_2 = board10.getBlock(1,2);
        Player player0 = players.get(0);
        Block toUseForTest = null;
        if (block_10_0_2.isPresent()) toUseForTest = block_10_0_2.get();
        assertNotNull(toUseForTest);
        toUseForTest.addPlayer(player0);
        assertEquals(1, toUseForTest.getPlayers().size(), "Error: player0 seems not in position");
        board10.movePlayer(player0,Direction.EAST);
        assertEquals(1, toUseForTest.getPlayers().size(), "Error: player0 should not be moved in a null block");
        board10.movePlayer(player0, Direction.SOUTH);
        assertEquals(0, toUseForTest.getPlayers().size(), "Error: player0 should be moved");
        if (block_10_1_2.isPresent()) toUseForTest = block_10_1_2.get();
        assertEquals(1, toUseForTest.getPlayers().size(), "Error: player0 should be moved here!");
        toUseForTest.removePlayer(player0);
        //Testing boarder on BOARD_10
        Optional<Block> block_10_1_3 = board10.getBlock(1,3);
        Optional<Block> block_10_2_3 = board10.getBlock(2,3);
        if (block_10_1_3.isPresent()) toUseForTest = block_10_1_3.get();
        toUseForTest.addPlayer(player0);
        assertEquals(1, toUseForTest.getPlayers().size(), "Error: player0 seems not in position");
        board10.movePlayer(player0,Direction.EAST);
        assertEquals(1, toUseForTest.getPlayers().size(), "Error: player0 should not be moved over the boarder");
        board10.movePlayer(player0, Direction.SOUTH);
        assertEquals(0, toUseForTest.getPlayers().size(), "Error: player0 should be moved");
        if (block_10_2_3.isPresent()) toUseForTest = block_10_2_3.get();
        assertEquals(1, toUseForTest.getPlayers().size(), "Error: player0 should be moved here!");
        toUseForTest.removePlayer(player0);
        //Testing multiple players on BOARD_10
        assertEquals(0, toUseForTest.getPlayers().size());
        for (Player i : players){
            toUseForTest.addPlayer(i);
        }
        assertEquals(players.size(), toUseForTest.getPlayers().size(), "Scenario not well configured");
        int x = 3;
        board10.movePlayer(players.get(x), Direction.NORTH);
        List<Player> assertion = new LinkedList<>();
        for (int i = 0; i < players.size(); i++){
            if (i != x) {
                assertion.add(players.get(i));
            }
        }
        assertEquals(assertion, toUseForTest.getPlayers());
        if (block_10_1_3.isPresent()) toUseForTest = block_10_1_3.get();
        assertEquals(1, toUseForTest.getPlayers().size());
        assertEquals(players.get(x), toUseForTest.getPlayers().get(0));
        board10.movePlayer(players.get(x), Direction.SOUTH);
        if (block_10_2_3.isPresent()) toUseForTest = block_10_2_3.get();
        assertEquals(players.size(), toUseForTest.getPlayers().size());
        for (Player i : players){
            toUseForTest.removePlayer(i);
        }
        assertEquals(0, toUseForTest.getPlayers().size());
    }

    @Test
    void findPlayer() {
        int i = 4;
        int k = 0;
        //Trying to find a single player on the board
        Optional<Block> block0 = board11a.findPlayer(players.get(0));
        assertEquals(Optional.empty(), block0);
        Optional<Block> block_12_2_2 = board12.getBlock(2,2);
        Block toUseForTest = null;
        if (block_12_2_2.isPresent()) toUseForTest = block_12_2_2.get();
        assertNotNull(toUseForTest);
        toUseForTest.addPlayer(players.get(i));
        assertEquals(block_12_2_2, board12.findPlayer(players.get(i)));
        //Trying to find a player not alone in the board
        Optional<Block> block_12_0_0 = board12.getBlock(0,0);
        if (block_12_0_0.isPresent()) toUseForTest = block_12_0_0.get();
        toUseForTest.addPlayer(players.get(k));
        assertEquals(block_12_2_2, board12.findPlayer(players.get(i)));
        toUseForTest.removePlayer(players.get(k));
        //testing to find 2nd player on block
        toUseForTest = block_12_2_2.get();
        toUseForTest.addPlayer(players.get(k));
        assertEquals(block_12_2_2, board12.findPlayer(players.get(k)));
        toUseForTest.removePlayer(players.get(k));
        toUseForTest.removePlayer(players.get(i));
        //testing illegal state (2 times same player on board)
        Player playerUbiquitous = players.get(3);
        toUseForTest.addPlayer(playerUbiquitous);
        if (block_12_0_0.isPresent()) toUseForTest = block_12_0_0.get();
        toUseForTest.addPlayer(playerUbiquitous);
        try {
            board12.findPlayer(playerUbiquitous);
            fail();
        } catch (IllegalStateException e){
            assertNull(e.getMessage());
        }
        toUseForTest.removePlayer(playerUbiquitous);
        toUseForTest = block_12_2_2.get();
        toUseForTest.removePlayer(playerUbiquitous);
    }

    @Test
    void teleportPlayer() {
        int i = 2;
        int k = 1;
        int j = 0;
        //Trying to teleport a single player on the board
        Optional<Block> block_12_2_2 = board12.getBlock(2,2);
        Optional<Block> block_12_0_0 = board12.getBlock(0,0);
        Block toUseForTest = null;
        if (block_12_2_2.isPresent()) toUseForTest = block_12_2_2.get();
        assertNotNull(toUseForTest);
        toUseForTest.addPlayer(players.get(i));
        if (block_12_0_0.isPresent()) toUseForTest = block_12_0_0.get();
        board12.teleportPlayer(players.get(i), toUseForTest);
        assertEquals(block_12_0_0, board12.findPlayer(players.get(i)));
        //Trying to teleport a player not alone in the board
        toUseForTest = block_12_2_2.get();
        toUseForTest.addPlayer(players.get(k));
        board12.teleportPlayer(players.get(i), toUseForTest);
        assertEquals(block_12_2_2, board12.findPlayer(players.get(i)));
        assertEquals(block_12_2_2, board12.findPlayer(players.get(k)));
        //testing illegal state (player non present on board)
        if (block_12_0_0.isPresent()) toUseForTest = block_12_0_0.get();
        try {
            board12.teleportPlayer(players.get(j), toUseForTest);
            fail();
        } catch (NullPointerException e){
            assertNull(e.getMessage());
        }
        toUseForTest = block_12_2_2.get();
        toUseForTest.removePlayer(players.get(i));
        toUseForTest.removePlayer(players.get(k));
    }
}