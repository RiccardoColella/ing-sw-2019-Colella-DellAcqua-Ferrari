package it.polimi.ingsw.server.model.weapons;

import com.google.gson.Gson;
import it.polimi.ingsw.server.model.collections.Deck;
import it.polimi.ingsw.server.model.exceptions.MissingConfigurationFileException;
import it.polimi.ingsw.utils.ConfigFileMaker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class creates all the weapons of the game
 *
 * @author Carlo Dell'Acqua
 */
public final class WeaponTileFactory {

    private static final String WEAPON_JSON_PATH = "config/weapons.json";
    private static final String WEAPON_JSON_PATH_RES = "/config/weapons.json";

    private static Map<String, WeaponTile> weaponMap;

    /**
     * Private empty constructor because this class should not have instances
     */
    private WeaponTileFactory() { }

    /**
     * This method initialize a new Deck containing all the known weapons
     *
     * @return a deck containing the supported weapons
     */
    public static Deck<WeaponTile> createDeck() {


        return new Deck<>(
                getMap()
                        .values()
                        .stream()
                        .map(WeaponTile::new)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Lazy loader for the weapon map
     *
     * @return the loaded weapon map
     */
    private static Map<String, WeaponTile> getMap() {

        if (weaponMap == null) {
            weaponMap = new HashMap<>();
            WeaponTile[] weapons;
            
            weapons = new Gson().fromJson(
                    ConfigFileMaker.load(WEAPON_JSON_PATH, WEAPON_JSON_PATH_RES),
                    WeaponTile[].class
            );

            for (WeaponTile weapon: weapons) {
                weaponMap.put(weapon.getName(), weapon);
            }
        }

        return weaponMap;
    }
}
