package it.polimi.ingsw.server.model.match;

import it.polimi.ingsw.server.model.player.DamageToken;

/**
 * Represents a killshot
 */
public class Killshot {
    /**
     * Damage token associated with the killshot
     */
    private final DamageToken damageToken;
    /**
     * Whether or not the killshot was an overkill
     */
    private boolean overkill;

    /**
     * Creates a killshot
     *
     * @param damageToken damage token associated with the killshot
     * @param overkill whether or not the killshot was an overkill
     */
    public Killshot(DamageToken damageToken, boolean overkill) {
        this.damageToken = damageToken;
        this.overkill = overkill;
    }

    /**
     * Creates a killshot, the overkill values is defaulted to false
     *
     * @param damageToken damage token associated with the killshot
     */
    public Killshot(DamageToken damageToken) {
        this(damageToken, false);
    }

    /**
     * @return the damage token associated with the killshot
     */
    public DamageToken getDamageToken() {
        return damageToken;
    }

    /**
     * @return true if the killshot is an overkill
     */
    public boolean isOverkill() {
        return overkill;
    }

    /**
     * Mark this killshot as an overkill
     */
    public void markAsOverkill() {
        this.overkill = true;
    }
}
