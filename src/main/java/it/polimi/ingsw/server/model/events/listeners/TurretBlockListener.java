package it.polimi.ingsw.server.model.events.listeners;

import it.polimi.ingsw.server.model.events.BonusTileEvent;
import it.polimi.ingsw.server.model.events.WeaponEvent;

import java.util.EventListener;

public interface TurretBlockListener extends EventListener {

    void onBonusTileDropped(BonusTileEvent e);

    void onBonusTileGrabbed(BonusTileEvent e);
}
