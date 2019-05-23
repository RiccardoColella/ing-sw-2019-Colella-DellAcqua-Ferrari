package it.polimi.ingsw.server.model.currency;

import com.google.gson.annotations.SerializedName;

import java.security.InvalidParameterException;

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

    public static boolean contains(String string){
        try {
            CurrencyColor.valueOf(string);
            return true;
        } catch (Exception e){
            return false;
        }
    }
}