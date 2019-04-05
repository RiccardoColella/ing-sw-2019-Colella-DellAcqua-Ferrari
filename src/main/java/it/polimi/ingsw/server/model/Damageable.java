package it.polimi.ingsw.server.model;

import java.util.List;

/**
 * This interface schematizes all entities that can receive damage
 */
public interface Damageable {

    /**
     * This method returns the damage tokens received so far by this Damageable
     * @return a list containing the DamageToken received so far, the list will be empty if there are none
     */
    List<DamageToken> getDamageTokens();

    /**
     * This method assigns damage tokens to a Damageable
     *
     * @param damageTokens a list of DamageToken that should be given to the Damageable
     */
    void addDamageTokens(List<DamageToken> damageTokens);


    /**
     * This method assigns a single damage token to a Damageable
     *
     * @param damageToken the DamageToken that should be given to the Damageable
     */
    void addDamageToken(DamageToken damageToken);

}
