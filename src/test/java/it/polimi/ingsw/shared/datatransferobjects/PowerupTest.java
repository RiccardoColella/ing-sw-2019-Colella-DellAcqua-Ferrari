package it.polimi.ingsw.shared.datatransferobjects;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PowerupTest {

    @Test
    void toStringTest() {
        Powerup powerup = new Powerup("PowerupName", CurrencyColor.YELLOW);
        assertEquals("PowerupName (Color: YELLOW)", powerup.toString());
        powerup = new Powerup("DifferentName", CurrencyColor.RED);
        assertEquals("DifferentName (Color: RED)", powerup.toString());
    }
}