package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.shared.datatransferobjects.Player;

/**
 * Network event carrying information about a spawned player
 *
 * @author Adriana Ferrari
 */
public class PlayerSpawned extends PlayerEvent {

    /**
     * The row where the player spawned
     */
    private final int row;

    /**
     * The column where the player spawned
     */
    private final int column;

    /**
     * Constructs a player spawned event
     *
     * @param player the player who spawned
     * @param row the row where the player spawned
     * @param column the column where the player spawned
     */
    public PlayerSpawned(Player player, int row, int column) {
        super(player);
        this.row = row;
        this.column = column;
    }

    /**
     * @return the row where the player spawned
     */
    public int getRow() {
        return row;
    }

    /**
     * @return the column where the player spawned
     */
    public int getColumn() {
        return column;
    }
}
