package it.polimi.ingsw.server.model.weapons;

import it.polimi.ingsw.server.model.currency.AmmoCube;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WeaponTest {

    @Test
    void copyConstructor() {
        for (Weapon.Name name : Weapon.Name.values()) {

            List<AmmoCube> cost1 = Arrays.asList(new AmmoCube(CurrencyColor.RED), new AmmoCube(CurrencyColor.YELLOW), new AmmoCube(CurrencyColor.BLUE));
            List<AmmoCube> cost2 = Arrays.asList(new AmmoCube(CurrencyColor.BLUE));

            Weapon copy = new Weapon(new Weapon(name, cost1, cost2));
            assertEquals(name, copy.getName(), "Incorrect weapon name");
            assertEquals(cost1, copy.getAcquisitionCost(), "Incorrect acquisition cost");
            assertEquals(cost2, copy.getReloadCost(), "Incorrect reload cost");
        }
    }

}