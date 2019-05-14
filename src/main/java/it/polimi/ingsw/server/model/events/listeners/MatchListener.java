package it.polimi.ingsw.server.model.events.listeners;

import it.polimi.ingsw.server.model.events.*;

import java.util.EventListener;

public interface MatchListener extends EventListener {


    void onMatchEnded(MatchEnded event);

    void onMatchModeChanged(MatchModeChanged event);

    void onMatchStarted(MatchEvent event);

    void onKillshotTrackChanged(KillshotTrackChanged e);

    void onWeaponDropped(WeaponDropped e);

    void onActivePlayerChanged(PlayerEvent e);
}
