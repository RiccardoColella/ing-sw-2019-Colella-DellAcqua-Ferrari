package it.polimi.ingsw.client.ui.cli;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.shared.datatransferobjects.BonusTile;
import it.polimi.ingsw.shared.datatransferobjects.Player;
import it.polimi.ingsw.shared.datatransferobjects.Powerup;
import it.polimi.ingsw.utils.Tuple;
import org.jetbrains.annotations.Contract;

import java.awt.*;
import java.io.PrintStream;
import java.util.List;
import java.util.*;

class GameRepresentation {


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
     * This map stores the data about the bonus on the game board
     */
    private Map<Point, List<CurrencyColor>> bonusMap;

    /**
     * This list stores the kills executed by every player in the correct order
     */
    private List<Tuple<PlayerColor, Boolean>> killshots;

    /**
     * This property stores all player's positions
     */
    Map<Player, Point> playerLocations = new HashMap<>();

    /**
     * This property stores the board as a list of strings
     */
    private List<String> board;

    private final RepresentationSettings settings;

    static class RepresentationSettings {
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
         * This property stores the max length in char for a name to be written in a block
         */
        private final int maxNicknamesLength;

        RepresentationSettings(
                int rowOffset,
                int rowDistance,
                int columnOffset,
                int columnDistance,
                int maxNicknamesLength
        ) {
            this.rowOffset = rowOffset;
            this.rowDistance = rowDistance;
            this.columnOffset = columnOffset;
            this.columnDistance = columnDistance;
            this.maxNicknamesLength = maxNicknamesLength;
        }

        private int getRowOffset() {
            return rowOffset;
        }

        private int getColumnOffset() {
            return columnOffset;
        }

        private int getRowDistance() {
            return rowDistance;
        }

        private int getColumnDistance() {
            return columnDistance;
        }

        private int getMaxNicknamesLength() {
            return maxNicknamesLength;
        }
    }


    /**
     * This property stores info about alive players
     */
    private Map<String, Boolean> alivePlayers;

    GameRepresentation(
            List<String>  board,
            List<Player> players,
            int skulls,
            Map<CurrencyColor, List<String>> weaponsOnSpawnpoint,
            Set<BonusTile> bonusTiles,

            RepresentationSettings settings

    ) {
        this.board = board;
        this.players = players;
        this.skulls = skulls;
        this.killshots = new LinkedList<>();
        this.weaponsOnSpawnpoint = weaponsOnSpawnpoint;
        this.bonusMap = new HashMap<>();
        initializeBonusMap(bonusTiles);

        alivePlayers = new HashMap<>();
        for (Player player : players){
            alivePlayers.put(player.getNickname(), false);
        }

        this.settings = settings;

    }

    /**
     * This functions initialize the bonus map when the match is starting
     * @param bonusTiles the collection of all ammocubes and powerups on the board, and their locations
     */
    private void initializeBonusMap(Collection<BonusTile> bonusTiles) {
        for (BonusTile tile : bonusTiles){
            bonusMap.put(tile.getLocation(), tile.getAmmoCubes());
        }
    }

    /**
     * This method removes a bonus from bonus the map
     * @param tile the bonus to be added
     */
    void removeBonusFromMap(BonusTile tile) {
        bonusMap.remove(tile.getLocation());
    }

    /**
     * This method adds a bonus to the bonus map
     * @param tile the bonus to be added
     */
    void addBonusToMap(BonusTile tile) {
        bonusMap.put(tile.getLocation(), tile.getAmmoCubes());
    }

    /**
     * Board getter
     * @return the board
     */
    List<String> getBoard() {
        return board;
    }


    /**
     * This method returns the row offset
     * @return the row offset
     */
    @Contract(pure = true)
    private int getRowOffset() { return this.settings.getRowOffset(); }

    /**
     * This method returns the column offset
     * @return the column offset
     */
    @Contract(pure = true)
    private int getColumnOffset() { return this.settings.getColumnOffset(); }

    /**
     * This method returns the max nickname length to be represented on the board
     * @return the max nickname length to be represented on the board
     */
    @Contract(pure = true)
    private int getMaxNicknamesLength() { return this.settings.getMaxNicknamesLength(); }

