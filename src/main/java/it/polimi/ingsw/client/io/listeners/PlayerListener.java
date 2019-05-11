package it.polimi.ingsw.client.io.listeners;

import it.polimi.ingsw.shared.events.networkevents.*;

public interface PlayerListener {
    void onPlayerDied(PlayerEvent e);

    void onPlayerReborn(PlayerEvent e);

    void onPlayerWalletChanged(PlayerWalletChanged e);

    void onPlayerBoardFlipped(PlayerEvent e);

    void onPlayerHealthChanged(PlayerHealthChanged e);

    void onWeaponReloaded(it.polimi.ingsw.shared.events.networkevents.WeaponEvent e);

    void onWeaponUnloaded(it.polimi.ingsw.shared.events.networkevents.WeaponEvent e);

    void onWeaponPicked(WeaponExchanged e);

    void onWeaponDropped(WeaponExchanged e);

    void onPlayerDisconnected(PlayerEvent e);

    void onPlayerReconnected(PlayerEvent e);
}
