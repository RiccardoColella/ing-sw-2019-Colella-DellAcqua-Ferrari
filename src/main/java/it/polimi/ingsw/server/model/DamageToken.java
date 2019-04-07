package it.polimi.ingsw.server.model;

import it.polimi.ingsw.server.model.player.Player;

/**
 * This class represents the tokens that is received upon being damaged or marked
 */
public class DamageToken {
    /**
     * Player who damages
     */
    private Player attacker;

    /**
     * Class constructor
     * @param attacker the player that inflicted the damage
     */
    public DamageToken(Player attacker) {
        this.attacker = attacker;
    }

    /**
     * Tells the who is attacking
     * @return the attacker
     */
    public Player getAttacker() {
        return attacker;
    }
}
