package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.shared.messages.ClientApi;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class inherits from the basic weapon and it also offers powered attacks that can be used together with the basic attack
 *
 * @author Adriana Ferrari
 */
public class WeaponWithMultipleEffects extends Weapon {

    /**
     * This property maps each powered attack to its cost
     */
    protected final List<Attack> poweredAttacks;

    /**
     * {@code true} if the attacks must be executed in order, {@code false} otherwise
     */
    private final boolean mustExecuteInOrder;


    /**
     * This constructor assignes all the final values to the weapon, making it ready to be bought
     *
     * @param name                  the name of the weapon
     * @param basicAttack           the basic attack of the weapon
     * @param poweredAttacks        a non-empty map of the powered attacks of the weapon with their relative cost
     * @param mustExecuteInOrder    specifies whether or not the poweredAttacks must be executed in order
     */
    public WeaponWithMultipleEffects(String name, Attack basicAttack, List<Attack> poweredAttacks, boolean mustExecuteInOrder) {
        super(name, basicAttack);
        this.poweredAttacks = Collections.unmodifiableList(poweredAttacks);
        this.mustExecuteInOrder = mustExecuteInOrder;
        this.allAttacks = new LinkedList<>(poweredAttacks);
        this.allAttacks.add(0, this.basicAttack);
        this.allAttacks = Collections.unmodifiableList(allAttacks);
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
     *
     * @return true if there is a fixed order for the attacks, otherwise false
     */
    protected boolean mustExecuteInOrder() {
        return this.mustExecuteInOrder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void attackSelection() {
        Optional<Attack> chosenAttack;
        availableAttacks = computeAvailableAttacks();
        if (availableAttacks.isEmpty() && !executedAttacks.isEmpty()) {
            chosenAttack = Optional.empty();
        } else if (availableAttacks.isEmpty()) {
            throw new IllegalStateException("No attacks were executable, weapon should not have been picked");
        } else if (executedAttacks.contains(basicAttack)) {
            List<String> attackNames = availableAttacks.stream().map(Attack::getName).collect(Collectors.toList());
            Optional<String> chosenName = interviewer.selectOptional("Select the effect", attackNames, ClientApi.ATTACK_QUESTION);
            chosenAttack = chosenName.map(name -> attackByName(name, availableAttacks));
        } else {
            List<String> attackNames = availableAttacks.stream().map(Attack::getName).collect(Collectors.toList());
            String chosenName = interviewer.select("Select the effect", attackNames, ClientApi.ATTACK_QUESTION);
            chosenAttack = Optional.of(attackByName(chosenName, availableAttacks));
        }
        activeAttack = chosenAttack.orElse(null);
    }

    /**
     * Finds an {@code Attack} in a list given its name
     *
     * @param name {@code String} representing the name of the {@code Attack}
     * @param toChooseFrom list of candidate attacks
     * @return the corresponding {@code Attack}
     * @throws IllegalStateException if no match is found
     */
    private Attack attackByName(String name, List<Attack> toChooseFrom) {
        for (Attack candidate : toChooseFrom) {
            if (candidate.getName().equals(name)) {
                return candidate;
            }
        }
        throw new IllegalStateException("No attack corresponds to " + name);
    }

    /**
     * Computes the attacks that can still be executed
     *
     * @return the list of the available attacks
     */
    private List<Attack> computeAvailableAttacks() {
        if (mustExecuteInOrder) {
            int next = executedAttacks.size();
            if (next < allAttacks.size() && canAffordAttack(allAttacks.get(next)) && canExecuteAttack(allAttacks.get(next))) {
                return new LinkedList<>(Collections.singletonList(allAttacks.get(next)));
            }
            return new LinkedList<>();
        }
        return allAttacks
                .stream()
                .filter(attack -> canAffordAttack(attack) && canExecuteAttack(attack) && !executedAttacks.contains(attack))
                .filter(availableAttack -> !availableAttack.basicMustBeDoneFirst() || executedAttacks.contains(basicAttack))
                .collect(Collectors.toList());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasAvailableAttacks(Player activePlayer) {
        resetStatus(activePlayer, new Interviewer() {
            @Override
            public <T> T select(String questionText, Collection<T> options, ClientApi messageName) {
                return null;
            }

            @Override
            public <T> Optional<T> selectOptional(String questionText, Collection<T> options, ClientApi messageName) {
                return Optional.empty();
            }
        });
        return !computeAvailableAttacks().isEmpty();
    }
}
