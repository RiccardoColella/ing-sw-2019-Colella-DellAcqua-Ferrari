package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.BasicAction;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.shared.viewmodels.Powerup;
import it.polimi.ingsw.utils.EnumValueByString;
import javafx.scene.control.ButtonType;
import javafx.util.Callback;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CallbackFactory {

    private CallbackFactory() {}

    public static Callback<ButtonType, Powerup> unskippablePowerup() {
        return button -> {
            Matcher m = Pattern.compile("([^\\s]+)(\\s)(.*)").matcher(button.getText());
            if (!m.find()) {
                throw new IllegalStateException("Invalid spawnpoint response");
            }
            return new Powerup(m.group(3), EnumValueByString.findByString(m.group(1).toUpperCase(), CurrencyColor.class));
        };
    }

    public static Callback<ButtonType, Powerup> skippablePowerup() {
        return button -> {
            if (!button.getText().equals("Skip")) {
                Matcher m = Pattern.compile("([^\\s]+)(\\s)(.*)").matcher(button.getText());
                if (!m.find()) {
                    throw new IllegalStateException("Invalid spawnpoint response");
                }
                return new Powerup(m.group(3), EnumValueByString.findByString(m.group(1).toUpperCase(), CurrencyColor.class));
            }
            return null;
        };
    }

    private static Callback<ButtonType, String> skippableString() {
        return button -> {
            if (!button.getText().equals("Skip")) {
                return button.getText();
            }
            return null;
        };
    }

    private static Callback<ButtonType, String> unskippableString() {
        return ButtonType::getText;
    }

    public static Callback<ButtonType, String> skippablePaymentMethod() {
        return skippableString();
    }

    public static Callback<ButtonType, String> unskippablePaymentMethod() {
        return unskippableString();
    }

    public static Callback<ButtonType, String> skippableWeapon() {
        return skippableString();
    }

    public static Callback<ButtonType, String> unskippableWeapon() {
        return unskippableString();
    }


    public static Callback<ButtonType, BasicAction> skippableBasicAction() {
        return button -> {
            if (!button.getText().equals("Skip")) {
                return EnumValueByString.findByString(button.getText().toUpperCase(), BasicAction.class);
            }
            return null;
        };
    }

    public static Callback<ButtonType, BasicAction> unskippableBasicAction() {
        return button -> EnumValueByString.findByString(button.getText().toUpperCase(), BasicAction.class);
    }

    public static Callback<ButtonType, Direction> skippableDirection() {
        return button -> {
            if (!button.getText().equals("Skip")) {
                return EnumValueByString.findByString(button.getText().toUpperCase(), Direction.class);
            }
            return null;
        };
    }

    public static Callback<ButtonType, Direction> unskippableDirection() {
        return button -> EnumValueByString.findByString(button.getText().toUpperCase(), Direction.class);
    }
}
