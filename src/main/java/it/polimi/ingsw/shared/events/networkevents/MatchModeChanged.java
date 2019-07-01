package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.server.model.match.Match;

/**
 * Network event carrying information about a match mode
 *
 * @author Carlo Dell'Acqua
 */
public class MatchModeChanged extends NetworkEvent {

    /**
     * The new match mode
     */
    private final Match.Mode mode;

    /**
     * Constructs the match mode changed event
     *
     * @param mode the new match mode
     */
    public MatchModeChanged(Match.Mode mode) {
        this.mode = mode;
    }

    /**
     * @return the new match mode
     */
    public Match.Mode getMode() {
        return mode;
    }
}
