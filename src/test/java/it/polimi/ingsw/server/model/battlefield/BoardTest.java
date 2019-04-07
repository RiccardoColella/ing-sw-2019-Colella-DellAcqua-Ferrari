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

    List<PlayerInfo> playerInfos = new LinkedList<>();
    List<Player> players = new LinkedList<>();

    @BeforeEach
    void setUp() {
        board10 = BoardFactory.create(BoardFactory.Preset.BOARD_10);
        board11a = BoardFactory.create(BoardFactory.Preset.BOARD_11_1);
        board11b = BoardFactory.create(BoardFactory.Preset.BOARD_11_2);
        board12 = BoardFactory.create(BoardFactory.Preset.BOARD_12);
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
        assertFalse(board11a.getBlock(2,0).isPresent(), "1. Error with a non present block in BOARD_11_1");
        assertFalse(board11a.getBlock(-1, 2).isPresent(), "2. Error in bound exception.");
        assertFalse(board11a.getBlock(0,4).isPresent(), "3. Error in bound exception.");
        assertEquals(block_11a_1_1, board11a.getBlock(1,1), "4. Error in picking block in BOARD_11_1.");
        assertEquals(block_11b_1_1, board11b.getBlock(1,1), "5. Error in picking block in BOARD_11_2.");

    }

    @Test
    void getBlockNeighbor() {
        //Tests on BOARD_10
        Optional<Block> block_10_0_1 = board10.getBlock(0,1);
        Optional<Block> block_10_2_3 = board10.getBlock(2,3);
        assertFalse(board10.getBlockNeighbor(board10.getBlock(0, 0).get(), Direction.NORTH).isPresent(), "1. Error in bound exception.");
        assertEquals(block_10_0_1, board10.getBlockNeighbor(board10.getBlock(0, 0).get(), Direction.EAST), "2. Error in EAST direction picking with BOARD_10.");
        assertFalse(board10.getBlockNeighbor(board10.getBlock(0,2).get(), Direction.EAST).isPresent(), "3. Error in a non present block in BOARD_10.");
        assertEquals(block_10_2_3, board10.getBlockNeighbor(board10.getBlock(1,3).get(), Direction.SOUTH), "4. Error in SOUTH direction picking with BOARD_10.");
        //Tests on Board_12
        Optional<Block> block_12_0_3 = board12.getBlock(0,3);
        Optional<Block> block_12_1_0 = board12.getBlock(1,0);
        assertEquals(block_12_0_3, board12.getBlockNeighbor(board12.getBlock(0,2).get(), Direction.EAST), "5. Error in EAST direction picking with BOARD_12.");
        assertEquals(block_12_1_0, board12.getBlockNeighbor(board12.getBlock(0,0).get(), Direction.SOUTH), "6. Error in SOUTH direction picking with BOARD_12.");
        assertFalse(board12.getBlockNeighbor(board12.getBlock(0,2).get(), Direction.NORTH).isPresent(), "7. Error in bound exception.");
        assertFalse(board12.getBlockNeighbor(board12.getBlock(0,0).get(), Direction.WEST).isPresent(), "8. Error in bound exception.");
    }

    @Test
    void getVisibleBlocks() {

    }

    @Test
    void getRoom() {
        Optional<Block> block_10_2_1 = board10.getBlock(2,1);
        Optional<Block> block_10_2_2 = board10.getBlock(2,2);
        Set<Block> roomWhite10 = new HashSet<>();
        roomWhite10.add(block_10_2_1.get());
        roomWhite10.add(block_10_2_2.get());
        assertEquals(roomWhite10, board10.getRoom(block_10_2_1.get()), "1. Error in getting white room in BOARD 10 part1of2.");
        assertEquals(roomWhite10, board10.getRoom(block_10_2_2.get()), "2. Error in getting white room in BOARD 10 part2of2.");

        Optional<Block> block_11a_0_3 = board11a.getBlock(0,3);
        Set<Block> roomGreen11a = new HashSet<>();
        roomGreen11a.add(block_11a_0_3.get());
        assertEquals(roomGreen11a, board11a.getRoom(block_11a_0_3.get()), "3. Error in getting green room in BOARD_11_1.");


        Optional<Block> block_11a_1_2 = board11a.getBlock(1,2);
        Optional<Block> block_11a_1_3 = board11a.getBlock(1,3);
        Optional<Block> block_11a_2_2 = board11a.getBlock(2,2);
        Optional<Block> block_11a_2_3 = board11a.getBlock(2,3);
        Set<Block> roomYellow11a = new HashSet<>();
        roomYellow11a.add(block_11a_1_2.get());
        roomYellow11a.add(block_11a_1_3.get());
        roomYellow11a.add(block_11a_2_2.get());
        roomYellow11a.add(block_11a_2_3.get());
        Set<Block> actual = board11a.getRoom(block_11a_1_2.get());
        assertEquals(roomYellow11a, actual, "4. Error in getting yellow room in BOARD_11_1.");

    }

    @Test
    void getRow() {
        Optional<Block> block_10_0_0 = board10.getBlock(0,0);
        Optional<Block> block_10_0_1 = board10.getBlock(0,1);
        Optional<Block> block_10_0_2 = board10.getBlock(0,2);
        List<Block> row_10_0 = new ArrayList<>();
        row_10_0.add(block_10_0_0.get());
        row_10_0.add(block_10_0_1.get());
        row_10_0.add(block_10_0_2.get());
        assertEquals(row_10_0, board10.getRow(block_10_0_0.get()));
        assertEquals(row_10_0, board10.getRow(block_10_0_1.get()));

        Optional<Block> block_10_1_0 = board10.getBlock(1,0);
        Optional<Block> block_10_1_1 = board10.getBlock(1,1);
        Optional<Block> block_10_1_2 = board10.getBlock(1,2);
        Optional<Block> block_10_1_3 = board10.getBlock(1,3);
        List<Block> row_10_1 = new ArrayList<>();
        row_10_1.add(block_10_1_0.get());
        row_10_1.add(block_10_1_1.get());
        row_10_1.add(block_10_1_2.get());
        row_10_1.add(block_10_1_3.get());
        assertEquals(row_10_1, board10.getRow(block_10_1_2.get()));
    }

    @Test
    void getColumn() {
        Optional<Block> block_10_0_0 = board10.getBlock(0,0);
        Optional<Block> block_10_1_0 = board10.getBlock(1,0);
        List<Block> column_10_0 = new ArrayList<>();
        column_10_0.add(block_10_0_0.get());
        column_10_0.add(block_10_1_0.get());
        assertEquals(column_10_0, board10.getColumn(block_10_0_0.get()));
        assertEquals(column_10_0, board10.getColumn(block_10_1_0.get()));

        Optional<Block> block_10_0_1 = board10.getBlock(0,1);
        Optional<Block> block_10_1_1 = board10.getBlock(1,1);
        Optional<Block> block_10_2_1 = board10.getBlock(2,1);
        List<Block> column_10_1 = new ArrayList<>();
        column_10_1.add(block_10_0_1.get());
        column_10_1.add(block_10_1_1.get());
        column_10_1.add(block_10_2_1.get());
        assertEquals(column_10_1, board10.getColumn(block_10_2_1.get()));
    }

    @Test
    void movePlayer() {
        //Testing null block on BOARD_10
        Optional<Block> block_10_0_2 = board10.getBlock(0,2);
        Optional<Block> block_10_1_2 = board10.getBlock(1,2);
        Player player0 = players.get(0);
        block_10_0_2.get().addPlayer(player0);
        assertEquals(1, block_10_0_2.get().getPlayers().size(), "Error: player0 seems not in position");
        board10.movePlayer(player0,Direction.EAST);
        assertEquals(1, block_10_0_2.get().getPlayers().size(), "Error: player0 should not be moved in a null block");
        board10.movePlayer(player0, Direction.SOUTH);
        assertEquals(0, block_10_0_2.get().getPlayers().size(), "Error: player0 should be moved");
        assertEquals(1, block_10_1_2.get().getPlayers().size(), "Error: player0 should be moved here!");
        block_10_1_2.get().removePlayer(player0);
        //Testing boarder on BOARD_10
        Optional<Block> block_10_1_3 = board10.getBlock(1,3);
        Optional<Block> block_10_2_3 = board10.getBlock(2,3);
        block_10_1_3.get().addPlayer(player0);
        assertEquals(1, block_10_1_3.get().getPlayers().size(), "Error: player0 seems not in position");
        board10.movePlayer(player0,Direction.EAST);
        assertEquals(1, block_10_1_3.get().getPlayers().size(), "Error: player0 should not be moved over the boarder");
        board10.movePlayer(player0, Direction.SOUTH);
        assertEquals(0, block_10_1_3.get().getPlayers().size(), "Error: player0 should be moved");
        assertEquals(1, block_10_2_3.get().getPlayers().size(), "Error: player0 should be moved here!");
        block_10_2_3.get().removePlayer(player0);
        //Testing multiple players on BOARD_10
        assertEquals(0, block_10_2_3.get().getPlayers().size());
        for (Player i : players){
            block_10_2_3.get().addPlayer(i);
        }
        assertEquals(players.size(), block_10_2_3.get().getPlayers().size(), "Scenario not well configured");
        int x = 3;
        board10.movePlayer(players.get(x), Direction.NORTH);
        List<Player> assertion = new LinkedList<>();
        for (int i = 0; i < players.size(); i++){
            if (i != x) {
                assertion.add(players.get(i));
            }
        }
        assertEquals(assertion, block_10_2_3.get().getPlayers());
        assertEquals(1, block_10_1_3.get().getPlayers().size());
        assertEquals(players.get(x), block_10_1_3.get().getPlayers().get(0));
        board10.movePlayer(players.get(x), Direction.SOUTH);
        assertEquals(players.size(), block_10_2_3.get().getPlayers().size());
        for (Player i : players){
            block_10_2_3.get().removePlayer(i);
        }
        assertEquals(0, block_10_2_3.get().getPlayers().size());
    }

    @Test
    void findPlayer() {
        int i = 4;
        int k = 0;
        //Trying to find a single player on the board
        Optional<Block> block0 = board11a.findPlayer(players.get(0));
        assertEquals(Optional.empty(), block0);
        Optional<Block> block_12_2_2 = board12.getBlock(2,2);
        block_12_2_2.get().addPlayer(players.get(i));
        assertEquals(block_12_2_2, board12.findPlayer(players.get(i)));
        //Trying to find a player not alone in the board
        Optional<Block> block_12_0_0 = board12.getBlock(0,0);
        block_12_0_0.get().addPlayer(players.get(k));
        assertEquals(block_12_2_2, board12.findPlayer(players.get(i)));
        block_12_0_0.get().removePlayer(players.get(k));
        //testing to find 2nd player on block
        block_12_2_2.get().addPlayer(players.get(k));
        assertEquals(block_12_2_2, board12.findPlayer(players.get(k)));
        block_12_2_2.get().removePlayer(players.get(k));
        block_12_2_2.get().removePlayer(players.get(i));
        //testing illegal state (2 times same player on board)
        Player playerUbiquitous = players.get(3);
        block_12_2_2.get().addPlayer(playerUbiquitous);
        block_12_0_0.get().addPlayer(playerUbiquitous);
        try {
            board12.findPlayer(playerUbiquitous);
            fail();
        } catch (IllegalStateException e){
            assertEquals(null, e.getMessage());
        }
        block_12_2_2.get().removePlayer(playerUbiquitous);
        block_12_2_2.get().removePlayer(playerUbiquitous);
    }


    @Test
    void teleportPlayer() {
        int i = 2;
        int k = 1;
        int j = 0;
        //Trying to teleport a single player on the board
        Optional<Block> block_12_2_2 = board12.getBlock(2,2);
        Optional<Block> block_12_0_0 = board12.getBlock(0,0);
        block_12_2_2.get().addPlayer(players.get(i));
        board12.teleportPlayer(players.get(i), block_12_0_0.get());
        assertEquals(block_12_0_0, board12.findPlayer(players.get(i)));
        //Trying to teleport a player not alone in the board
        block_12_2_2.get().addPlayer(players.get(k));
        board12.teleportPlayer(players.get(i), block_12_2_2.get());
        assertEquals(block_12_2_2, board12.findPlayer(players.get(i)));
        assertEquals(block_12_2_2, board12.findPlayer(players.get(k)));
        //testing illegal state (player non present on board)
        try {
            board12.teleportPlayer(players.get(j), block_12_0_0.get());
            fail();
        } catch (NullPointerException e){
            assertEquals(null, e.getMessage());
        }
        block_12_2_2.get().removePlayer(players.get(i));
        block_12_2_2.get().removePlayer(players.get(k));
    }
}