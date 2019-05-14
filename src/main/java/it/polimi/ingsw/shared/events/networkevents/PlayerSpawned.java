package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.shared.viewmodels.Player;

public class PlayerSpawned extends PlayerEvent {

    private final int row;
    private final int column;

    public PlayerSpawned(Player player, int row, int column) {
        super(player);
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
