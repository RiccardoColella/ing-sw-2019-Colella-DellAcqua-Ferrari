package it.polimi.ingsw.server.model.events.listeners;

import it.polimi.ingsw.server.model.events.PlayerDamaged;

import java.awt.*;
import java.util.EventListener;

public interface PlayerDamagedListener extends EventListener {

    void onPlayerDamaged(PlayerDamaged e);
}
