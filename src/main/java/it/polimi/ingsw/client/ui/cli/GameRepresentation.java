package it.polimi.ingsw.client.ui.cli;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.exceptions.MissingConfigurationFileException;
import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.shared.datatransferobjects.Powerup;
import it.polimi.ingsw.shared.events.networkevents.MatchStarted;
import it.polimi.ingsw.shared.datatransferobjects.Player;
import it.polimi.ingsw.utils.ConfigFileMaker;
import org.jetbrains.annotations.Contract;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class GameRepresentation {

    /**
     * Board preset
     */
    private BoardFactory.Preset preset;

    /**
     * List of all players
     */
    private List<Player> players;

    /**
     * This number stores the skulls owned by the player
     */
    private int skulls;

    /**
     * This property stores all weapons on all spawnpoint.
     */
    private Map<CurrencyColor, List<String>> weaponsOnSpawnpoint;

    /**
     * This property stores all player's positions
     */
    Map<Player, Point> playerLocations = new HashMap<>();

    /**
     * This property stores the location of the needed json file
     */
    private static final String TEXTS_JSON_PATH = "./config/gameTextsForCLI.json";
    private static final String TEXTS_JSON_PUSH_RES = "/config/gameTextsForCLI.json";

    /**
     * This property stores the board as a list of strings
     */
    private List<String> board;

    /**
     * This property stores the first useful char for writing on a row
     */
    private final int rowOffset;
    /**
     * This property stores the first useful char for writing on a column
     */
    private final int columnOffset;
    /**
     * This property stores the horizontal distance in char between two blocks
     */
    private final int rowDistance;
    /**
     * This property stores the vertical distance in lines between two blocks
     */
    private final int columnDistance;
    /**
     * This property stores the width in char of the whole board
     */
    private final int boardWidth;
    /**
     * This property stores the max length in char for a name to be written in a block
     */
    private final int maxNicknamesLength;

    /**
     * This variable stores the anonymous class useful for coloured outputs
     */
    private ANSIColor colors;

    GameRepresentation(MatchStarted e){

        this.preset = e.getPreset();
        this.players = new LinkedList<>(e.getOpponents());
        this.players.add(0, e.getSelf());
        this.skulls = e.getSkulls();
        this.weaponsOnSpawnpoint = new EnumMap<>(CurrencyColor.class);
        this.weaponsOnSpawnpoint.put(CurrencyColor.BLUE, e.getWeaponTop());
        this.weaponsOnSpawnpoint.put(CurrencyColor.RED, e.getWeaponLeft());
        this.weaponsOnSpawnpoint.put(CurrencyColor.YELLOW, e.getWeaponRight());

        this.colors = new ANSIColor();

        JsonElement jsonElement;

        jsonElement = new JsonParser().parse(ConfigFileMaker.load(TEXTS_JSON_PATH, TEXTS_JSON_PUSH_RES));

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        this.rowOffset = jsonObject.get("rowOffset").getAsInt();
        this.columnOffset = jsonObject.get("columnOffset").getAsInt();
        this.rowDistance =jsonObject.get("rowDistance").getAsInt();
        this.columnDistance = jsonObject.get("columnDistance").getAsInt();
        this.boardWidth = jsonObject.get("boardWidth").getAsInt();
        this.maxNicknamesLength = jsonObject.get("maxNicknamesLength").getAsInt();

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

    /**
     * Board getter
     * @return the board
     */
    public List<String> getBoard() {
        return board;
    }

    /**
     * Board setter. This method simplifies the class's constructor method
     * @param elem name of the element that contains the board in the json file
     * @param jsonObject json object from which extract the board
     */
    private void setBoard(String elem, JsonObject jsonObject){
        List<String> boardUnderConstruction = new LinkedList<>();

        JsonArray boardDescription = jsonObject.get(elem).getAsJsonArray();
        for (JsonElement line : boardDescription){
            boardUnderConstruction.add(line.getAsString());
        }
        this.board = boardUnderConstruction;
    }

    @Contract(pure = true)
    private int getRowOffset() { return rowOffset; }

    @Contract(pure = true)
    private int getColumnOffset() { return columnOffset; }

    @Contract(pure = true)
    private int getRowDistance() { return rowDistance; }

    @Contract(pure = true)
    private int getColumnDistance() { return columnDistance; }

    protected List<Player> getPlayers(){ return this.players; }

    /**
     * This method prints the given board to the given output
     * @param board board to be printed
     * @param printStream output stream to which print to
     */
    public void printBoard(List<String> board, PrintStream printStream){
        for (String line : board){
            printStream.print(line);
        }
    }

    /**
     * This method prints the board as caught from the json file
     * @param printStream output stream to which print to
     */
    void printEmptyBoard(PrintStream printStream){
        for (String line : board){
            printStream.print(line);
        }
    }

    /**
     * This method builds a board containing all the player in this.playerLocations variable
     * @param board board to which add the players
     * @return the built board
     */
    public List<String> positPlayers(List<String> board){
        List<String> boardWithPlayers = new LinkedList<>(board);
        for (Player player : players){
            int i = players.indexOf(player);
            String nick = player.getNickname();
            if (nick.length() > this.maxNicknamesLength){
                nick = nick.substring(0, this.maxNicknamesLength);
            }
            int x = playerLocations.get(player).x;
            int y = playerLocations.get(player).y;
            // Row is the general offset for rows + the player's row + the distance of the needed block
            int row = getRowOffset() + i + getRowDistance()*x;
            // Column is the general column offset + the distance of the needed block
            int column = getColumnOffset() + getColumnDistance()*y;
            String line = board.get(row);
            String newLine = line.substring(0, column) +
                    colors.getEscape(player.getColor()) +
                    nick +
                    colors.getEscapeReset() +
                    line.substring(column + nick.length(), line.length()-1) +
                    "\n";
            boardWithPlayers.set(row, newLine);
        }
        return boardWithPlayers;
    }

    /**
     * This method builds a board containing all the weapons in this.weaponsOnSpawnpoint variable
     * @param board board to which add the players
     * @return the built board
     */
    public List<String> positSpawnpointsWeapons(List<String> board){
        List<String> boardWithWeapons = new LinkedList<>(board);
        String separator = " - ";
        weaponsOnSpawnpoint
                .forEach( (spawnpointColor, weapons) -> {
                    int index =
                            rowOffset - 1 +
                            rowDistance* Arrays.asList(CurrencyColor.values()).indexOf(spawnpointColor);
                    String line = board.get(index);
                    line = line.substring(0, line.length() - 2) +
                            colors.getEscape(spawnpointColor) +
                            " " + spawnpointColor.toString() + "SPAWNPOINT" +
                            colors.getEscapeReset() +
                            "\n";
                    boardWithWeapons.set(index, line);
                    for (String weapon : weapons){
                        index++;
                        line = board.get(index);
                        line = line.substring(0, line.length() - 2) +
                                separator +
                                weapon +
                                "\n";
                        boardWithWeapons.set(index, line);
                    }
                });
        return boardWithWeapons;
    }

    /**
     * This method builds a board containing all the player info in this.players variable
     * @param board board to which add the players
     * @return the built board
     */
    public List<String> positPlayerInfo(List<String> board){
        List<String> boardUpdated = new LinkedList<>(board);
        String separator = ". ";
        for (Player player : players){
            List<String> linesToAdd = new LinkedList<>();
            // First line
            linesToAdd.add("\n");

            // Second line
            StringBuilder newLine = new StringBuilder();
            newLine.append(" - ");
            // coloured Nickname
            appendColoredString(newLine, player.getColor(), player.getNickname());
            newLine.append(separator);
            // coloured color
            appendColoredString(newLine, player.getColor(), player.getColor().toString());
            newLine.append(separator);
            // Skulls, damages and marks
            addPlayerHealth(newLine, player, separator);
            newLine.append("\n");
            linesToAdd.add(newLine.toString());

            // Third line
            newLine = new StringBuilder();
            // tab
            newLine.append("\t");
            newLine.append("wallet| ");
            // Loaded weapons
            for (String loadedWeapon : player.getWallet().getLoadedWeapons()){
                newLine.append("lw: ");
                newLine.append(loadedWeapon);
                newLine.append(". ");
            }
            // Unloaded weapons
            for (String unloadedWeapon : player.getWallet().getUnloadedWeapons()){
                newLine.append("uw: ");
                newLine.append(unloadedWeapon);
                newLine.append(". ");
            }
            newLine.append("\n");
            linesToAdd.add(newLine.toString());

            // Fourth line
            newLine = new StringBuilder();
            // tab and 8 spaces, to align with the upper line
            newLine.append("\t      | ");
            // adding a colored cube for every ammo cube
            newLine.append("Ammocubes: ");
            if (!player.getWallet().getAmmoCubes().isEmpty()){
                newLine.append("|");
            }
            for (CurrencyColor ammoCubeColor : player.getWallet().getAmmoCubes()){
                appendColoredBackgroundString(newLine, ammoCubeColor, " " + ammoCubeColor.toString().substring(0,1) + " ");
                newLine.append("|");
            }
            newLine.append(" ");
            // adding all player's powerups
            for (Powerup powerup : player.getWallet().getPowerups()){
                newLine.append("p: ");
                appendColoredString(newLine, powerup.getColor(), powerup.getName());
                newLine.append(". ");
            }
            newLine.append("\n");
            linesToAdd.add(newLine.toString());

            boardUpdated.addAll(linesToAdd);
        }
        return boardUpdated;
    }

    /**
     * This method simplifies the positPlayerInfo method managing player's health
     * @param stringBuilder string to which add the infos
     * @param player player we're considering
     * @param infoSeparator separator between infos
     */
    private void addPlayerHealth(StringBuilder stringBuilder, final Player player, String infoSeparator){
        stringBuilder.append("Skulls: ");
        stringBuilder.append(player.getSkulls());
        stringBuilder.append(infoSeparator);

        stringBuilder.append("Damages: ");
        stringBuilder.append(player.getDamage().size());
        if (player.getDamage().size() > 0){
            stringBuilder.append(infoSeparator);
        }
        appendLettersOnColoredBackground(stringBuilder, player.getDamage());
        stringBuilder.append(infoSeparator);

        stringBuilder.append("Marks: ");
        stringBuilder.append(player.getMarks().size());
        stringBuilder.append(" ");
        appendLettersOnColoredBackground(stringBuilder, player.getMarks());
    }

    /**
     * This method appends to the string builder a letter for every player color given (the initial of the color) on a
     * coloured background
     * @param stringBuilder string to which add infos
     * @param list list of color to be added to the list
     */
    private void appendLettersOnColoredBackground(StringBuilder stringBuilder, List<PlayerColor> list){
        for (PlayerColor color : list){
            switch (color) {
                case TURQUOISE:
                    appendColoredBackgroundString(stringBuilder, PlayerColor.TURQUOISE, "T");
                    break;
                case GREEN:
                    appendColoredBackgroundString(stringBuilder, PlayerColor.GREEN, "G");
                    break;
                case YELLOW:
                    appendColoredBackgroundString(stringBuilder, PlayerColor.YELLOW, "Y");
                    break;
                case GRAY:
                    //If is gray we don't color the background. Otherwise we wouldn't see what's written
                    appendColoredString(stringBuilder, PlayerColor.GRAY, "B");
                    break;
                case PURPLE:
                    appendColoredBackgroundString(stringBuilder, PlayerColor.PURPLE, "P");
                    break;
            }
        }
    }

    /**
     * This method appends a coloured string to a given string
     * @param stringBuilder string to which add infos
     * @param color chosen color
     * @param string string to be added
     */
    private void appendColoredString(StringBuilder stringBuilder, PlayerColor color, String string){
        stringBuilder.append(colors.getEscape(color));
        stringBuilder.append(string);
        stringBuilder.append(colors.getEscapeReset());
    }

    /**
     * This method appends a coloured string to a given string
     * @param originalString string to which add infos
     * @param color chosen color
     * @param stringToAdd string to be added
     */
    private void appendColoredString(StringBuilder originalString, CurrencyColor color, String stringToAdd){
        originalString.append(colors.getEscape(color));
        originalString.append(stringToAdd);
        originalString.append(colors.getEscapeReset());
    }

    /**
     * This method appends a string coloured in background to a given string
     * @param originalString string to which add infos
     * @param color chosen color
     * @param stringToAdd string to be added
     */
    private void appendColoredBackgroundString(StringBuilder originalString, CurrencyColor color, String stringToAdd){
        originalString.append(colors.getEscapeBackground(color));
        originalString.append(stringToAdd);
        originalString.append(colors.getEscapeReset());
    }

    /**
     * This method appends a string coloured in background to a given string
     * @param originalString string to which add infos
     * @param color chosen color
     * @param stringToAdd string to be added
     */
    private void appendColoredBackgroundString(StringBuilder originalString, PlayerColor color, String stringToAdd){
        originalString.append(colors.getEscapeBackground(color));
        originalString.append(stringToAdd);
        originalString.append(colors.getEscapeReset());
    }

    /**
     * This player returns the chosen player from the list of all players
     * @param playerToSelect player to select
     * @return the chosen player from the list of all players
     */
    private Player selectPlayer(Player playerToSelect){
        for (Player player : players){
            if (player == playerToSelect){
                return player;
            }
        }
        throw new IllegalArgumentException("Player " + playerToSelect.getNickname() + " not found in players");
    }

    /**
     * This method updates the player's position
     * @param player player to be updated
     * @param r new row
     * @param c new column
     */
    public void movePlayer(Player player, int r, int c) {
        playerLocations.put(selectPlayer(player), new Point(r, c));
    }

    /**
     * this method manages the weapon grabbing from a spawnpoint block
     * @param player player who is grabbing
     * @param weapon weapon to be grabbed
     * @param r row of the block from which to grab
     * @param c row of the block from which to grab
     */
    void grabPlayerWeapon(Player player, String weapon, int r, int c){
        // Here we select the spawnpoint from which remove the weapon grabbed
        getSpawnpointWeapons(r, c).remove(weapon);
        // then we add the weapon to player's wallet
        selectPlayer(player).getWallet().getLoadedWeapons().add(weapon);
    }

    /**
     * This method manages the weapon drop process to a spawnpoint block
     * @param player player who is dropping
     * @param weapon weapon to be dropped
     * @param r row of the block to which drop
     * @param c row of the block to which drop
     */
    void dropPlayerWeapon(Player player, String weapon, int r, int c){
        // Here we select the spawnpoint to which add the weapon dropped
        getSpawnpointWeapons(r, c).add(weapon);
        // then we remove the weapon from player's wallet
        if (selectPlayer(player).getWallet().getLoadedWeapons().contains(weapon)){
            selectPlayer(player).getWallet().getLoadedWeapons().remove(weapon);
        } else selectPlayer(player).getWallet().getUnloadedWeapons().remove(weapon);
    }

    /**
     * This method returns the list of weapons of the chose spawnpoint
     * @param r row of the chosen spawnpoint
     * @param c column of the chosen spawnpoint
     * @return list of weapons of the chosen spawnpoint
     */
    private List<String> getSpawnpointWeapons(int r, int c){
        if (r == 0){
            return weaponsOnSpawnpoint.get(CurrencyColor.RED);
        } else if (c == 0){
            return weaponsOnSpawnpoint.get(CurrencyColor.BLUE);
        } else return weaponsOnSpawnpoint.get(CurrencyColor.YELLOW);

    }

    /**
     * This method updates a player's situation
     * @param player new player's situation
     */
    public void updatePlayer(Player player) {
        for (Player p : players) {
            if (p.getNickname().equals(player.getNickname())) {
                players.set(players.indexOf(p), player);
                break;
            }
        }
    }

    /**
     * This method shows a map whith all the stored infos in this class
     * @param printStream output to which print the map
     */
    public void showUpdatedSituation(PrintStream printStream){
        List<String> updatedBoard = new LinkedList<>(getBoard());
        updatedBoard = positPlayers(updatedBoard);
        updatedBoard = positSpawnpointsWeapons(updatedBoard);
        updatedBoard = positPlayerInfo(updatedBoard);
        for (String line : updatedBoard){
            printStream.print(line);
        }

    }
}
