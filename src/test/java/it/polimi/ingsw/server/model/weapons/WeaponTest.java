package it.polimi.ingsw.server.model.weapons;

import it.polimi.ingsw.server.model.currency.Ammo;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WeaponTest {

    @Test
    void copyConstructor() {
        for (Weapon.Name name : Weapon.Name.values()) {

            List<Ammo> cost1 = Arrays.asList(new Ammo(CurrencyColor.RED), new Ammo(CurrencyColor.YELLOW), new Ammo(CurrencyColor.BLUE));
            List<Ammo> cost2 = Arrays.asList(new Ammo(CurrencyColor.BLUE));

            Weapon copy = new Weapon(new Weapon(name, cost1, cost2));
            assertEquals(name, copy.getName(), "Incorrect weapon name");
            assertEquals(cost1, copy.getAcquisitionCost(), "Incorrect acquisition cost");
            assertEquals(cost2, copy.getReloadCost(), "Incorrect reload cost");
        }
    }

}