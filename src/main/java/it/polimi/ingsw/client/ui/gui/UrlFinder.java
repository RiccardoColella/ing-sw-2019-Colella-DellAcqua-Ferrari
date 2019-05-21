package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.shared.datatransferobjects.Powerup;

import java.util.List;

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

    public static String findPowerup(Powerup powerup) {
        return "/assets/powerups/" + powerup.getName() + " " + powerup.getColor().toString() + ".png";
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

    public static String findAvatar(PlayerColor color) {
        return "/assets/avatars/" + color.toString() + ".png";
    }

    public static String findBonusTile(List<CurrencyColor> colors) {
        int red = (int) colors.stream().filter(c -> c.equals(CurrencyColor.RED)).count();
        int blue = (int) colors.stream().filter(c -> c.equals(CurrencyColor.BLUE)).count();
        int yellow = (int) colors.stream().filter(c -> c.equals(CurrencyColor.YELLOW)).count();
        if (red + blue + yellow == 3) {
            CurrencyColor single = red == 1 ? CurrencyColor.RED : yellow == 1 ? CurrencyColor.YELLOW : CurrencyColor.BLUE;
            CurrencyColor couple = red == 2 ? CurrencyColor.RED : yellow == 2 ? CurrencyColor.YELLOW : CurrencyColor.BLUE;
            return "/assets/bonus_tiles/" + single.toString() + "_" + couple.toString() + "_" + couple.toString() + ".png";
        } else if (red + blue + yellow == 2) {
            return "/assets/bouns_tiles/" + colors.get(0).toString() + "_" + colors.get(1).toString() + ".png";
        } else {
            throw new IllegalArgumentException("Unrecognized color set");
        }
    }
}
