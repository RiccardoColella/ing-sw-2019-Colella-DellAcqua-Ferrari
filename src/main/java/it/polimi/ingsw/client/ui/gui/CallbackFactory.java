package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.shared.datatransferobjects.Powerup;
import it.polimi.ingsw.utils.EnumValueByString;
import javafx.scene.control.ButtonType;
import javafx.util.Callback;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Factory class that provides callback functions for different ButtonTypes, each with the expected return value
 * @author Adriana Ferrari
 */
public final class CallbackFactory {

    /**
     * Empty private constructor
     */
    private CallbackFactory() {}

    /**
     * Builds an unskippable Powerup callback function
     * @return a Callback that returns a Powerup
     */
    public static Callback<ButtonType, Powerup> unskippablePowerup() {
        return button -> {
            Matcher m = Pattern.compile("([^\\s]+)(\\s)(.*)").matcher(button.getText());
            if (!m.find()) {
                throw new IllegalStateException("Invalid spawnpoint response");
            }
            return new Powerup(m.group(3), EnumValueByString.findByString(m.group(1).toUpperCase(), CurrencyColor.class));
        };
    }

    /**
     * Builds a skippable Powerup callback function
     * @return a Callback that returns a Powerup or null
     */
    public static Callback<ButtonType, Powerup> skippablePowerup() {
        return button -> {
            if (!button.getButtonData().isCancelButton()) {
                Matcher m = Pattern.compile("([^\\s]+)(\\s)(.*)").matcher(button.getText());
                if (!m.find()) {
                    throw new IllegalStateException("Invalid spawnpoint response");
                }
                return new Powerup(m.group(3), EnumValueByString.findByString(m.group(1).toUpperCase(), CurrencyColor.class));
            }
            return null;
        };
    }

    /**
     * Builds a skippable String callback function
     * @return a Callback that returns a String or null
     */
    public static Callback<ButtonType, String> skippableString() {
        return button -> {
            if (!button.getButtonData().isCancelButton()) {
                return button.getText();
            }
            return null;
        };
    }

    /**
     * Builds an unskippable String callback function
     * @return a Callback that returns a String
     */
    public static Callback<ButtonType, String> unskippableString() {
        return ButtonType::getText;
    }

    /**
     * Builds a skippable Weapon (in the form of a String) callback function
     * @return a Callback that returns a String or null
     */
    public static Callback<ButtonType, String> skippableWeapon() {
        return skippableString();
    }

    /**
     * Builds an unskippable Weapon (in the form of a String) callback function
     * @return a Callback that returns a String
     */
    public static Callback<ButtonType, String> unskippableWeapon() {
        return unskippableString();
    }

    /**
     * Builds a skippable {@code Set<String>} callback function
     * @return a Callback that returns a {@code Set<String>} or null
     */
    public static Callback<ButtonType, Set<String>> skippableStringSet() {
        return button -> {
            if (!button.getButtonData().isCancelButton()) {
                Matcher m = Pattern.compile("^\\s-\\s(.*)$").matcher(button.getText());
                if (!m.find()) {
                    throw new IllegalStateException("Invalid nickname set response");
                }
                Set<String> playerSet = new HashSet<>();
                do {
                    playerSet.add(m.group(1));
                } while (m.find());

                return playerSet;
            }
            return null;
        };
    }

    /**
     * Builds an unskippable {@code Set<String>} callback function
     * @return a Callback that returns a {@code Set<String>}
     */
    public static Callback<ButtonType, Set<String>> unskippableStringSet() {
        return button -> {
            Matcher m = Pattern.compile("^\\s-\\s(.*)$").matcher(button.getText());
            if (!m.find()) {
                throw new IllegalStateException("Invalid nickname set response");
            }
            Set<String> playerSet = new HashSet<>();
            do {
                playerSet.add(m.group(1));
            } while (m.find());

            return playerSet;
        };
    }

    /**
     * Builds a skippable CurrencyColor callback function
     * @return a Callback that returns a CurrencyColor or null
     */
    public static Callback<ButtonType, CurrencyColor> skippableCurrencyColor() {
        return button -> {
            if (!button.getButtonData().isCancelButton()) {
                return EnumValueByString.findByString(button.getText().toUpperCase(), CurrencyColor.class);
            }
            return null;
        };
    }

    /**
     * Builds an unskippable CurrencyColor callback function
     * @return a Callback that returns a CurrencyColor
     */
    public static Callback<ButtonType, CurrencyColor> unskippableCurrencyColor() {
        return button -> EnumValueByString.findByString(button.getText().toUpperCase(), CurrencyColor.class);
    }
}
