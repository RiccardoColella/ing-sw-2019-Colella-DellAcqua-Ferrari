package it.polimi.ingsw.server.model.weapons;

import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.exceptions.MissingOwnershipException;

import java.util.List;

/**
 * This class inherits from the basic weapon and adds an alternative attack that can be used instead of the basic one
 */
public class WeaponWithAlternative extends Weapon {

    /**
     * This property represents the alternative attack of the weapon, which can be used in place of the basic attack
     */
    private final Attack alternativeAttack;
    /**
     * This property represents the cost that shall be paid to use the alternative attack, if the list is empty the attack is free
     */
    private final List<Coin> alternativeAttackCost;

    /**
     * This constructor assignes all the final values to the weapon, making it ready to be bought
     *
     * @param name                  the name of the weapon
     * @param basicAttack           the basic attack of the weapon
     * @param acquisitionCost       a list of coin equal to the acquisition cost of the weapon
     * @param reloadCost            a list of coin equal to the reload cost of the weapon
     * @param alternativeAttack     the alternative attack of the weapon
     * @param alternativeAttackCost a list of coin equal to the cost of the alternative attack
     */
    public WeaponWithAlternative(Name name, Attack basicAttack, List<Coin> acquisitionCost, List<Coin> reloadCost, Attack alternativeAttack, List<Coin> alternativeAttackCost) {
        super(name, basicAttack, acquisitionCost, reloadCost);
        this.alternativeAttack = alternativeAttack;
        this.alternativeAttackCost = alternativeAttackCost;
    }

    /**
     * This method gets the alternative attack of the weapon
     * @return the alternative attack of the weapon
     */
    public Attack getAlternativeAttack() {
        return this.alternativeAttack;
    }

    /**
     * This method gets the cost of the alternative attack of the weapon
     * @return the cost of the alternative attack of the weapon
     */
    public List<Coin> getAlternativeAttackCost() {
        return this.alternativeAttackCost;
    }

    @Override
    protected boolean hasAttack(Attack attack) {
        return super.hasAttack(attack) || this.alternativeAttack.equals(attack);
    }
}
