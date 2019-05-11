package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.shared.viewmodels.Player;

public class WeaponExchanged extends WeaponEvent {

    private final int row;
    private final int column;

    public WeaponExchanged(Player owner, String weaponName, int row, int column) {
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
