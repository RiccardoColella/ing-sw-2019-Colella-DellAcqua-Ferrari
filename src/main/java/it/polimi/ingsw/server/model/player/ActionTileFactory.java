package it.polimi.ingsw.server.model.player;

import com.google.gson.Gson;
import it.polimi.ingsw.server.model.exceptions.MissingConfigurationFileException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.EnumMap;
import java.util.Map;

public class ActionTileFactory {

    private static final String ACTION_TILE_JSON_PATH = "./resources/actionTiles.json";

    private static Map<ActionTile.Type, ActionTile> actionTileMap;

    /**
     * Private empty constructor because this class should not have instances
     */
    private ActionTileFactory() { }

    /**
     * This method is used to create an ActionTile
     *
     * @param type the enum corresponding to the desired action tile
     * @return the action tile containing the configured set of CompoundAction
     */
    public static ActionTile create(ActionTile.Type type) {

        if (actionTileMap == null) {
            actionTileMap = new EnumMap<>(ActionTile.Type.class);
            ActionTile[] actionTiles;
            try {
                actionTiles = new Gson().fromJson(
                        new FileReader(new File(ACTION_TILE_JSON_PATH)),
                        ActionTile[].class
                );
            } catch (FileNotFoundException e) {
                throw new MissingConfigurationFileException("Action tile configuration file missing");
            }

            for (ActionTile actionTile: actionTiles) {
                actionTileMap.put(actionTile.getType(), actionTile);
            }
        }

        return actionTileMap.get(type);
    }

}
