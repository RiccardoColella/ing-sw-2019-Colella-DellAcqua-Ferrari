package it.polimi.ingsw.client.ui.cli;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.shared.datatransferobjects.BonusTile;
import it.polimi.ingsw.shared.datatransferobjects.Player;
import it.polimi.ingsw.shared.events.networkevents.MatchResumed;
import it.polimi.ingsw.shared.events.networkevents.MatchStarted;
import it.polimi.ingsw.utils.ConfigFileMaker;

import java.awt.*;
import java.util.*;
import java.util.List;

public class GameRepresentationFactory {

    private GameRepresentationFactory() {

    }


    /**
     * This property stores the location of the needed json file
     */
    private static final String TEXTS_JSON_PATH = "./config/gameTextsForCLI.json";
    private static final String TEXTS_JSON_PUSH_RES = "/config/gameTextsForCLI.json";


    /**
     * This method creates a game representation for cli
     * @param e MatchStarted event, containing info about the starting match
     * @return the expected game representation
     */
    public static GameRepresentation create(MatchStarted e) {

        List<Player> players = new LinkedList<>(e.getOpponents());
        players.add(0, e.getSelf());
        int skulls = e.getSkulls();
        Map<CurrencyColor, List<String>> weaponsOnSpawnpoint = new EnumMap<>(CurrencyColor.class);
        weaponsOnSpawnpoint.put(CurrencyColor.BLUE, e.getWeaponTop());
        weaponsOnSpawnpoint.put(CurrencyColor.RED, e.getWeaponLeft());
        weaponsOnSpawnpoint.put(CurrencyColor.YELLOW, e.getWeaponRight());
        Set<BonusTile> turretBonusTiles = e.getTurretBonusTiles();

        BoardFactory.Preset preset = e.getPreset();

        JsonElement jsonElement;
        jsonElement = new JsonParser().parse(ConfigFileMaker.load(TEXTS_JSON_PATH, TEXTS_JSON_PUSH_RES));

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        GameRepresentation.RepresentationSettings settings = new GameRepresentation.RepresentationSettings(
                jsonObject.get("rowOffset").getAsInt(),
                jsonObject.get("rowDistance").getAsInt(),
                jsonObject.get("columnOffset").getAsInt(),
                jsonObject.get("columnDistance").getAsInt(),
                jsonObject.get("maxNicknamesLength").getAsInt()
        );

        List<String> board;
        switch (preset){
            case BOARD_1:
                board = setBoard("board1", jsonObject);
                break;
            case BOARD_2:
                board = setBoard("board2", jsonObject);
                break;
            case BOARD_3:
                board = setBoard("board3", jsonObject);
                break;
            case BOARD_4:
                board = setBoard("board4", jsonObject);
                break;
            default: throw new IllegalArgumentException("Cannot find preset " + e.getPreset().toString());
        }

        return new GameRepresentation(
                board,
                players,
                skulls,
                weaponsOnSpawnpoint,
                turretBonusTiles,
                settings
        );
    }

    /**
     * This method creates a game representation for cli when it is resumed
     * @param e MatchResumed event, containing info about the resumed match
     * @return the expected game representation
     */
    public static GameRepresentation create(MatchResumed e) {
        GameRepresentation gameRepresentation = create((MatchStarted) e);
        gameRepresentation.setKillshots(e.getKillshots());
        e.getPlayerLocations().entrySet()
                .stream()
                .forEach(player -> {
                    gameRepresentation.movePlayer(player.getKey(), player.getValue().y, player.getValue().x);
                    gameRepresentation.setPlayerAlive(player.getKey());
                });
        return gameRepresentation;

    }


    /**
     * Board setter. This method simplifies the class's constructor method
     * @param elem name of the element that contains the board in the json file
     * @param jsonObject json object from which extract the board
     */
    private static List<String> setBoard(String elem, JsonObject jsonObject) {
        List<String> boardUnderConstruction = new LinkedList<>();

        JsonArray boardDescription = jsonObject.get(elem).getAsJsonArray();
        for (JsonElement line : boardDescription){
            boardUnderConstruction.add(line.getAsString());
        }
        return boardUnderConstruction;
    }
}
