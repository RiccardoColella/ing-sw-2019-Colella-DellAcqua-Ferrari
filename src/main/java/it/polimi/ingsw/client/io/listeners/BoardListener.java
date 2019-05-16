package it.polimi.ingsw.client.io.listeners;

import it.polimi.ingsw.shared.events.networkevents.PlayerMoved;
import it.polimi.ingsw.shared.events.networkevents.WeaponExchanged;

public interface BoardListener {
    void onPlayerMoved(PlayerMoved e);

    void onPlayerTeleported(PlayerMoved e);

    void onNewWeaponAvailable(WeaponExchanged e);
}
