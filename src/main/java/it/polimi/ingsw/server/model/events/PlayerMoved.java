package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.player.Player;

/**
 * Event fired when a player moves on the board
 */
public class PlayerMoved extends BoardEvent {

    /**
     * The player who moved
     */
    private final Player player;
    /**
     * The new location of the player
     */
    private final Block destination;

    /**
     * Constructs a player moved event
     *
     * @param board the board on which the player has moved
     * @param player the player who moved
     * @param destination the new location of the player
     */
    public PlayerMoved(Board board, Player player, Block destination) {
        super(board);
        this.player = player;
        this.destination = destination;
    }

    /**
     * @return the player who moved
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return the new location of the player
     */
    public Block getDestination() {
        return destination;
    }
}
