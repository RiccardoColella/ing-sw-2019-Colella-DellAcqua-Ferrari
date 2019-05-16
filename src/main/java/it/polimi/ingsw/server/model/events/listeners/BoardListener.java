package it.polimi.ingsw.server.model.events.listeners;

import it.polimi.ingsw.server.model.events.NewWeaponAvailable;
import it.polimi.ingsw.server.model.events.PlayerMoved;

import java.util.EventListener;

public interface BoardListener extends EventListener {

    void onPlayerTeleported(PlayerMoved e);

    void onPlayerMoved(PlayerMoved e);

    void onNewWeaponAvailable(NewWeaponAvailable e);
}
