package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.currency.BonusTile;
import it.polimi.ingsw.server.model.weapons.WeaponTile;

import java.util.EventObject;

public class BonusTileEvent extends EventObject {

    private final BonusTile bonusTile;

    public BonusTileEvent(Object source, BonusTile bonusTile) {
        super(source);
        this.bonusTile = bonusTile;
    }

    public BonusTile getBonusTile() {
        return bonusTile;
    }
}
