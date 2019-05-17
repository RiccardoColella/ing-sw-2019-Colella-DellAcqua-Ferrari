package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.player.BasicAction;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.shared.viewmodels.Powerup;
import it.polimi.ingsw.utils.EnumValueByString;
import javafx.scene.control.ButtonType;
import javafx.util.Callback;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CallbackFactory {

    private CallbackFactory() {}

    public static Callback<ButtonType, Point> skippablePoint() {
        return button -> {
            if (!button.getButtonData().isCancelButton()) {
                Matcher m = Pattern.compile("(\\d+)\\s(\\d+)").matcher(button.getText());
                if (!m.find()) {
                    throw new IllegalStateException("Invalid spawnpoint response");
                }
                return new Point(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
            }
            return null;
        };
    }

    public static Callback<ButtonType, Point> unskippablePoint() {
        return button -> {
            Matcher m = Pattern.compile("(\\d+)\\s(\\d+)").matcher(button.getText());
            if (!m.find()) {
                throw new IllegalStateException("Invalid spawnpoint response");
            }
            return new Point(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
        };
    }

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

    public static Callback<ButtonType, String> skippableString() {
        return button -> {
            if (!button.getButtonData().isCancelButton()) {
                return button.getText();
            }
            return null;
        };
    }

    public static Callback<ButtonType, String> unskippableString() {
        return ButtonType::getText;
    }

    public static Callback<ButtonType, String> skippableWeapon() {
        return skippableString();
    }

    public static Callback<ButtonType, String> unskippableWeapon() {
        return unskippableString();
    }


    public static Callback<ButtonType, BasicAction> skippableBasicAction() {
        return button -> {
            if (!button.getButtonData().isCancelButton()) {
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
            if (!button.getButtonData().isCancelButton()) {
                return EnumValueByString.findByString(button.getText().toUpperCase(), Direction.class);
            }
            return null;
        };
    }

    public static Callback<ButtonType, Direction> unskippableDirection() {
        return button -> EnumValueByString.findByString(button.getText().toUpperCase(), Direction.class);
    }

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

    public static Callback<ButtonType, CurrencyColor> skippableCurrencyColor() {
        return button -> {
            if (!button.getButtonData().isCancelButton()) {
                return EnumValueByString.findByString(button.getText().toUpperCase(), CurrencyColor.class);
            }
            return null;
        };
    }

    public static Callback<ButtonType, CurrencyColor> unskippableCurrencyColor() {
        return button -> EnumValueByString.findByString(button.getText().toUpperCase(), CurrencyColor.class);
    }
}