    /**
     * This method returns the row distance from one block to another
     * @return the row distance
     */
    @Contract(pure = true)
    private int getRowDistance() { return settings.getRowDistance(); }

    /**
     * This method returns the column distance from one block to another
     * @return the column distance
     */
    @Contract(pure = true)
    private int getColumnDistance() { return this.settings.getColumnDistance(); }

    /**
     * This method returns the list of all players
     * @return the list of all players
     */
    protected List<Player> getPlayers() { return this.players; }

    /**
     * This method prints the given board to the given output
     * @param board board to be printed
     * @param printStream output stream to which print to
     */
    void printBoard(List<String> board, PrintStream printStream) {
        for (String line : board){
            printStream.print(line);
        }
    }

    /**
     * This method prints the board as caught from the json file
     * @param printStream output stream to which print to
     */
    void printEmptyBoard(PrintStream printStream) {
        for (String line : board){
            printStream.print(line);
        }
    }

    /**
     * This method sets a player as alive
     * @param playerAlive player to be set as alive
     */
    void setPlayerAlive(Player playerAlive){
        alivePlayers.put(playerAlive.getNickname(), true);
    }

    /**
     * This method sets a player as alive
     * @param playerAlive player to be set as alive
     */
    void setPlayerAlive(String playerAlive){
        alivePlayers.put(playerAlive, true);
    }

    /**
     * This method sets a player as alive
     * @param playerAlive player to be set as alive
     */
    void setPlayerAlive(PlayerColor playerAlive){
        alivePlayers.put(selectPlayer(playerAlive).getNickname(), true);
    }

    /**
     * This method sets a player as died
     * @param playerDied player to be setted as died
     */
    void setPlayerDied(Player playerDied){
        alivePlayers.put(playerDied.getNickname(), false);
    }


    /**
     * This method sets a player as died
     * @param playerDied player to be setted as died
     */
    void setPlayerDied(String playerDied) {
        alivePlayers.put(playerDied, false);
    }

    /**
     * This method update the killshots variable
     * @param killshots new killshot track to be setted
     */
    void setKillshots(List<Tuple<PlayerColor, Boolean>> killshots){
        this.killshots = killshots;
    }

    /**
     * This method builds a board containing all the player in this.playerLocations variable
     * @param board board to which add the players
     * @return the built board
     */
    private List<String> positPlayers(List<String> board) {
        List<String> boardWithPlayers = new LinkedList<>(board);
        for (Player player : players){
            if (alivePlayers.get(selectPlayer(player).getNickname())){
                int i = players.indexOf(player);
                String nick = player.getNickname();
                if (nick.length() > getMaxNicknamesLength()){
                    nick = nick.substring(0, getMaxNicknamesLength());
                }
                int x = playerLocations.get(player).x;
                int y = playerLocations.get(player).y;
                // Row is the general offset for rows + the player's row + the distance of the needed block
                int row = getRowOffset() + i + getRowDistance()*x;
                // Column is the general column offset + the distance of the needed block
                int column = getColumnOffset() + getColumnDistance()*y;
                String line = board.get(row);
                String newLine = line.substring(0, column) +
                        ANSIColor.getEscape(player.getColor()) +
                        ANSIColor.getEscapeBold() +
                        nick +
                        ANSIColor.getEscapeReset() +
                        line.substring(column + nick.length(), line.length()-1) +
                        "\n";
                boardWithPlayers.set(row, newLine);
            }
        }
        return boardWithPlayers;
    }

    /**
     * This method posit on the return list of strings the kill-shots track as last line of the given board
     * @param board the list of strings to which add the kill-shots track
     * @return the list of strings with the kill-shots track added as last element
     */
    private List<String> positKillshots(List<String> board){
        List<String> boardWithKillshots = new LinkedList<>(board);
        String killshotsString = " - Killshots: [";
        StringBuilder killshotsLine = new StringBuilder(killshotsString);
        for (Tuple<PlayerColor, Boolean> kill : killshots){
            if (kill.getItem2()){
                appendColoredBoldBackgroundString(killshotsLine, kill.getItem1(), "K");
            } else appendColoredBackgroundString(killshotsLine, kill.getItem1(), "k");
        }
        appendBoldRepeatedString(killshotsLine, "-", skulls - killshots.size());
        killshotsLine.append("]");
        boardWithKillshots.add(killshotsLine.toString());
        return boardWithKillshots;
    }

