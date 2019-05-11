package it.polimi.ingsw.client.io.listeners;

import it.polimi.ingsw.shared.events.networkevents.*;

public interface BoardListener {
    void onPlayerMoved(PlayerMoved e);

    void onPlayerTeleported(PlayerMoved e);
}
