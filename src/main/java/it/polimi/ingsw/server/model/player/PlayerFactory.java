package it.polimi.ingsw.server.model.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.server.model.exceptions.MissingConfigurationFileException;
import it.polimi.ingsw.server.model.match.Match;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class PlayerFactory {
    private static final String CONSTRAINTS_JSON_FILENAME = "./resources/playerConstraints.json";
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
        try {
            jsonElement = new JsonParser().parse(new FileReader(new File(CONSTRAINTS_JSON_FILENAME)));
        } catch (IOException e) {
            throw new MissingConfigurationFileException("Unable to read Rewards configuration file", e);
        }
        JsonObject constraints = jsonElement.getAsJsonObject();
        constraints.keySet().forEach(k -> constraintsMap.put(k, constraints.get(k).getAsInt()));
    }
}
