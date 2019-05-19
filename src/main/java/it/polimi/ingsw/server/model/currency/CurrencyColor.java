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
    YELLOW
}