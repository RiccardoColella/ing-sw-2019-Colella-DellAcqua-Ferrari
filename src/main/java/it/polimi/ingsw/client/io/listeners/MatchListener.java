package it.polimi.ingsw.client.io.listeners;

import it.polimi.ingsw.shared.events.MatchStarted;

public interface MatchListener {
    void onMatchStarted(MatchStarted e);
}