    /**
     * This method builds a board containing all the weapons in this.weaponsOnSpawnpoint variable
     * @param board board to which add the players
     * @return the built board
     */
    private List<String> positSpawnpointsWeapons(List<String> board) {
        List<String> boardWithWeapons = new LinkedList<>(board);
        String separator = " - ";
        weaponsOnSpawnpoint
                .forEach( (spawnpointColor, weapons) -> {
                    int index =
                            getRowOffset() - 1 +
                            getRowDistance()* Arrays.asList(CurrencyColor.values()).indexOf(spawnpointColor);
                    String line = board.get(index);
                    line = line.substring(0, line.length() - 2) +
                            ANSIColor.getEscape(spawnpointColor) +
                            " " + spawnpointColor.toString() + "SPAWNPOINT" +
                            ANSIColor.getEscapeReset() +
                            "\n";
                    boardWithWeapons.set(index, line);
                    for (String weapon : weapons) {
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
    private List<String> positPlayerInfo(List<String> board) {
        List<String> boardUpdated = positKillshots(board);
        String separator = ". ";
        for (Player player : players){
            List<String> linesToAdd = new LinkedList<>();
            // First line
            linesToAdd.add("\n");

            // Second line
            StringBuilder newLine = new StringBuilder();
            newLine.append(" - ");
            // coloured Nickname
            appendColoredBoldString(newLine, player.getColor(), player.getNickname());
            newLine.append(separator);
            // coloured color
            appendColoredString(newLine, player.getColor(), player.getColor().toString());
            newLine.append(separator);
            // Skulls, damages and marks
            addPlayerHealth(newLine, player, separator);
            newLine.append(" BoardFlipped: ");
            newLine.append(player.isBoardFlipped());
            newLine.append(" ActionsTileFlipped: ");
            newLine.append(player.isTileFlipped());
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
    private void addPlayerHealth(StringBuilder stringBuilder, final Player player, String infoSeparator) {
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
    private void appendLettersOnColoredBackground(StringBuilder stringBuilder, List<PlayerColor> list) {
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
    private void appendColoredString(StringBuilder stringBuilder, PlayerColor color, String string) {
        stringBuilder.append(ANSIColor.getEscape(color));
        stringBuilder.append(string);
        stringBuilder.append(ANSIColor.getEscapeReset());
    }

    /**
     * This method appends a coloured bold string to a given string
     * @param stringBuilder string to which add infos
     * @param color chosen color
     * @param string string to be added
     */
    private void appendColoredBoldString(StringBuilder stringBuilder, PlayerColor color, String string) {
        stringBuilder.append(ANSIColor.getEscape(color));
        appendBoldRepeatedString(stringBuilder, string, 1);
    }

    /**
     * This method appends a coloured bold string to a given string
     * @param stringBuilder string to which add infos
     * @param color chosen color
     * @param string string to be added
     */
    private void appendColoredBoldBackgroundString(StringBuilder stringBuilder, PlayerColor color, String string) {
        stringBuilder.append(ANSIColor.getEscapeBackground(color));
        appendBoldRepeatedString(stringBuilder, string, 1);
    }

    /**
     * This method appends a coloured bold string to a given string
     * @param stringBuilder string to which add infos
     * @param string string to be added
     * @param times the number of times the string needs to be repeated
     */
    private void appendBoldRepeatedString(StringBuilder stringBuilder, String string, int times) {
        stringBuilder.append(ANSIColor.getEscapeBold());
        for (int i = 0; i < times; i++){
            stringBuilder.append(string);
        }
        stringBuilder.append(ANSIColor.getEscapeReset());
    }

    /**
     * This method appends a coloured string to a given string
     * @param originalString string to which add infos
     * @param color chosen color
     * @param stringToAdd string to be added
     */
    private void appendColoredString(StringBuilder originalString, CurrencyColor color, String stringToAdd) {
        originalString.append(ANSIColor.getEscape(color));
        originalString.append(stringToAdd);
        originalString.append(ANSIColor.getEscapeReset());
    }

    /**
     * This method appends a string coloured in background to a given string
     * @param originalString string to which add infos
     * @param color chosen color
     * @param stringToAdd string to be added
     */
    private void appendColoredBackgroundString(StringBuilder originalString, CurrencyColor color, String stringToAdd) {
        originalString.append(ANSIColor.getEscapeBackground(color));
        originalString.append(stringToAdd);
        originalString.append(ANSIColor.getEscapeReset());
    }

    /**
     * This method appends a string coloured in background to a given string
     * @param originalString string to which add infos
     * @param color chosen color
     * @param stringToAdd string to be added
     */
    private void appendColoredBackgroundString(StringBuilder originalString, PlayerColor color, String stringToAdd) {
        originalString.append(ANSIColor.getEscapeBackground(color));
        originalString.append(stringToAdd);
        originalString.append(ANSIColor.getEscapeReset());
    }

    /**
     * This player returns the chosen player from the list of all players
     * @param playerToSelect player to select
     * @return the chosen player from the list of all players
     */
    private Player selectPlayer(Player playerToSelect) {
        for (Player player : players){
            if (player.getNickname().equals(playerToSelect.getNickname())){
                return player;
            }
        }
        throw new IllegalArgumentException("Player " + playerToSelect.getNickname() + " not found in players");
    }

    /**
     * This player returns the chosen player from the list of all players
     * @param playerToSelect player to select
     * @return the chosen player from the list of all players
     */
    private Player selectPlayer(PlayerColor playerToSelect) {
        for (Player player : players){
            if (player.getColor().equals(playerToSelect)){
                return player;
            }
        }
        throw new IllegalArgumentException("PlayerColor " + playerToSelect.toString() + " not found in players");
    }

    /**
     * This method updates the player's position
     * @param player player to be updated
     * @param r new row
     * @param c new column
     */
    void movePlayer(Player player, int r, int c) {
        playerLocations.put(selectPlayer(player), new Point(r, c));
    }

    /**
     * This method updates the player's position
     * @param player player to be updated
     * @param r new row
     * @param c new column
     */
    void movePlayer(PlayerColor player, int r, int c) {
        movePlayer(selectPlayer(player), r, c);
    }

    /**
     * this method manages the weapon grabbing from a spawnpoint block
     * @param player player who is grabbing
     * @param weapon weapon to be grabbed
     * @param r row of the block from which to grab
     * @param c row of the block from which to grab
     */
    void grabPlayerWeapon(Player player, String weapon, int r, int c) {
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
    void dropPlayerWeapon(Player player, String weapon, int r, int c) {
        // Here we select the spawnpoint to which add the weapon dropped
        getSpawnpointWeapons(r, c).add(weapon);
        // then we remove the weapon from player's wallet
        if (selectPlayer(player).getWallet().getLoadedWeapons().contains(weapon)){
            selectPlayer(player).getWallet().getLoadedWeapons().remove(weapon);
        } else selectPlayer(player).getWallet().getUnloadedWeapons().remove(weapon);
    }

    /**
     * This string adds a weapon to a spawnpoint
     * @param weapon weapon to be added to the spawnpoint
     * @param r row of the spawnpoint
     * @param c column of the spawnpoint
     * @return the spawnpoint color as string
     */
    String addWeaponToSpawnpoint(String weapon, int r, int c) {
        List<String> spawnpointWeapons = getSpawnpointWeapons(r, c);
        if (!spawnpointWeapons.contains(weapon)){
            spawnpointWeapons.add(weapon);
        }
        return getSpawnpointColor(r, c).toString();
    }
    /**
     * This method returns the list of weapons of the chose spawnpoint
     * @param r row of the chosen spawnpoint
     * @param c column of the chosen spawnpoint
     * @return list of weapons of the chosen spawnpoint
     */
    private List<String> getSpawnpointWeapons(int r, int c) {
        if (r == 0){
            return weaponsOnSpawnpoint.get(CurrencyColor.BLUE);
        } else if (c == 0){
            return weaponsOnSpawnpoint.get(CurrencyColor.RED);
        } else return weaponsOnSpawnpoint.get(CurrencyColor.YELLOW);

    }

    /**
     * This method returns the color of the spawnpoint given it's position
     * @param r the row of the spawnpoint
     * @param c the column of the spawnpoint
     * @return the spawnpoint's color
     */
    private CurrencyColor getSpawnpointColor(int r, int c) {
        if (r == 0){
            return CurrencyColor.RED;
        } else if (c == 0){
            return CurrencyColor.BLUE;
        } else return CurrencyColor.YELLOW;
    }

    /**
     * This method updates a player's situation
     * @param player new player's situation
     */
    void updatePlayer(Player player) {
        for (Player p : players) {
            if (p.getNickname().equals(player.getNickname())) {
                players.set(players.indexOf(p), player);
                break;
            }
        }
    }

    /**
     * This method shows a map with all the stored infos in this class
     * @param printStream output to which print the map
     */
    void showUpdatedSituation(PrintStream printStream) {
        List<String> updatedBoard = new LinkedList<>(getBoard());
        updatedBoard = positPlayers(updatedBoard);
        updatedBoard = positSpawnpointsWeapons(updatedBoard);
        updatedBoard = positPlayerInfo(updatedBoard);
        for (String line : updatedBoard){
            printStream.print(ANSIColor.parseLettersToBackground(line));
        }
    }
    /**
     * This method shows a map with the stored infos in this class without detailed players's info
     * @param printStream output to which print the map
     */
    void showUpdatedMap(PrintStream printStream) {
        List<String> updatedBoard = new LinkedList<>(getBoard());
        updatedBoard = positPlayers(updatedBoard);
        updatedBoard = positSpawnpointsWeapons(updatedBoard);
        for (String line : updatedBoard){
            printStream.print(ANSIColor.parseLettersToBackground(line));
        }
    }

    /**
     * This method prints on the given print stream the players's info
     * @param printStream the print stream to which print the players's info
     */
    void showPlayersInfo(PrintStream printStream) {
        List<String> playersInfo = positPlayerInfo(new LinkedList<>());
        for (String line : playersInfo){
            printStream.print(line);
        }
    }

    /**
     * This method shows the situations of ammos on the board
     * @param printStream stream to which print the info
     */
    void showBonusMap(PrintStream printStream) {
        List<String> boardToPrint = new LinkedList<>(getBoard());
        int ammoToDisplayLenght = getColumnDistance()/3;
        for (Map.Entry<Point, List<CurrencyColor>> bonusTileEntry : bonusMap.entrySet()) {
            int x = (int) (getColumnOffset() + bonusTileEntry.getKey().getX()*getColumnDistance() + 4);
            int y = (int) (getRowOffset()    + bonusTileEntry.getKey().getY()*getRowDistance());
            int i = 0;
            for (CurrencyColor color : bonusTileEntry.getValue()){
                StringBuilder ammoToDisplay = new StringBuilder();
                for (int k = 0; k < ammoToDisplayLenght; k++){
                    ammoToDisplay.append(ANSIColor.parseSymbolToBeParsedAsColor(color));
                }
                String newLine = boardToPrint.get(y + i);
                newLine = newLine.substring(0, x) + ammoToDisplay.toString() + newLine.substring(x + ammoToDisplayLenght);
                boardToPrint.remove(y + i);
                boardToPrint.add(y + i, newLine);
                i++;
            }
            if (bonusTileEntry.getValue().size() < 3) {
                String powerupString = "Pow-Up";
                String newLine = boardToPrint.get(y + i);
                newLine = newLine.substring(0, x) + powerupString + newLine.substring(x + powerupString.length());
                boardToPrint.remove(y + i);
                boardToPrint.add(y + i, newLine);
            }
        }
        for (String line : boardToPrint){
            printStream.print(ANSIColor.parseLettersToBackground(line));
        }
    }

}
