package it.polimi.ingsw.client.ui.cli;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.exceptions.MissingConfigurationFileException;
import it.polimi.ingsw.shared.events.networkevents.MatchStarted;
import it.polimi.ingsw.shared.events.networkevents.PlayerHealthChanged;
import it.polimi.ingsw.shared.datatransferobjects.Player;
import it.polimi.ingsw.shared.datatransferobjects.Wallet;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class GameRepresentation {

    private BoardFactory.Preset preset;

    private List<Player> players;

    private int skulls;

    private List<String> weaponsOnLeftSpawnpoint;

    private List<String> weaponsOnRightSpawnpoint;

    private List<String> weaponsOnTopSpawnpoint;

    Map<Player, Point> playerLocations = new HashMap<>();

    private static final String  TEXTS_JSON_FILE = "./resources/gameTextsForCLI.json";

    private List<String> board;


    private final int rowOffset;
    private final int columnOffset;
    private final int rowDistance;
    private final int columnDistance;

    public GameRepresentation(MatchStarted e){

        this.preset = e.getPreset();
        this.players = new LinkedList<>(e.getOpponents());
        this.players.add(0, e.getSelf());
        this.skulls = e.getSkulls();
        this.weaponsOnLeftSpawnpoint = new ArrayList<>(e.getWeaponLeft());
        this.weaponsOnTopSpawnpoint = new ArrayList<>(e.getWeaponTop());
        this.weaponsOnRightSpawnpoint = new ArrayList<>(e.getWeaponRight());

        JsonElement jsonElement;

        try {
            jsonElement = new JsonParser().parse(new FileReader(new File(TEXTS_JSON_FILE)));
        } catch (IOException ex) {
            throw new MissingConfigurationFileException("Unable to read texts configuration file");
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        this.rowOffset = jsonObject.get("rowOffset").getAsInt();
        this.columnOffset = jsonObject.get("columnOffset").getAsInt();
        this.rowDistance =jsonObject.get("rowDistance").getAsInt();
        this.columnDistance = jsonObject.get("columnDistance").getAsInt();

        switch (preset){
            case BOARD_1:
                setBoard("board1", jsonObject);
                break;
            case BOARD_2:
                setBoard("board2", jsonObject);
                break;
            case BOARD_3:
                setBoard("board3", jsonObject);
                break;
            case BOARD_4:
                setBoard("board4", jsonObject);
                break;
            default: throw new IllegalArgumentException("Cannot find preset " + e.getPreset().toString());
        }
    }

    private void setBoard(String elem, JsonObject jsonObject){
        List<String> boardUnderConstruction = new LinkedList<>();

        JsonArray boardDescription = jsonObject.get(elem).getAsJsonArray();
        for (JsonElement line : boardDescription){
            boardUnderConstruction.add(line.getAsString());
        }
        this.board = boardUnderConstruction;
    }

    private int getRowOffset() { return rowOffset; }

    private int getColumnOffset() { return columnOffset; }

    private int getRowDistance() { return rowDistance; }

    private int getColumnDistance() { return columnDistance; }

    public void printBoard(List<String> board, PrintStream printStream){
        for (String line : board){
            printStream.print(line);
        }
    }

    void printEmptyBoard(PrintStream printStream){
        for (String line : board){
            printStream.print(line);
        }
    }

    public List<String> positPlayers(){
        List<String> boardWithPlayers = new LinkedList<>(board);
        for (Player player : players){
            int i = players.indexOf(player);
            String nick = player.getNickname();
            if (nick.length() > 14){
                nick = nick.substring(0, 14);
            }
            int x = playerLocations.get(player).x;
            int y = playerLocations.get(player).y;
            // Row is the general offset for rows + the player's row + the distance of the needed block
            int row = getRowOffset() + i + getRowDistance()*x;
            // Column is the general column offset + the distance of the needed block
            int column = getColumnOffset() + getColumnDistance()*y;
            String line = board.get(row);
            String newLine = line.substring(0, column) +
                    nick +
                    line.substring(column + nick.length(), line.length()-1) +
                    "\n";
            boardWithPlayers.set(row, newLine);
        }
        return boardWithPlayers;
    }

    protected List<Player> getPlayers(){ return this.players; }

    private Player selectPlayer(Player playerToSelect){
        for (Player player : players){
            if (player == playerToSelect){
                return player;
            }
        }
        throw new IllegalArgumentException("Player " + playerToSelect.getNickname() + " not found in players");
    }

    public void movePlayer(Player player, int r, int c) {
        playerLocations.put(selectPlayer(player), new Point(r, c));
    }

    void grabPlayerWeapon(Player player, String weapon, int r, int c){
        // Here we select the spawnpoint from which remove the weapon grabbed
        if (r == 0){
            weaponsOnLeftSpawnpoint.remove(weapon);
        } else if (c == 0){
            weaponsOnTopSpawnpoint.remove(weapon);
        } else weaponsOnRightSpawnpoint.remove(weapon);
        // then we add the weapon to player's wallet
        selectPlayer(player).getWallet().getLoadedWeapons().add(weapon);
    }

    void dropPlayerWeapon(Player player, String weapon, int r, int c){
        // Here we select the spawnpoint to which add the weapon dropped
        if (r == 0){
            weaponsOnLeftSpawnpoint.add(weapon);
        } else if (c == 0){
            weaponsOnTopSpawnpoint.add(weapon);
        } else weaponsOnRightSpawnpoint.add(weapon);
        // then we remove the weapon from player's wallet
        if (selectPlayer(player).getWallet().getLoadedWeapons().contains(weapon)){
            selectPlayer(player).getWallet().getLoadedWeapons().remove(weapon);
        } else selectPlayer(player).getWallet().getUnloadedWeapons().remove(weapon);
    }


    public void updatePlayer(Player player) {
        for (Player p : players) {
            if (p.getNickname().equals(player.getNickname())) {
                players.set(players.indexOf(p), player);
                break;
            }
        }
    }
}
