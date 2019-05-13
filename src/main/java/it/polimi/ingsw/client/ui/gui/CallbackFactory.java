package it.polimi.ingsw.client.ui.gui;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.utils.EnumValueByString;
import it.polimi.ingsw.utils.Tuple;
import javafx.scene.control.ButtonType;
import javafx.util.Callback;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CallbackFactory {

    public static Callback<ButtonType, Tuple<String, CurrencyColor>> unskippablePowerup() {
        return button -> {
            Matcher m = Pattern.compile("([^\\s])+(\\s)(.)*").matcher(button.getText());
            if (!m.find()) {
                throw new IllegalStateException("Unvalid spawnpoint response");
            }
            return new Tuple<>(m.group(3), EnumValueByString.findByString(m.group(1).toUpperCase(), CurrencyColor.class));
        };
    }

    public static Callback<ButtonType, Tuple<String, CurrencyColor>> skippablePowerup() {
        return button -> {
            if (!button.getText().equals("Skip")) {
                Matcher m = Pattern.compile("([^\\s])+(\\s)(.)*").matcher(button.getText());
                if (!m.find()) {
                    throw new IllegalStateException("Unvalid spawnpoint response");
                }
                return new Tuple<>(m.group(3), EnumValueByString.findByString(m.group(1).toUpperCase(), CurrencyColor.class));
            }
            return null;
        };
    }
}
