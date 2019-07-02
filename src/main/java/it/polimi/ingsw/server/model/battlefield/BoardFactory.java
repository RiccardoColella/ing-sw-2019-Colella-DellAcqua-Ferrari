package it.polimi.ingsw.server.model.battlefield;

import com.google.gson.*;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.utils.ConfigFileMaker;
import it.polimi.ingsw.utils.EnumValueByString;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Factory class for the board game
 *
 * @author Carlo Dell'Acqua
 */
public class BoardFactory {

    /**
     * File system path to the configuration file
     */
    private static final String BOARD_JSON_PATH = "./config/boards.json";

    /**
     * Path to the configuration file in the resources
     */
    private static final String BOARD_JSON_PATH_RES = "/config/boards.json";

    /**
     * Map that associates each preset to the relative board
     */
    private static Map<Preset, Board> boardMap;


    /**
     * This enum represents the possible configurations the board can have
     */
    public enum Preset {
        BOARD_1,
        BOARD_2,
        BOARD_3,
        BOARD_4
    }


    /**
     * This method is used to create a board based on the chosen preset
     *
     * @param preset the enum corresponding to the desired preset
     * @return the board
     */
    public static Board create(Preset preset) {

        if (boardMap == null) {
            initialize();
        }

        return boardMap.get(preset).copy();
    }

    /**
     * Reads from the json file the presets configurations and stores them in the map
     */
    private static void initialize() {
        boardMap = new EnumMap<>(Preset.class);
        JsonElement jsonElement;

        jsonElement = new JsonParser().parse(ConfigFileMaker.load(BOARD_JSON_PATH, BOARD_JSON_PATH_RES));


        for (int i = 0; i < Preset.values().length; i++) {

            JsonArray fieldJson = jsonElement.getAsJsonArray().get(i).getAsJsonArray();
            List<Block[]> field = new LinkedList<>();

            for (int r = 0; r < fieldJson.size(); r++) {

                List<Block> blockRow = new LinkedList<>();
                JsonArray row = fieldJson.get(r).getAsJsonArray();

                for (int c = 0; c < row.size(); c++) {
                    blockRow.add(blockFromJsonElement(r, c, row.get(c)));
                }
                field.add(blockRow.toArray(new Block[]{}));
            }

            boardMap.put(Preset.values()[i], new Board(field.toArray(new Block[][]{})));
        }
    }

    /**
     * Converts a JsonElement representing a block in a Block object
     *
     * @param r the row of the block in the board
     * @param c the column of the block in the board
     * @param json the json element representing the block
     * @return the Block corresponding to the json element
     */
    private static Block blockFromJsonElement(int r, int c, JsonElement json) {

        if (json.equals(JsonNull.INSTANCE)) {
            return null;
        } else {

            JsonObject borders = json.getAsJsonObject().get("borders").getAsJsonObject();

            if (json.getAsJsonObject().get("color") == null) {
                // TURRET

                return new TurretBlock(
                                r,
                                c,
                                EnumValueByString.findByString(borders.get(Direction.NORTH.toString()).getAsString(), Block.BorderType.class),
                                EnumValueByString.findByString(borders.get(Direction.EAST.toString()).getAsString(), Block.BorderType.class),
                                EnumValueByString.findByString(borders.get(Direction.SOUTH.toString()).getAsString(), Block.BorderType.class),
                                EnumValueByString.findByString(borders.get(Direction.WEST.toString()).getAsString(), Block.BorderType.class)
                        );
            } else {
                // SPAWNPOINT

                return new SpawnpointBlock(
                                r,
                                c,
                                EnumValueByString.findByString(borders.get(Direction.NORTH.toString()).getAsString(), Block.BorderType.class),
                                EnumValueByString.findByString(borders.get(Direction.EAST.toString()).getAsString(), Block.BorderType.class),
                                EnumValueByString.findByString(borders.get(Direction.SOUTH.toString()).getAsString(), Block.BorderType.class),
                                EnumValueByString.findByString(borders.get(Direction.WEST.toString()).getAsString(), Block.BorderType.class),
                                EnumValueByString.findByString(json.getAsJsonObject().get("color").getAsString(), CurrencyColor.class),
                                json.getAsJsonObject().get("maxWeapons").getAsInt()
                        );
            }
        }
    }
}
