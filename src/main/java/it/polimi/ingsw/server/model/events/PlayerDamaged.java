package it.polimi.ingsw.server.model.events;

import it.polimi.ingsw.server.model.player.Player;

/**
 * Event fired when a player gets a damage
 */
public class PlayerDamaged extends PlayerEvent {

    /**
     * The attacker who gave the damage
     */
    private Player attacker;

    /**
     * Constructs a player damaged event
     *
     * @param victim the source object
     * @param attacker the attacker
     */
    public PlayerDamaged(Player victim, Player attacker) {
        super(victim);
        this.attacker = attacker;
    }

    /**
     * @return the attacker
     */
    public Player getAttacker(){
        return attacker;
    }

    /**
     * @return the damaged player
     */
    public Player getVictim(){
        return getPlayer();
    }
}
