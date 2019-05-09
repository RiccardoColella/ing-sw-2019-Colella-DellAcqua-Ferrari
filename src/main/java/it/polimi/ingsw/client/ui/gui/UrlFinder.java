package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.utils.Tuple;

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

    public static String findAmmo(CurrencyColor color) {
        return "/assets/ammo/" + color.toString() + ".png";
    }

    public static String findPowerup(Tuple<String, CurrencyColor> powerup) {
        return "/assets/powerups/" + powerup.getItem1() + " " + powerup.getItem2().toString() + ".png";
    }

    public static String findWeapon(String name) {
        return "/assets/weapons/" + name + ".png";
    }

    public static String findToken(PlayerColor color) {
        return "/assets/tokens/" + color.toString() + ".png";
    }

    public static String findSkull() {
        return "/assets/skull.png";
    }
}
