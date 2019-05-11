package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.player.Player;

import java.util.EventObject;

public class BoardEvent extends EventObject {

    public BoardEvent(Board board) {
        super(board);
    }

    public Board getBoard() {
        return (Board)source;
    }
}
