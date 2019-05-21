package it.polimi.ingsw.client.io.listeners;

import it.polimi.ingsw.shared.events.networkevents.BonusTileEvent;
import it.polimi.ingsw.shared.events.networkevents.PlayerMoved;
import it.polimi.ingsw.shared.events.networkevents.PlayerWeaponExchanged;
import it.polimi.ingsw.shared.events.networkevents.WeaponEvent;

public interface BoardListener {
    void onPlayerMoved(PlayerMoved e);

    void onPlayerTeleported(PlayerMoved e);

    void onNewWeaponAvailable(WeaponEvent e);

    void onBonusTileGrabbed(BonusTileEvent e);

    void onBonusTileDropped(BonusTileEvent e);
}
