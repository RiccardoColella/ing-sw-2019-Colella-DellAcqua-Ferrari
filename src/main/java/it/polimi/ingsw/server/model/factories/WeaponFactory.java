package it.polimi.ingsw.server.model.factories;

import it.polimi.ingsw.server.model.collections.Deck;
import it.polimi.ingsw.server.model.weapons.Weapon;
import it.polimi.ingsw.server.model.weapons.WeaponWithMultipleEffects;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * This class creates all the 21 weapons of the game
 */
public final class WeaponFactory {

    /**
     * Private empty constructor because this class should not have instances
     */
    private WeaponFactory() {

    }
    /**
     * This method is used to create any weapon
     * @param name the enum corresponding to the desired weapon
     * @return the weapon, ready to be bought
     */
    public static Weapon create(Weapon.Name name) {
        return null;
    }

    public static Deck<Weapon> createDeck() {
        //CREATING THE WEAPON DECK:
        return new Deck<>(
                Arrays
                        .stream(Weapon.Name.values())
                        .map(WeaponFactory::create)
                        .collect(Collectors.toCollection(LinkedList::new))
        );
    }
}
