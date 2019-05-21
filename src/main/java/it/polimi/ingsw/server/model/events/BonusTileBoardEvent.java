package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.currency.BonusTile;

import java.util.EventObject;

public class BonusTileBoardEvent extends EventObject {

    private final BonusTile bonusTile;
    private final Block location;

    public BonusTileBoardEvent(Object source, BonusTile bonusTile, Block location) {
        super(source);
        this.location = location;
        this.bonusTile = bonusTile;
    }

    public BonusTile getBonusTile() {
        return bonusTile;
    }

    public Block getLocation() {
        return location;
    }
}
