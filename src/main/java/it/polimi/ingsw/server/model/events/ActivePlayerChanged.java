package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.shared.datatransferobjects.Player;

public class ActivePlayerChanged extends MatchEvent {
    private final Player activePlayer;

    public ActivePlayerChanged(Match source, Player activePlayer) {
        super(source);
        this.activePlayer = activePlayer;
    }

    public Player getActivePlayer() {
        return activePlayer;
    }
}
