package it.polimi.ingsw.server.model.weapons;

import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.exceptions.MissingOwnershipException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class inherits from the basic weapon and it also offers powered attacks that can be used together with the basic attack
 */
public class WeaponWithMultipleEffects extends Weapon {

    /**
     * This property maps each powered attack to its cost
     */
    private final Map<Attack, List<Coin>> poweredAttacks;

    /**
     * This property states whether the attacks can be used in any order or not
     */
    private final boolean mustExecuteInOrder;

    /**
     * This constructor assignes all the final values to the weapon, making it ready to be bought
     *
     * @param name                  the name of the weapon
     * @param basicAttack           the basic attack of the weapon
     * @param acquisitionCost       a list of coin equal to the acquisition cost of the weapon
     * @param reloadCost            a list of coin equal to the reload cost of the weapon
     * @param poweredAttacks        a non-empty map of the powered attacks of the weapon with their relative cost
     * @param mustExecuteInOrder    true if the effects can only be used in the given order and with basic effect first
     */
    public WeaponWithMultipleEffects(Name name, Attack basicAttack, List<Coin> acquisitionCost, List<Coin> reloadCost, Map<Attack, List<Coin>> poweredAttacks, boolean mustExecuteInOrder) {
        super(name, basicAttack, acquisitionCost, reloadCost);
        this.poweredAttacks = poweredAttacks;
        this.mustExecuteInOrder = mustExecuteInOrder;
    }

    /**
     * This method gets a list with all the possible powered attack of the weapon
     * @return a list with the powered attacks of the weapon
     */
    public List<Attack> getPoweredAttacks() {
        return new ArrayList<>(this.poweredAttacks.keySet());
    }

    /**
     * This method gets the cost of the given attack
     * @param attack the attack you want to know the cost of
     * @return the cost of the given attack, null if the weapon does not include it
     */
    public List<Coin> getAttackCost(Attack attack) {
        if (hasAttack(attack)) {
            return poweredAttacks.get(attack);
        } else {
            throw new MissingOwnershipException("Attack " + attack + " does not belong to this weapon");
        }
    }

    /**
     * This method tells whether the powered attacks and the basic attack can be used in any order
     * @return true if there is a fixed order for the attacks, otherwise false
     */
    public boolean mustExecuteInOrder() {
        return this.mustExecuteInOrder;
    }

    @Override
    protected boolean hasAttack(Attack attack) {
        return super.hasAttack(attack) || this.poweredAttacks.containsKey(attack);
    }
}