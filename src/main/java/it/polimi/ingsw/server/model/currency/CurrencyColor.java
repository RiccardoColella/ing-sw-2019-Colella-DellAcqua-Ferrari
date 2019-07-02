package it.polimi.ingsw.server.model.currency;

import com.google.gson.annotations.SerializedName;

/**
 * This enum contains the possible coin colors
 */
public enum CurrencyColor {
    @SerializedName("BLUE")
    BLUE,
    @SerializedName("RED")
    RED,
    @SerializedName("YELLOW")
    YELLOW;

    /**
     * States whether there is an enum value matching the given string
     *
     * @param string the string version of the enum
     * @return true if there is an enum value matching the given string
     */
    public static boolean contains(String string){
        try {
            CurrencyColor.valueOf(string);
            return true;
        } catch (Exception e){
            return false;
        }
    }
}