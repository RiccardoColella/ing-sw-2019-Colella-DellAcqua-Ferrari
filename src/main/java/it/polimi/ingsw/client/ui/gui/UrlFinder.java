package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.shared.datatransferobjects.Powerup;

import java.util.List;

/**
 * Utility class that composes the paths of each type of graphical element
 *
 * @author Adriana Ferrari
 */
public final class UrlFinder {

    /**
     * Private empty constructor
     */
    private UrlFinder() {}

    /**
     * Finds the path to a player board
     *
     * @param color the color of the board
     * @param isFlipped whether the board is flipped
     * @return the path to the corresponding player board
     */
    public static String findPlayerBoard(PlayerColor color, boolean isFlipped) {
        String end = isFlipped ? "_BACK.png" : ".png";
        return "/assets/player_boards/" + color.toString() + end;
    }

    /**
     * Finds the path to a player tile
     *
     * @param color the color of the tile
     * @param isFlipped whether the tile is flipped
     * @return the path to the corresponding player tile
     */
    public static String findPlayerTile(PlayerColor color, boolean isFlipped) {
        String end = isFlipped ? "_BACK.png" : ".png";
        return "/assets/action_tiles/" + color.toString() + end;
    }

    /**
     * Finds the path to an ammo cube
     *
     * @param color the color of the ammo cube
     * @return the path to the corresponding ammo cube
     */
    public static String findAmmo(CurrencyColor color) {
        return "/assets/ammo/" + color.toString() + ".png";
    }

    /**
     * Finds the path to a powerup
     *
     * @param powerup the desired powerup
     * @return the path to the corresponding powerup
     */
    public static String findPowerup(Powerup powerup) {
        return "/assets/powerups/" + powerup.getName() + " " + powerup.getColor().toString() + ".png";
    }

    /**
     * Finds the path to a weapon
     *
     * @param name the name of the weapon
     * @return the path to the corresponding weapon
     */
    public static String findWeapon(String name) {
        return "/assets/weapons/" + name + ".png";
    }

    /**
     * Finds the path to a token
     *
     * @param color the color of the token
     * @return the path to the corresponding token
     */
    public static String findToken(PlayerColor color) {
        return "/assets/tokens/" + color.toString() + ".png";
    }

    /**
     * Finds the path to a skull
     *
     * @return the path to a skull
     */
    public static String findSkull() {
        return "/assets/skull.png";
    }

    /**
     * Finds the path to an avatar
     *
     * @param color the color of the avatar
     * @return the path to the avatar
     */
    public static String findAvatar(PlayerColor color) {
        return "/assets/avatars/" + color.toString() + ".png";
    }

    /**
     * Finds the path to a bonus tile
     *
     * @param colors the colors contained in the bonus tile
     * @return the path to the corresponding bonus tile
     */
    public static String findBonusTile(List<CurrencyColor> colors) {
        int red = (int) colors.stream().filter(c -> c.equals(CurrencyColor.RED)).count();
        int blue = (int) colors.stream().filter(c -> c.equals(CurrencyColor.BLUE)).count();
        int yellow = (int) colors.stream().filter(c -> c.equals(CurrencyColor.YELLOW)).count();
        if (red + blue + yellow == 3) {
            CurrencyColor single = red == 1 ? CurrencyColor.RED : yellow == 1 ? CurrencyColor.YELLOW : CurrencyColor.BLUE;
            CurrencyColor couple = red == 2 ? CurrencyColor.RED : yellow == 2 ? CurrencyColor.YELLOW : CurrencyColor.BLUE;
            return "/assets/bonus_tiles/" + single.toString() + "_" + couple.toString() + "_" + couple.toString() + ".png";
        } else if (red + blue + yellow == 2) {
            return "/assets/bonus_tiles/" + colors.get(0).toString() + "_" + colors.get(1).toString() + ".png";
        } else {
            throw new IllegalArgumentException("Unrecognized color set");
        }
    }
}
