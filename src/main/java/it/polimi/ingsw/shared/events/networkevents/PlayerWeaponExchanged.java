package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.shared.datatransferobjects.Player;

public class PlayerWeaponExchanged extends PlayerWeaponEvent {

    private final int row;
    private final int column;

    public PlayerWeaponExchanged(Player owner, String weaponName, int row, int column) {
        super(owner, weaponName);
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }
}
