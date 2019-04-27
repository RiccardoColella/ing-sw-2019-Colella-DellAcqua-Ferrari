package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.weapons.Weapon;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.shared.messages.ClientApi;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class inherits from the basic weapon and adds an alternative attack that can be used instead of the basic one
 */
public class WeaponWithAlternative extends BasicWeapon {

    /**
     * This property represents the alternative attack of the weapon, which can be used in place of the basic attack
     */
    protected final Attack alternativeAttack;

    /**
     * This constructor assignes all the final values to the weapon, making it ready to be bought
     *
     * @param name                  the name of the weapon
     * @param basicAttack           the basic attack of the weapon
     * @param alternativeAttack     the alternative attack of the weapon
     */
    public WeaponWithAlternative(String name, Attack basicAttack, Attack alternativeAttack) {
        super(name, basicAttack);
        this.alternativeAttack = alternativeAttack;
    }

    @Override
    public void shoot(Interviewer interviewer, Player activePlayer) {
        Attack selectedAttack;
        availableAttacks.clear();
        executedAttacks.clear();
        previouslyHit.clear();
        currentShooter = activePlayer;
        fixedDirection = null;

        if (canAffordAttack(basicAttack) && canDoFirstAction(basicAttack)) {
            availableAttacks.add(basicAttack);
        }
        if (canAffordAttack(alternativeAttack) && canDoFirstAction(alternativeAttack)) {
            availableAttacks.add(alternativeAttack);
        }
        if (availableAttacks.size() == 2) {
            List<String> potentialAttacks = availableAttacks.stream().map(Attack::getName).collect(Collectors.toList());
            String selectedAttackName = interviewer.select("Select the weapon mode", potentialAttacks, ClientApi.ATTACK_QUESTION);
            if (selectedAttackName.equals(basicAttack.getName())) {
                selectedAttack = basicAttack;
            } else {
                selectedAttack = alternativeAttack;
            }
        } else if (availableAttacks.size() == 1) {
            selectedAttack = availableAttacks.get(0);
        } else throw new IllegalStateException("No attacks available");
        handlePayment(interviewer, selectedAttack, activePlayer);
        executedAttacks.add(selectedAttack);
        selectedAttack.execute(interviewer, this);
        availableAttacks.remove(selectedAttack);
    }

    @Override
    public boolean hasAvailableAttacks(Player activePlayer) {
        currentShooter = activePlayer;
        return (canAffordAttack(basicAttack) && canDoFirstAction(basicAttack))
                ||
               (canAffordAttack(alternativeAttack) && canDoFirstAction(alternativeAttack));
    }
}
