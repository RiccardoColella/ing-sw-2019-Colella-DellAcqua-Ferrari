package it.polimi.ingsw.server.model.currency;

/**
 * This enum contains the possible coin colors
 */
public enum CurrencyColor {
    RED,
    BLUE,
    YELLOW;

    public static CurrencyColor findByString(String s) {
        for (CurrencyColor color: CurrencyColor.values()) {
            if (s.equals(color.toString())) {
                return color;
            }
        }

        throw new IllegalArgumentException();
    }
}