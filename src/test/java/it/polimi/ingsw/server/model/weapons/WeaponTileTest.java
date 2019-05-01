package it.polimi.ingsw.server.model.weapons;

import it.polimi.ingsw.server.model.currency.AmmoCube;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeaponTileTest {

    /**
     * This test verifies that the WeaponTile copy constructor returns a weapon that is an exact copy
     */
    @Test
    void copyConstructor() {
        for (WeaponTile.Name name : WeaponTile.Name.values()) {

            List<AmmoCube> cost1 = Arrays.asList(new AmmoCube(CurrencyColor.RED), new AmmoCube(CurrencyColor.YELLOW), new AmmoCube(CurrencyColor.BLUE));
            List<AmmoCube> cost2 = Collections.singletonList(new AmmoCube(CurrencyColor.BLUE));

            WeaponTile copy = new WeaponTile(new WeaponTile(name, cost1, cost2));
            assertEquals(name, copy.getName(), "Incorrect weapon name");
            assertEquals(cost1, copy.getAcquisitionCost(), "Incorrect acquisition cost");
            assertEquals(cost2, copy.getReloadCost(), "Incorrect reload cost");
        }
    }

}