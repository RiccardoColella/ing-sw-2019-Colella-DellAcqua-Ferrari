package it.polimi.ingsw.server.model.weapons;

import it.polimi.ingsw.server.model.collections.Deck;
import it.polimi.ingsw.server.model.weapons.Weapon;
import it.polimi.ingsw.server.model.weapons.WeaponFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeaponFactoryTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void create() {
        for (Weapon.Name name : Weapon.Name.values()) {
            assertNotNull(WeaponFactory.create(name), "Weapon JSON file did not provide a valid configuration for weapon " + name);
        }
    }

    @Test
    void createDeck() {
        Deck<Weapon> deck = WeaponFactory.createDeck();
        assertEquals(21, deck.size(), "Not all 21 weapons were loaded");
    }
}