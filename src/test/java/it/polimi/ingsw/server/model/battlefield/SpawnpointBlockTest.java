package it.polimi.ingsw.server.model.battlefield;

import it.polimi.ingsw.server.model.collections.Deck;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.factories.WeaponFactory;
import it.polimi.ingsw.server.model.weapons.Weapon;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SpawnpointBlockTest {

    @Test
    void grabWeapon() {
        SpawnpointBlock block = new SpawnpointBlock(0,0, Block.BorderType.NONE, Block.BorderType.NONE, Block.BorderType.NONE, Block.BorderType.NONE, CurrencyColor.BLUE);
        Deck<Weapon> weapons = WeaponFactory.createDeck();

        Optional<Weapon> weapon = weapons.pick();
        assertTrue(weapon.isPresent());
        block.dropWeapon(weapon.get());
        assertEquals(1,block.getWeapons().size());
        block.grabWeapon(weapon.get());
        assertEquals(0, block.getWeapons().size());
        try {
            block.grabWeapon(weapon.get());
            fail();
        } catch (IllegalArgumentException e){
            assertNull(e.getMessage());
        }
    }

    @Test
    void dropWeapon() {
        SpawnpointBlock block = new SpawnpointBlock(0,0, Block.BorderType.NONE, Block.BorderType.NONE, Block.BorderType.NONE, Block.BorderType.NONE, CurrencyColor.BLUE);
        Deck<Weapon> weapons = WeaponFactory.createDeck();

        Optional<Weapon> weapon1 = weapons.pick();
        Optional<Weapon> weapon2 = weapons.pick();
        Optional<Weapon> weapon3 = weapons.pick();
        Optional<Weapon> weapon4 = weapons.pick();
        assertTrue(weapon1.isPresent());
        assertTrue(weapon2.isPresent());
        assertTrue(weapon3.isPresent());
        assertTrue(weapon4.isPresent());

        assertEquals(0, block.getWeapons().size());
        block.dropWeapon(weapon1.get());
        block.dropWeapon(weapon2.get());
        block.dropWeapon(weapon3.get());
        assertEquals(3, block.getWeapons().size());

        try {
            block.dropWeapon(weapon1.get());
            fail();
        } catch (IllegalStateException e){
            assertNull(e.getMessage());
        }






    }
}