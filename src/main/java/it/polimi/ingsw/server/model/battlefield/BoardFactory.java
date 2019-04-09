package it.polimi.ingsw.server.model.battlefield;

import com.google.gson.*;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.exceptions.MissingConfigurationFileException;

import java.io.*;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BoardFactory {

    private static final String BOARD_JSON_FILENAME = "./resources/boards.json";
    private static Map<Preset, Block[][]> boardMap;


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
            boardMap = new EnumMap<>(Preset.class);
            JsonElement jsonElement;
            try {
                jsonElement = new JsonParser().parse(new FileReader(new File(BOARD_JSON_FILENAME)));
            } catch (IOException e) {
                throw new MissingConfigurationFileException("Unable to read Board configuration file");
            }



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

                boardMap.put(Preset.values()[i], field.toArray(new Block[][]{}));
            }
        }


        return new Board(boardMap.get(preset));
    }

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
                                Block.BorderType.findByString(borders.get(Direction.NORTH.toString()).getAsString()),
                                Block.BorderType.findByString(borders.get(Direction.EAST.toString()).getAsString()),
                                Block.BorderType.findByString(borders.get(Direction.SOUTH.toString()).getAsString()),
                                Block.BorderType.findByString(borders.get(Direction.WEST.toString()).getAsString())
                        );
            } else {
                // SPAWNPOINT

                return new SpawnpointBlock(
                                r,
                                c,
                                Block.BorderType.findByString(borders.get(Direction.NORTH.toString()).getAsString()),
                                Block.BorderType.findByString(borders.get(Direction.EAST.toString()).getAsString()),
                                Block.BorderType.findByString(borders.get(Direction.SOUTH.toString()).getAsString()),
                                Block.BorderType.findByString(borders.get(Direction.WEST.toString()).getAsString()),
                                CurrencyColor.findByString(json.getAsJsonObject().get("color").getAsString())
                        );
            }
        }
    }
}
