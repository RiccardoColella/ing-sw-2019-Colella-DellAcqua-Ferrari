package it.polimi.ingsw.client.ui.cli;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.PlayerColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ANSIColor {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";

    private static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    private static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    private static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    private static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    private static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    private static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    private static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";

    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_UNDERLINED = "\u001B[4m";
    private static final String ANSI_REVERSED = "\u001B[7m";

    private ANSIColor(){
        throw new IllegalStateException("An utility class, as ANSIColor is, should not be constructed");
    }

    /**
     * This method returns the ANSI_RESET value
     * @return the ANSI_RESET value
     */
    static String getEscapeReset(){
        return ANSI_RESET;
    }

    /**
     * This method returns the ANSI escape for the given color
     * @param color selected color
     * @return the ANSI escape for the given color
     */
    static String getEscape(CurrencyColor color){
        switch (color){
            case YELLOW:
                return ANSI_YELLOW;
            case BLUE:
                return ANSI_BLUE;
            case RED:
                return ANSI_RED;
            default:
                return ANSI_RESET;
        }
    }

    /**
     * This method returns the ANSI escape for the given color
     * @param color selected color
     * @return the ANSI escape for the given color
     */
    static String getEscape(PlayerColor color){
        switch (color){
            case YELLOW:
                return ANSI_YELLOW;
            case GRAY:
                return ANSI_BLACK;
            case PURPLE:
                return ANSI_PURPLE;
            case GREEN:
                return ANSI_GREEN;
            case TURQUOISE:
                return ANSI_CYAN;
            default:
                return ANSI_RESET;
        }
    }

    /**
     * This method returns the ANSI escape for the given background color
     * @param color selected color
     * @return the ANSI escape for the given background color
     */
    static String getEscapeBackground(CurrencyColor color){
        switch (color){
            case YELLOW:
                return ANSI_YELLOW_BACKGROUND;
            case BLUE:
                return ANSI_BLUE_BACKGROUND;
            case RED:
                return ANSI_RED_BACKGROUND;
            default:
                return ANSI_RESET;
        }
    }

    /**
     * This method returns the ANSI escape for the given background color
     * @param color selected color
     * @return the ANSI escape for the given background color
     */
    static String getEscapeBackground(PlayerColor color){
        switch (color){
            case YELLOW:
                return ANSI_YELLOW_BACKGROUND;
            case GRAY:
                return ANSI_BLACK_BACKGROUND;
            case PURPLE:
                return ANSI_PURPLE_BACKGROUND;
            case GREEN:
                return ANSI_GREEN_BACKGROUND;
            case TURQUOISE:
                return ANSI_CYAN_BACKGROUND;
            default:
                return ANSI_RESET;
        }
    }

    /**
     * This method returns the ANSI escape for the bold writing
     * @return the ANSI escape for the bold writing
     */
    static String getEscapeBold() {
        return ANSI_BOLD;
    }

    /**
     * This method returns the ANSI escape for the underlined writing
     * @return the ANSI escape for the underlined writing
     */
    static String getEscapeUnderlined() {
        return ANSI_UNDERLINED;
    }

    /**
     * This method returns the ANSI escape for the reversed writing
     * @return the ANSI escape for the reversed writing
     */
    static String getEscapeReversed() {
        return ANSI_REVERSED;
    }

    /**
     * This method parse colors for string using formats as WordToBeWrittenInColor (Color: COLOR)
     * @param stringToParse the string to be parsed
     * @return the colored string
     */
    static String parseColor(String stringToParse) {
        Matcher m = Pattern.compile(".*(\\w*)(Color: ([A-Z]+))").matcher(stringToParse);
        if (m.find()) {
            if (CurrencyColor.contains(m.group(3))){
                return getEscape(CurrencyColor.valueOf(m.group(3))) + stringToParse + getEscapeReset();
            } else if (PlayerColor.contains(m.group(3))){
                return getEscape(PlayerColor.valueOf(m.group(3))) + stringToParse + getEscapeReset();
            }
        }
        return stringToParse;
    }

    /**
     * This method parses some special characters to a background-colored space
     * @param line the String to be parsed
     * @return the parsed String
     */
    static String parseLettersToBackground(String line) {
        line = line.replace("ρ", ANSI_RED_BACKGROUND + " " + getEscapeReset());
        line = line.replace("γ", ANSI_GREEN_BACKGROUND + " " + getEscapeReset());
        line = line.replace("ψ", ANSI_YELLOW_BACKGROUND + " " + getEscapeReset());
        line = line.replace("β", ANSI_BLUE_BACKGROUND + " " + getEscapeReset());
        line = line.replace("π", ANSI_PURPLE_BACKGROUND + " " + getEscapeReset());
        line = line.replace("χ", ANSI_CYAN_BACKGROUND + " " + getEscapeReset());
        line = line.replace("κ", ANSI_BLACK_BACKGROUND + " " + getEscapeReset());
        return line;
    }

    /**
     * This method encodes special colors to characters
     * @param color color to be encoded
     * @return the special character encoded
     */
    static String parseSymbolToBeParsedAsColor(CurrencyColor color) {
        switch (color) {
            case YELLOW:
                return "ψ";
            case RED:
                return "ρ";
            case BLUE:
                return "χ";
            default:
                return "";
        }
    }
}
