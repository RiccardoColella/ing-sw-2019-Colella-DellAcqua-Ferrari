package it.polimi.ingsw.client.io.listeners;

import it.polimi.ingsw.shared.events.networkevents.KillshotTrackChanged;
import it.polimi.ingsw.shared.events.networkevents.MatchEnded;
import it.polimi.ingsw.shared.events.networkevents.MatchModeChanged;
import it.polimi.ingsw.shared.events.networkevents.MatchStarted;

public interface MatchListener {
    void onMatchStarted(MatchStarted e);

    void onMatchModeChanged(MatchModeChanged e);

    void onKillshotTrackChanged(KillshotTrackChanged e);

    void onMatchEnded(MatchEnded e);
}
