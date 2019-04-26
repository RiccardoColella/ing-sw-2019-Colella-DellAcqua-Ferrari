package it.polimi.ingsw.server.model.currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PowerupTileTest {

    private AmmoCube redAmmo;
    private PowerupTile redTeleporterPowerup;
    private PowerupTile redNewtonPowerup;
    private PowerupTile blueTeleporterPowerup;
    private PowerupTile anotherRedTeleporterPowerup;

    @BeforeEach
    void setUp() {
        this.redAmmo = AmmoCubeFactory.create(CurrencyColor.RED);
        this.redTeleporterPowerup = PowerupTileFactory.create(PowerupTile.Type.TELEPORTER, CurrencyColor.RED);
        this.redNewtonPowerup = PowerupTileFactory.create(PowerupTile.Type.NEWTON, CurrencyColor.RED);
        this.blueTeleporterPowerup = PowerupTileFactory.create(PowerupTile.Type.TELEPORTER, CurrencyColor.BLUE);
        this.anotherRedTeleporterPowerup = PowerupTileFactory.create(PowerupTile.Type.TELEPORTER, CurrencyColor.RED);
    }

    /**
     * This test covers the method equalsTo() in the following situations:
     * - two equal powerups
     * - an ammo and a powerup
     * - two powerups with different type
     * - two powerups with different color
     */
    @Test
    void equalsTo() {
        //two powerups with same color and same type are equal
        assertTrue(redTeleporterPowerup.equalsTo(anotherRedTeleporterPowerup));
        //an ammo and a powerup are not equal even if they are of the same color
        assertFalse(redTeleporterPowerup.equalsTo(redAmmo));
        //two powerups with same color but different type are not equal
        assertFalse(redTeleporterPowerup.equalsTo(redNewtonPowerup));
        //two powerups with same type but different color are not equal
        assertFalse(redTeleporterPowerup.equalsTo(blueTeleporterPowerup));
    }

    /**
     * This test covers the method hasSameValueAs() in the following situations:
     * - two powerups with the same color and type
     * - an ammo and a powerup with the same color
     * - two powerups with the same color
     * - two powerups with the same type
     * - an ammo and a powerup with different colors
     */
    @Test
    void hasSameValueAs() {
        //two powerups of the same color have the same value
        assertTrue(redTeleporterPowerup.hasSameValueAs(anotherRedTeleporterPowerup));
        assertTrue(redTeleporterPowerup.hasSameValueAs(redNewtonPowerup));
        //an ammo and a powerup of the same color have the same value
        assertTrue(redTeleporterPowerup.hasSameValueAs(redAmmo));
        //two powerups of different colors do not have the same value even if they are of the same type
        assertFalse(redTeleporterPowerup.hasSameValueAs(blueTeleporterPowerup));
        //an ammo and a powerup of different colors do not have the same value
        assertFalse(blueTeleporterPowerup.hasSameValueAs(redAmmo));
    }
}