package it.polimi.ingsw.shared.events.networkevents;

/**
 * Network event carrying information about a weapon
 *
 * @author Carlo Dell'Acqua
 */
public class WeaponEvent extends NetworkEvent {

    /**
     * The weapon name
     */
    private final String weaponName;
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
     * @param weaponName the weapon name
     * @param row the row on the board where the event occurred
     * @param column the column on the board where the event occurred
     */
    public WeaponEvent(String weaponName, int row, int column) {
        this.weaponName = weaponName;
        this.row = row;
        this.column = column;
    }

    /**
     * @return the weapon name
     */
    public String getWeaponName() {
        return weaponName;
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
