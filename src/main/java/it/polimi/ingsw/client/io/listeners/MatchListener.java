package it.polimi.ingsw.client.io.listeners;

import it.polimi.ingsw.shared.events.networkevents.*;

/**
 * Interface shared between client and server of a class that will react to the status changes of the match
 */
public interface MatchListener {

    /**
     * This method is called when the match starts
     * @param e the event corresponding to the match starting
     */
    void onMatchStarted(MatchStarted e);

    /**
     * This method is called when the match mode changes
     * @param e the event corresponding to the change of the match mode
     */
    void onMatchModeChanged(MatchModeChanged e);

    /**
     * This method is called when the killshot track changes
     * @param e the event corresponding to the change of the killshot track
     */
    void onKillshotTrackChanged(KillshotTrackChanged e);

    /**
     * This method is called when the match ends
     * @param e the event corresponding to the end of the match
     */
    void onMatchEnded(MatchEnded e);

    /**
     * This method is called when the match is resumed
     * @param e the event corresponding to the match being resumed
     */
    void onMatchResumed(MatchResumed e);
}
