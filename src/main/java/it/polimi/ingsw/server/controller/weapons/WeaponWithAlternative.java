package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.shared.messages.ClientApi;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class inherits from the basic weapon and adds an alternative attack that can be used instead of the basic one
 *
 * @author Adriana Ferrari
 */
public class WeaponWithAlternative extends Weapon {

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
        allAttacks = new LinkedList<>();
        this.allAttacks.add(basicAttack);
        this.allAttacks.add(this.alternativeAttack);
        this.allAttacks = Collections.unmodifiableList(allAttacks);
    }

    /**
     * @inheritDoc Weapon
     */
    @Override
    protected void attackSelection() {
        Attack selectedAttack;
        if (executedAttacks.isEmpty()) {
            availableAttacks = allAttacks
                    .stream()
                    .filter(attack -> canAffordAttack(attack) && canExecuteAttack(attack))
                    .collect(Collectors.toList());
            if (availableAttacks.size() == 2) {
                List<String> potentialAttacks = availableAttacks
                        .stream()
                        .map(Attack::getName)
                        .collect(Collectors.toList());
                String selectedAttackName = interviewer.select("Select the weapon mode", potentialAttacks, ClientApi.ATTACK_QUESTION);
                selectedAttack = availableAttacks
                        .stream()
                        .filter(attack -> attack.getName().equals(selectedAttackName))
                        .findAny()
                        .orElseThrow(() -> new IllegalStateException("The selected attack is not available"));
            } else if (availableAttacks.size() == 1) {
                selectedAttack = availableAttacks.get(0);
            } else throw new IllegalStateException("No attacks available");
            activeAttack = selectedAttack;
        } else {
            activeAttack = null;
        }

    }

    /**
     * @inheritDoc Weapon
     */
    @Override
    public boolean hasAvailableAttacks(Player activePlayer) {
        currentShooter = activePlayer;
        return (canAffordAttack(basicAttack) && canExecuteAttack(basicAttack))
                ||
               (canAffordAttack(alternativeAttack) && canExecuteAttack(alternativeAttack));
    }
}
