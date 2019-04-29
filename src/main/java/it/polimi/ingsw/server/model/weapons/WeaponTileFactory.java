package it.polimi.ingsw.server.model.weapons;

import com.google.gson.Gson;
import it.polimi.ingsw.server.model.collections.Deck;
import it.polimi.ingsw.server.model.exceptions.MissingConfigurationFileException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * This class creates all the 21 weapons of the game
 */
public final class WeaponTileFactory {

    private static final String WEAPON_JSON_PATH = "./resources/weapons.json";

    private static Map<WeaponTile.Name, WeaponTile> weaponMap;

    /**
     * Private empty constructor because this class should not have instances
     */
    private WeaponTileFactory() { }

    /**
     * This method is used to create any weapon
     * @param name the enum corresponding to the desired weapon
     * @return the weapon, ready to be bought
     */
    public static WeaponTile create(WeaponTile.Name name) {

        if (weaponMap == null) {
            weaponMap = new EnumMap<>(WeaponTile.Name.class);
            WeaponTile[] weapons;
            try {
                weapons = new Gson().fromJson(
                        new FileReader(new File(WEAPON_JSON_PATH)),
                        WeaponTile[].class
                );
            } catch (FileNotFoundException e) {
                throw new MissingConfigurationFileException("WeaponTile configuration file not found", e);
            }

            for (WeaponTile weapon: weapons) {
                weaponMap.put(weapon.getName(), weapon);
            }
        }

        return new WeaponTile(weaponMap.get(name));
    }

    /**
     * This method initialize a new Deck containing all the known weapons
     *
     * @return a deck containing the supported weapons
     */
    public static Deck<WeaponTile> createDeck() {
        LinkedList<WeaponTile> weapons = new LinkedList<>();
        for (WeaponTile.Name name : WeaponTile.Name.values()) {
            WeaponTile weapon = create(name);
            weapons.add(weapon);
        }
        return new Deck<>(weapons);
    }
}
