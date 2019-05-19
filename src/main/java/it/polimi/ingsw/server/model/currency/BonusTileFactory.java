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

public final class BonusTileFactory {

    private static final String BONUS_DECK_JSON_PATH = "./config/bonusDeck.json";
    private static final String BONUS_DECK_JSON_PATH_RES = "/config/bonusDeck.json";
    private static Map<BonusTile, Integer> tileQuantityMap;
    /**
     * Empty private constructor
     */
    private BonusTileFactory() {

    }

    public static BonusTile create(CurrencyColor firstColor, CurrencyColor secondColor) {
        return new BonusTile(AmmoCubeFactory.create(firstColor), AmmoCubeFactory.create(secondColor));
    }

    public static BonusTile create(CurrencyColor firstColor, CurrencyColor secondColor, CurrencyColor thirdColor) {
        return new BonusTile(AmmoCubeFactory.create(firstColor), AmmoCubeFactory.create(secondColor), AmmoCubeFactory.create(thirdColor));
    }

    /**
     * This method creates the single tiles, mapping them with their amount, reading from the configuration file
     */
    private static void readTiles() {
        tileQuantityMap = new HashMap<>();
        JsonElement jsonElement;

        jsonElement = new JsonParser().parse(ConfigFileMaker.load(BONUS_DECK_JSON_PATH, BONUS_DECK_JSON_PATH_RES));

        JsonArray jsonTileArray = jsonElement.getAsJsonArray();
        jsonTileArray.forEach(
                tile -> {
                    if (tile.getAsJsonObject().get("thirdAmmo") != null) {
                        tileQuantityMap.put(
                                BonusTileFactory.create(
                                        EnumValueByString.findByString(tile.getAsJsonObject().get("firstAmmo").getAsString(), CurrencyColor.class),
                                        EnumValueByString.findByString(tile.getAsJsonObject().get("secondAmmo").getAsString(), CurrencyColor.class),
                                        EnumValueByString.findByString(tile.getAsJsonObject().get("thirdAmmo").getAsString(), CurrencyColor.class)
                                ),
                                tile.getAsJsonObject().get("quantity").getAsInt()
                        );
                    } else {
                        tileQuantityMap.put(
                                BonusTileFactory.create(
                                        EnumValueByString.findByString(tile.getAsJsonObject().get("firstAmmo").getAsString(), CurrencyColor.class),
                                        EnumValueByString.findByString(tile.getAsJsonObject().get("secondAmmo").getAsString(), CurrencyColor.class)
                                ),
                                tile.getAsJsonObject().get("quantity").getAsInt()
                        );
                    }
                }

        );
    }
    public static Deck<BonusTile> createDeck() {
        if (tileQuantityMap == null) {
            readTiles();
        }
        List<BonusTile> bonusTiles = new LinkedList<>();
        tileQuantityMap.forEach((tile, quantity) -> {
            for (int i = 0; i < quantity; i++) {
                bonusTiles.add(new BonusTile(tile));
            }
        });
        return new Deck<>(bonusTiles, true);
    }
}
