package it.polimi.ingsw.server.model.battlefield;

import it.polimi.ingsw.server.model.collections.Deck;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.weapons.Weapon;
import it.polimi.ingsw.server.model.weapons.WeaponFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SpawnpointBlockTest {

    private SpawnpointBlock block;
    private Deck<Weapon> weapons;

    @BeforeEach
    void setUp() {
        //Creating a blue spawnpoint with max number of weapons 3
        block = new SpawnpointBlock(0,0, Block.BorderType.NONE, Block.BorderType.NONE, Block.BorderType.NONE, Block.BorderType.NONE, CurrencyColor.BLUE, 3);
        //Creating a weapon deck
        weapons = WeaponFactory.createDeck();
    }

    /**
     * This test covers the method grabWeapon() in the following situations:
     * - grabbing a weapon that belongs to the spawnpoint
     * - grabbing a weapon that does not belong to the spawnpoint
     */
    @Test
    void grabWeapon() {
        Optional<Weapon> weapon = weapons.pick();
        //making sure the deck returned an actual weapon
        assertTrue(weapon.isPresent());
        //adding a weapon to the block
        block.drop(weapon.get());
        //making sure the weapon was added correctly
        assertEquals(1, block.getWeapons().size());
        //grabbing the weapon
        block.grabWeapon(weapon.get());
        //making sure the weapon was removed from the block
        assertEquals(0, block.getWeapons().size());
        //testing the method when the weapon to grab does not belong to the spawnpoint, which causes an exception
        assertThrows(
                IllegalArgumentException.class,
                () -> block.grabWeapon(weapon.get()),
                "Trying to grab a weapon that is not in this spawnpoint"
        );
    }

    /**
     * This test covers the method drop() in the following situations:
     * - dropping up to max number allowed of weapons (3 in this case)
     * - trying to drop more weapons than it's allowed
     */
    @Test
    void drop() {
        //picking 4 weapons from the deck, and making sure they exist
        Optional<Weapon> weapon1 = weapons.pick();
        Optional<Weapon> weapon2 = weapons.pick();
        Optional<Weapon> weapon3 = weapons.pick();
        Optional<Weapon> weapon4 = weapons.pick();
        assertTrue(weapon1.isPresent());
        assertTrue(weapon2.isPresent());
        assertTrue(weapon3.isPresent());
        assertTrue(weapon4.isPresent());
        //nothing was added to the spawnpoint, so it should have no weapons
        assertEquals(0, block.getWeapons().size());
        block.drop(weapon1.get());
        block.drop(weapon2.get());
        block.drop(weapon3.get());
        //checking all weapons have been added correctly
        assertEquals(3, block.getWeapons().size());
        //trying to add a fourth weapon when the max is 3 will cause an exception
        assertThrows(
                IllegalStateException.class,
                () -> block.drop(weapon4.get()),
                "4 weapons were dropped, max was 3"
        );
    }
}