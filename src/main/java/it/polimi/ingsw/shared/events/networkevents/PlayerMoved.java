package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.shared.viewmodels.Player;

public class PlayerMoved extends PlayerEvent {

    private final int row;
    private final int column;

    public PlayerMoved(Player player, int row, int column) {
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
