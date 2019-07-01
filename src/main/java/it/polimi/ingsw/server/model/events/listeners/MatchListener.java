package it.polimi.ingsw.server.model.events.listeners;

import it.polimi.ingsw.server.model.events.*;

import java.util.EventListener;

/**
 * Interface of a class that will react to the status changes of the match
 */
public interface MatchListener extends EventListener {

    /**
     * This method is called when the match ends
     *
     * @param event the event corresponding to the end of the match
     */
    void onMatchEnded(MatchEnded event);

    /**
     * This method is called when the match mode changes
     *
     * @param event the event corresponding to the match mode change
     */
    void onMatchModeChanged(MatchModeChanged event);

    /**
     * This method is called when the match starts
     *
     * @param event the event corresponding to the beginning of the match
     */
    void onMatchStarted(MatchEvent event);

    /**
     * This method is called when the killshot track changes
     *
     * @param e the event corresponding to the killshot track change
     */
    void onKillshotTrackChanged(KillshotTrackChanged e);

    /**
     * This method is called when the turn changes
     *
     * @param e the event corresponding to the changing of the turn
     */
    void onActivePlayerChanged(PlayerEvent e);
}
