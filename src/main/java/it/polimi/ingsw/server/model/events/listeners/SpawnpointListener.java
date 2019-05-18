package it.polimi.ingsw.server.model.events.listeners;

import it.polimi.ingsw.server.model.events.WeaponEvent;

import java.util.EventListener;

public interface SpawnpointListener extends EventListener {

    void onWeaponDropped(WeaponEvent e);
}
