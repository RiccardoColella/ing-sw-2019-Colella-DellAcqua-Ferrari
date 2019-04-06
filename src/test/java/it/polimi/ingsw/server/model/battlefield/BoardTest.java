package it.polimi.ingsw.server.model.battlefield;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.factories.BoardFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board10;
    private Board board11a;
    private Board board11b;
    private Board board12;


    @BeforeEach
    void setUp() {
        board10 = BoardFactory.create(BoardFactory.Preset.BOARD_10);
        board11a = BoardFactory.create(BoardFactory.Preset.BOARD_11_1);
        board11b = BoardFactory.create(BoardFactory.Preset.BOARD_11_2);
        board12 = BoardFactory.create(BoardFactory.Preset.BOARD_12);
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
    void movePlayer() {
    }

    @Test
    void findPlayer() {
    }

    @Test
    void teleportPlayer() {
    }
}