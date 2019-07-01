package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.shared.datatransferobjects.Player;

/**
 * Network event carrying information about a spawned player
 *
 * @author Carlo Dell'Acqua
 */
public class PlayerMoved extends PlayerEvent {

    /**
     * The row on the board where the player moved
     */
    private final int row;
    /**
     * The column on the board where the player moved
     */
    private final int column;

    /**
     * Constructs a player moved event
     *
     * @param player the player who moved
     * @param row the row on the board where the player moved
     * @param column the column on the board where the player moved
     */
    public PlayerMoved(Player player, int row, int column) {
        super(player);
        this.row = row;
        this.column = column;
    }

    /**
     * @return the row on the board where the player moved
     */
    public int getRow() {
        return row;
    }

    /**
     * @return the column on the board where the player moved
     */
    public int getColumn() {
        return column;
    }
}
