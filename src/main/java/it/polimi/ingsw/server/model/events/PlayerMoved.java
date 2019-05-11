package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.player.Player;

import java.util.EventObject;

public class PlayerMoved extends BoardEvent {

    private final Player player;
    private final Block destination;

    public PlayerMoved(Board board, Player player, Block destination) {
        super(board);
        this.player = player;
        this.destination = destination;
    }

    public Player getPlayer() {
        return player;
    }

    public Block getDestination() {
        return destination;
    }
}
