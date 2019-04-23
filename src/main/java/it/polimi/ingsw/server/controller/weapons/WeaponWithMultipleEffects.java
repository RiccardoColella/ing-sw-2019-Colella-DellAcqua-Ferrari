package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.weapons.Weapon;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.shared.commands.ClientApi;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class inherits from the basic weapon and it also offers powered attacks that can be used together with the basic attack
 */
public class WeaponWithMultipleEffects extends BasicWeapon {

    /**
     * This property maps each powered attack to its cost
     */
    protected final List<Attack> poweredAttacks;

    private final boolean mustExecuteInOrder;

    /**
     * This constructor assignes all the final values to the weapon, making it ready to be bought
     *
     * @param name                  the name of the weapon
     * @param basicAttack           the basic attack of the weapon
     * @param poweredAttacks        a non-empty map of the powered attacks of the weapon with their relative cost
     */
    public WeaponWithMultipleEffects(Weapon.Name name, Attack basicAttack, List<Attack> poweredAttacks, boolean mustExecuteInOrder) {
        super(name, basicAttack);
        this.poweredAttacks = Collections.unmodifiableList(poweredAttacks);
        this.mustExecuteInOrder = mustExecuteInOrder;
    }

    /**
     * This method gets a list with all the possible powered attack of the weapon
     * @return a list with the powered attacks of the weapon
     */
    protected List<Attack> getPoweredAttacks() {
        return this.poweredAttacks;
    }

    /**
     * This method tells whether the powered attacks and the basic attack can be used in any order
     * @return true if there is a fixed order for the attacks, otherwise false
     */
    protected boolean mustExecuteInOrder() {
        return this.mustExecuteInOrder;
    }


    @Override
    public void shoot(Interviewer interviewer, Player activePlayer) {
        List<Attack> allAttacks = new LinkedList<>(poweredAttacks);
        allAttacks.add(0, this.basicAttack);
        previouslyHit.clear();
        executedAttacks.clear();
        currentShooter = activePlayer;
        Optional<Attack> chosenAttack;

        do {
            if (mustExecuteInOrder) {
                chosenAttack = forceOrder(interviewer, allAttacks);
            } else {
                availableAttacks = computeAvailableAttacks(allAttacks);
                if (availableAttacks.isEmpty() && !executedAttacks.isEmpty()) {
                    chosenAttack = Optional.empty();
                } else if (availableAttacks.isEmpty()) {
                    throw new IllegalStateException("No attacks were executable, weapon should not have been picked");
                } else {
                    List<Attack> executableNow = computeNowExecutableAttacks();
                    if (executableNow.isEmpty()) {

                        chosenAttack = Optional.empty();
                    } else if (executedAttacks.contains(basicAttack)) {
                        List<String> attackNames = executableNow.stream().map(Attack::getName).collect(Collectors.toList());
                        Optional<String> chosenName = interviewer.selectOptional("Select the effect", attackNames, ClientApi.ATTACK_QUESTION);
                        chosenAttack = chosenName.map(name -> attackByName(name, executableNow));
                    } else {
                        List<String> attackNames = executableNow.stream().map(Attack::getName).collect(Collectors.toList());
                        String chosenName = interviewer.select("Select the effect", attackNames, ClientApi.ATTACK_QUESTION);
                        chosenAttack = Optional.of(attackByName(chosenName, executableNow));
                    }
                }
            }
            activeAttack = chosenAttack.orElse(null);
            if (activeAttack != null) {
                executedAttacks.add(activeAttack);
                handlePayment(interviewer, activeAttack, currentShooter);
                activeAttack.execute(interviewer, this);
            }
        } while (activeAttack != null);

    }

    private Attack attackByName(String name, List<Attack> toChooseFrom) {
        for (Attack candidate : toChooseFrom) {
            if (candidate.getName().equals(name)) {
                return candidate;
            }
        }
        throw new IllegalStateException("No attack corresponds to " + name);
    }

    private List<Attack> computeAvailableAttacks(List<Attack> allAttacks) {
        List<Attack> available = new LinkedList<>();
        for (Attack attack : allAttacks) {
            if (canAffordAttack(attack) && canDoFirstAction(attack) && !executedAttacks.contains(attack)) {
                available.add(attack);
            }
        }
        return available;
    }

    private List<Attack> computeNowExecutableAttacks() {
        List<Attack> executableNow = new LinkedList<>();
        for (Attack availableAttack : availableAttacks) {
            if (!availableAttack.basicMustBeDoneFirst() || executedAttacks.contains(basicAttack)) {
                executableNow.add(availableAttack);
            }
        }
        return executableNow;
    }

    private Optional<Attack> forceOrder(Interviewer interviewer, List<Attack> allAttacks) {
        availableAttacks = new LinkedList<>(allAttacks);
        availableAttacks.removeAll(executedAttacks);
        if (availableAttacks.isEmpty()) {
            return Optional.empty();
        } else if (!executedAttacks.isEmpty()) {
            Optional<String> chosen = interviewer.selectOptional("Select the effect", Collections.singleton(availableAttacks.get(0).getName()), ClientApi.ATTACK_QUESTION);
            return chosen.map(name -> attackByName(name, Collections.singletonList(availableAttacks.get(0))));
        } else {
            String chosen = interviewer.select("Select the effect", Collections.singleton(availableAttacks.get(0).getName()), ClientApi.ATTACK_QUESTION);
            return Optional.of(attackByName(chosen, Collections.singletonList(availableAttacks.get(0))));
        }
    }

    @Override
    public boolean hasAvailableAttacks(Player activePlayer) {
        currentShooter = activePlayer;
        List<Attack> available = new LinkedList<>();
        for (Attack a : poweredAttacks) {
            if (canAffordAttack(a) && canDoFirstAction(a)) {
                available.add(a);
            }
        }
        return (canAffordAttack(basicAttack) && canDoFirstAction(basicAttack))
                ||
                !available.isEmpty();
    }
}
