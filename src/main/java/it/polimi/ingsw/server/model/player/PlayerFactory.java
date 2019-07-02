package it.polimi.ingsw.server.model.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.utils.ConfigFileMaker;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory that generates players
 *
 * @author Adriana Ferrari
 */
public final class PlayerFactory {
    /**
     * Paths of the constraints configuration file
     */
    private static final String CONSTRAINTS_JSON_PATH = "./config/playerConstraints.json";
    private static final String CONSTRAINTS_JSON_PATH_RES = "/config/playerConstraints.json";

    /**
     * Key value store of constrains
     */
    private static Map<String, Integer> constraintsMap;

    private PlayerFactory() { }

    /**
     * Creates a player given a match and a PlayerInfo object
     *
     * @param match the match to associate the player with
     * @param info the information needed for the player initialization
     * @return a player object associated with the given match
     */
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
