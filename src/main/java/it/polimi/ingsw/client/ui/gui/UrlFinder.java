package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.PlayerColor;

public final class UrlFinder {

    private UrlFinder() {}

    public static String findPlayerBoard(PlayerColor color, boolean isFlipped) {
        String end = isFlipped ? "_BACK.png" : ".png";
        return "/assets/player_boards/" + color.toString() + end;
    }

    public static String findPlayerTile(PlayerColor color, boolean isFlipped) {
        String end = isFlipped ? "_BACK.png" : ".png";
        return "/assets/action_tiles/" + color.toString() + end;
    }
}
