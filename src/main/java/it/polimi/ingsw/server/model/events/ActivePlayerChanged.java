package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.shared.datatransferobjects.Player;

/**
 * Event fired when the active player changes
 */
public class ActivePlayerChanged extends MatchEvent {
    /**
     * The new active player
     */
    private final Player activePlayer;

    /**
     * Constructs this event
     *
     * @param source the match which changed the active player
     * @param activePlayer the new active player
     */
    public ActivePlayerChanged(Match source, Player activePlayer) {
        super(source);
        this.activePlayer = activePlayer;
    }

    /**
     * @return the new active player
     */
    public Player getActivePlayer() {
        return activePlayer;
    }
}
