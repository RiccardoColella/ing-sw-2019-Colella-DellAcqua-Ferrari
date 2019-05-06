package it.polimi.ingsw.shared.events.listeners;

import it.polimi.ingsw.shared.events.MatchStarted;

public interface MatchListener {
    void onMatchStarted(MatchStarted e);
}
