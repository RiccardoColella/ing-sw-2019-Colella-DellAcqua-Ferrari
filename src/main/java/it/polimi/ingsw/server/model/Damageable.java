package it.polimi.ingsw.server.model;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Damageable {

    /**
     * This method returns the damage tokens received so far by this Damageable
     * @return a list containing the DamageToken received so far, the list will be empty if there are none
     */
    List<DamageToken> getDamageTokens();

    /**
     * This method assigns damage tokens to a Damageable
     * @param damageTokens a list of DamageToken that should be given to the Damageable
     */
    void addDamageTokens(@NotNull List<DamageToken> damageTokens);

}
