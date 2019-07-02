package it.polimi.ingsw.server.model.currency;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import it.polimi.ingsw.server.model.collections.Deck;
import it.polimi.ingsw.server.model.exceptions.MissingConfigurationFileException;
import it.polimi.ingsw.utils.ConfigFileMaker;
import it.polimi.ingsw.utils.EnumValueByString;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class creates all the powerup tiles needed in the game
 */
public final class PowerupTileFactory {

    /**
     * File system path to the configuration file
     */
    private static final String POWERUP_DECK_JSON_PATH = "./config/powerupDeck.json";

    /**
     * Path to the configuration file in the resources
     */
    private static final String POWERUP_DECK_JSON_PATH_RES = "/config/powerupDeck.json";

    /**
     * Map that pairs a powerup tile to how many time it appears in the deck
     */
    private static Map<PowerupTile, Integer> tileQuantityMap;

    /**
     * Private empty constructor because this class should not have instances
     */
    private PowerupTileFactory() { }

    /**
     * This method creates a new powerup tile given its type and its color
     * @param type a value of the Name enum
     * @param color a value of the CurrencyColor enum
     * @return a new powerup tile with the given characteristics
     */
    public static PowerupTile create(String type, CurrencyColor color) {
        return new PowerupTile(color, type);
    }

    /**
     * This method creates a new Deck of PowerupTile
     * @return the Deck of PowerupTile
     */
    public static Deck<PowerupTile> createDeck() {
        if (tileQuantityMap == null) {
            readTiles();
        }
        List<PowerupTile> powerupCards = new LinkedList<>();
        tileQuantityMap.forEach((tile, quantity) -> {
            for (int i = 0; i < quantity; i++) {
                powerupCards.add(new PowerupTile(tile));
            }
        });
        return new Deck<>(powerupCards, true);
    }

    /**
     * This method creates the single tiles, mapping them with their amount, reading from the configuration file
     */
    private static void readTiles() {
        tileQuantityMap = new HashMap<>();
        JsonElement jsonElement;

        jsonElement = new JsonParser().parse(ConfigFileMaker.load(POWERUP_DECK_JSON_PATH, POWERUP_DECK_JSON_PATH_RES));

        JsonArray jsonTileArray = jsonElement.getAsJsonArray();
        jsonTileArray.forEach(
            tile -> tileQuantityMap.put(
                new PowerupTile(
                    EnumValueByString.findByString(tile.getAsJsonObject().get("color").getAsString(), CurrencyColor.class),
                    tile.getAsJsonObject().get("name").getAsString()
                ),
                tile.getAsJsonObject().get("quantity").getAsInt()
            )
        );
    }
}
