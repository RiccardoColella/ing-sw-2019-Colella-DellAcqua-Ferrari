package it.polimi.ingsw.server.model.currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AmmoCubeTest {

    private AmmoCube redAmmo;
    private PowerupTile redPowerup;
    private AmmoCube anotherRedAmmo;
    private AmmoCube blueAmmo;

    @BeforeEach
    void setUp() {
        this.redAmmo = AmmoCubeFactory.create(CurrencyColor.RED);
        this.redPowerup = PowerupTileFactory.create(PowerupTile.Type.TELEPORTER, CurrencyColor.RED);
        this.anotherRedAmmo = AmmoCubeFactory.create(CurrencyColor.RED);
        this.blueAmmo = AmmoCubeFactory.create(CurrencyColor.BLUE);
    }

    /**
     * This test covers the method equalsTo() in the following situations:
     * - two equal ammos
     * - an ammo and a powerup
     * - two non-equal ammos
     */
    @Test
    void equalsTo() {
        //two ammos of the same color are equal
        assertTrue(redAmmo.equalsTo(anotherRedAmmo));
        //two ammos of different colors are not equal
        assertFalse(redAmmo.equalsTo(blueAmmo));
        //an ammo and a powerup are not equal even if they are of the same color
        assertFalse(redAmmo.equalsTo(redPowerup));
    }

    /**
     * This test covers the method hasSameValueAs() in the following situations:
     * - two ammos with the same value
     * - an ammo and a powerup with the same value
     * - two ammos with different value
     * - an ammo and a powerup with different value
     */
    @Test
    void hasSameValueAs() {
        //two ammos of the same color have the same value
        assertTrue(redAmmo.hasSameValueAs(anotherRedAmmo));
        //an ammo and a powerup of the same color have the same value
        assertTrue(redAmmo.hasSameValueAs(redPowerup));
        //two ammos of different colors do not have the same value
        assertFalse(redAmmo.hasSameValueAs(blueAmmo));
        //an ammo and a powerup of different colors do not have the same value
        assertFalse(blueAmmo.hasSameValueAs(redPowerup));
    }
}