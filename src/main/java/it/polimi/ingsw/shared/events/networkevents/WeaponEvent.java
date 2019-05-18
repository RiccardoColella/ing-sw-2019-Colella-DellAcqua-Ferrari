package it.polimi.ingsw.shared.events.networkevents;

public class WeaponEvent extends NetworkEvent {

    private final String weaponName;
    private final int row;
    private final int column;

    public WeaponEvent(String weaponName, int row, int column) {
        this.weaponName = weaponName;
        this.row = row;
        this.column = column;
    }

    public String getWeaponName() {
        return weaponName;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }
}
