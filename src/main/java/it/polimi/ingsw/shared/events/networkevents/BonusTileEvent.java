package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.shared.datatransferobjects.BonusTile;

public class BonusTileEvent extends NetworkEvent {

    private final BonusTile bonusTile;

    public BonusTileEvent(BonusTile bonusTile) {
        this.bonusTile = bonusTile;
    }

    public BonusTile getBonusTile() {
        return bonusTile;
    }
}
