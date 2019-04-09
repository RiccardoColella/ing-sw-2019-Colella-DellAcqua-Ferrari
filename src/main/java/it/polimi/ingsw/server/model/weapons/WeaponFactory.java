package it.polimi.ingsw.server.model.weapons;

import com.google.gson.Gson;
import it.polimi.ingsw.server.model.collections.Deck;
import it.polimi.ingsw.server.model.exceptions.MissingConfigurationFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

/**
 * This class creates all the 21 weapons of the game
 */
public final class WeaponFactory {

    private static final String WEAPON_JSON_PATH = "./resources/weapons.json";

    private static Map<Weapon.Name, Weapon> weaponMap;

    /**
     * Private empty constructor because this class should not have instances
     */
    private WeaponFactory() { }

    /**
     * This method is used to create any weapon
     * @param name the enum corresponding to the desired weapon
     * @return the weapon, ready to be bought
     */
    public static Weapon create(Weapon.Name name) {

        if (weaponMap == null) {
            weaponMap = new EnumMap<>(Weapon.Name.class);
            Weapon[] weapons;
            try {
                weapons = new Gson().fromJson(
                        new FileReader(new File(WEAPON_JSON_PATH)),
                        Weapon[].class
                );
            } catch (FileNotFoundException e) {
                throw new MissingConfigurationFileException("Weapon configuration file not found");
            }

            for (Weapon weapon: weapons) {
                weaponMap.put(weapon.getName(), weapon);
            }
        }

        return new Weapon(weaponMap.get(name));
    }

    /**
     * This method initialize a new Deck containing all the known weapons
     *
     * @return a deck containing the supported weapons
     */
    public static Deck<Weapon> createDeck() {
        LinkedList<Weapon> weapons = new LinkedList<>();
        for (Weapon.Name name : Weapon.Name.values()) {
            Weapon weapon = create(name);
            weapons.add(weapon);
        }
        return new Deck<>(weapons);
    }
}
