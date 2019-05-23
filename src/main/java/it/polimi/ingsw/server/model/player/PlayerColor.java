package it.polimi.ingsw.server.model.player;

/**
 * This enum represents the colors of the player pawns
 */
public enum PlayerColor {
    YELLOW,
    GREEN,
    PURPLE,
    GRAY,
    TURQUOISE;

    public static boolean contains(String string){
        try {
            PlayerColor.valueOf(string);
            return true;
        } catch (Exception e){
            return false;
        }
    }
}
