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

    /**
     * Checks whether or not a string is the representation of one of the values in this enum
     *
     * @param string the string to check
     * @return true if the string is the representation of one of the values in this enum
     */
    public static boolean contains(String string){
        try {
            PlayerColor.valueOf(string);
            return true;
        } catch (Exception e){
            return false;
        }
    }
}
