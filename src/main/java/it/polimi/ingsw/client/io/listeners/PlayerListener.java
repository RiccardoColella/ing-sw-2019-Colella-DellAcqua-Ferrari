package it.polimi.ingsw.client.io.listeners;

import it.polimi.ingsw.shared.events.networkevents.*;

public interface PlayerListener {
    void onPlayerDied(PlayerEvent e);

    void onPlayerReborn(PlayerEvent e);

    void onPlayerWalletChanged(PlayerWalletChanged e);

    void onPlayerBoardFlipped(PlayerEvent e);

    void onPlayerTileFlipped(PlayerEvent e);

    void onPlayerHealthChanged(PlayerHealthChanged e);

    void onWeaponReloaded(PlayerWeaponEvent e);

    void onWeaponUnloaded(PlayerWeaponEvent e);

    void onWeaponPicked(PlayerWeaponExchanged e);

    void onWeaponDropped(PlayerWeaponExchanged e);

    void onPlayerReconnected(PlayerEvent e);

    void onPlayerSpawned(PlayerSpawned e);

    void onPlayerOverkilled(PlayerEvent e);

    void onActivePlayerChanged(PlayerEvent e);
}
