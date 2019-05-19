package it.polimi.ingsw.server.model.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.server.model.exceptions.MissingConfigurationFileException;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.utils.ConfigFileMaker;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class PlayerFactory {
    private static final String CONSTRAINTS_JSON_PATH = "./config/playerConstraints.json";
    private static final String CONSTRAINTS_JSON_PATH_RES = "/config/playerConstraints.json";
    private static Map<String, Integer> constraintsMap;

    private PlayerFactory() {

    }

    public static Player create(Match match, PlayerInfo info) {
        if (constraintsMap == null) {
            readConstraints();
        }
        return new Player(match, info, new PlayerConstraints(constraintsMap));
    }

    /**
     * This method is used to read the player constraints from a json
     */
    private static void readConstraints() {
        constraintsMap = new HashMap<>();
        JsonElement jsonElement;

        jsonElement = new JsonParser().parse(ConfigFileMaker.load(CONSTRAINTS_JSON_PATH, CONSTRAINTS_JSON_PATH_RES));

        JsonObject constraints = jsonElement.getAsJsonObject();
        constraints.keySet().forEach(k -> constraintsMap.put(k, constraints.get(k).getAsInt()));
    }
}
