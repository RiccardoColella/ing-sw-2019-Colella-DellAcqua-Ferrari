package it.polimi.ingsw.server.model.match;

import it.polimi.ingsw.server.model.player.DamageToken;

public class Killshot {
    private final DamageToken damageToken;
    private boolean overkill;

    public Killshot(DamageToken damageToken, boolean overkill) {
        this.damageToken = damageToken;
        this.overkill = overkill;
    }

    public Killshot(DamageToken damageToken) {
        this(damageToken, false);
    }

    public DamageToken getDamageToken() {
        return damageToken;
    }

    public boolean isOverkill() {
        return overkill;
    }

    public void markAsOverkill() {
        this.overkill = true;
    }
}
