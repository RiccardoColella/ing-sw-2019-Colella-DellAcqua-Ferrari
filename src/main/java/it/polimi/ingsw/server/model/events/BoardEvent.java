package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.battlefield.Board;

import java.util.EventObject;

/**
 * Generic event related to the board
 */
public class BoardEvent extends EventObject {

    /**
     * Constructs a board event
     *
     * @param board the board that is firing this event
     */
    public BoardEvent(Board board) {
        super(board);
    }

    /**
     * @return the board that is firing this event
     */
    public Board getBoard() {
        return (Board)source;
    }
}
