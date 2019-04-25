package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.player.Player;

import java.util.EventObject;

public class PlayerDamaged extends EventObject {

    private Player attacker;

    /**
     *
     * @param victim the source object
     */
    public PlayerDamaged(Player victim, Player attacker) {
        super(victim);
        this.attacker = attacker;
    }

    public Player getAttacker(){
        return attacker;
    }

    public Player getVictim(){
        return (Player) this.getSource();
    }
}
