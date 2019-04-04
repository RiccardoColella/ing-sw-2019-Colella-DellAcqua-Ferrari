package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.Player;

public class PlayerDied {

    private Player victim;
    private Player killer;
    private boolean wasOverkilled;

    public PlayerDied(Player victim, Player killer, boolean wasOverkilled) {
        this.killer = killer;
        this.victim = victim;
        this.wasOverkilled = wasOverkilled;
    }

    public Player getVictim() {
        return victim;
    }

    public Player getKiller() {
        return killer;
    }

    public boolean wasOverkilled() {
        return wasOverkilled;
    }
}
