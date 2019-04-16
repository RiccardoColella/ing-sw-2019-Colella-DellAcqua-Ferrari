package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.currency.AmmoCube;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.exceptions.MissingOwnershipException;
import it.polimi.ingsw.server.model.player.Player;

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
    private final List<Attack> poweredAttacks;

    /**
     * This property states whether the attacks can be used in any order or not
     */
    private final boolean mustExecuteInOrder;

    private List<Attack> remainingAttacks;

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
    public WeaponWithMultipleEffects(Name name, Attack basicAttack, List<AmmoCube> acquisitionCost, List<AmmoCube> reloadCost, List<Attack> poweredAttacks, boolean mustExecuteInOrder) {
        super(name, basicAttack, acquisitionCost, reloadCost);
        this.poweredAttacks = poweredAttacks;
        this.mustExecuteInOrder = mustExecuteInOrder;
    }

    /**
     * This method gets a list with all the possible powered attack of the weapon
     * @return a list with the powered attacks of the weapon
     */
    public List<Attack> getPoweredAttacks() {
        return this.poweredAttacks;
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
        return super.hasAttack(attack) || this.poweredAttacks.contains(attack);
    }

    @Override
    public void shoot(Communicator communicator, Player activePlayer) {
        remainingAttacks = new LinkedList<>(poweredAttacks);
        remainingAttacks.add(this.basicAttack);

        do {
            if ((this.activeAttack = askForAttack(communicator)) != null) {
                // TODO: execute attack
            }
        } while (!remainingAttacks.isEmpty() && activeAttack != null);

    }

    @Nullable
    private Attack askForAttack(Communicator communicator) {
        List<String> options = remainingAttacks.stream().map(Attack::getName).collect(Collectors.toCollection(LinkedList::new));

        Attack chosenAttack = null;
        Optional<String> choice;

        choice = remainingAttacks.contains(this.basicAttack) ?
                Optional.of(communicator.select(options)) :
                communicator.selectOptional(options);

        if (choice.isPresent()) {
            chosenAttack = remainingAttacks.stream()
                    .filter(attack -> attack.getName().equals(choice.get()))
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException(("Player chose an attack that does not exist")));
            remainingAttacks.remove(chosenAttack);
        }

        return chosenAttack;
    }
}
