package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.shared.datatransferobjects.Player;

/**
 * Network event carrying information about a player's weapon
 *
 * @author Carlo Dell'Acqua
 */
public class PlayerWeaponExchanged extends PlayerWeaponEvent {

    /**
     * The row on the board where this event occurred
     */
    private final int row;
    /**
     * The column on the board where this event occurred
     */
    private final int column;


    /**
     * Constructs a weapon event
     *
     * @param owner the player who owns the weapon
     * @param row the row on the board where the event occurred
     * @param column the column on the board where the event occurred
     */
    public PlayerWeaponExchanged(Player owner, String weaponName, int row, int column) {
        super(owner, weaponName);
        this.row = row;
        this.column = column;
    }

    /**
     * @return the row on the board
     */
    public int getRow() {
        return row;
    }

    /**
     * @return the column on the board
     */
    public int getColumn() {
        return column;
    }
}
