package it.polimi.ingsw.client.io.listeners;

import it.polimi.ingsw.shared.events.networkevents.*;

public interface MatchListener {
    void onMatchStarted(MatchStarted e);

    void onMatchModeChanged(MatchModeChanged e);

    void onKillshotTrackChanged(KillshotTrackChanged e);

    void onMatchEnded(MatchEnded e);

    void onMatchResumed(MatchResumed e);
}
