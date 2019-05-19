package it.polimi.ingsw.client.ui.cli;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.PlayerColor;

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

    String getEscapeReset(){
        return ANSI_RESET;
    }

    String getEscape(CurrencyColor color){
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

    String getEscape(PlayerColor color){
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

    String getEscapeBackground(CurrencyColor color){
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

    String getEscapeBackground(PlayerColor color){
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



}
