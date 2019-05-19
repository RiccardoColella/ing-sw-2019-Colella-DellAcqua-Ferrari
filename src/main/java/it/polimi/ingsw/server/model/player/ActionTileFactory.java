package it.polimi.ingsw.server.model.player;

import com.google.gson.Gson;
import it.polimi.ingsw.server.model.exceptions.MissingConfigurationFileException;
import it.polimi.ingsw.utils.ConfigFileMaker;

import java.io.*;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

public class ActionTileFactory {

    private static final String ACTION_TILE_JSON_PATH = "config/actionTiles.json";
    private static final String ACTION_TILE_JSON_PATH_RES = "/config/actionTiles.json";

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

            actionTiles = new Gson().fromJson(
                    ConfigFileMaker.load(ACTION_TILE_JSON_PATH, ACTION_TILE_JSON_PATH_RES),
                    ActionTile[].class
            );

            for (ActionTile actionTile: actionTiles) {
                actionTileMap.put(actionTile.getType(), actionTile);
            }
        }

        return actionTileMap.get(type);
    }

}
