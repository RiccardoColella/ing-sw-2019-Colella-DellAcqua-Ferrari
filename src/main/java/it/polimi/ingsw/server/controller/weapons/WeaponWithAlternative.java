package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.weapons.Weapon;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.utils.Tuple;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class inherits from the basic weapon and adds an alternative attack that can be used instead of the basic one
 */
public class WeaponWithAlternative extends BasicWeapon {

    /**
     * This property represents the alternative attack of the weapon, which can be used in place of the basic attack
     */
    private final Attack alternativeAttack;

    /**
     * This constructor assignes all the final values to the weapon, making it ready to be bought
     *
     * @param name                  the name of the weapon
     * @param basicAttack           the basic attack of the weapon
     * @param alternativeAttack     the alternative attack of the weapon
     */
    public WeaponWithAlternative(Weapon.Name name, Attack basicAttack, Attack alternativeAttack) {
        super(name, basicAttack);
        this.alternativeAttack = alternativeAttack;
    }

    /**
     * This method gets the alternative attack of the weapon
     * @return the alternative attack of the weapon
     */
    public Attack getAlternativeAttack() {
        return this.alternativeAttack;
    }


    @Override
    protected boolean hasAttack(Attack attack) {
        return super.hasAttack(attack) || this.alternativeAttack.equals(attack);
    }

    @Override
    public void shoot(Interviewer interviewer, Player activePlayer) {
        Attack selectedAttack;
        availableAttacks.clear();
        if (canAffordAttack(basicAttack) && canDoFirstAction(basicAttack)) {
            availableAttacks.add(basicAttack);
        }
        if (canAffordAttack(alternativeAttack) && canDoFirstAction(alternativeAttack)) {
            availableAttacks.add(alternativeAttack);
        }
        if (availableAttacks.size() == 2) {
            List<Tuple<String, List<String>>> potentialAttacks = new LinkedList<>();
            for (Attack attack : availableAttacks) {
                potentialAttacks.add(new Tuple<>(attack.getName(), attack.getCost().stream().map(Object::toString).collect(Collectors.toList())));
            }
            String selectedAttackName = interviewer.select(potentialAttacks).getItem1();
            if (selectedAttackName.equals(basicAttack.getName())) {
                selectedAttack = basicAttack;
            } else {
                selectedAttack = alternativeAttack;
            }
        } else if (availableAttacks.size() == 1) {
            selectedAttack = availableAttacks.get(0);
        } else throw new IllegalStateException("No attacks available");
        handlePayment(interviewer, selectedAttack, activePlayer);
        selectedAttack.execute(interviewer, this);
    }

    @Override
    public boolean hasAvailableAttacks(Player activePlayer) {
        currentShooter = activePlayer;
        return (canAffordAttack(basicAttack) && canDoFirstAction(basicAttack))
                ||
               (canAffordAttack(alternativeAttack) && canDoFirstAction(alternativeAttack));
    }
}
