package it.polimi.ingsw.server.model.factories;

import com.google.gson.Gson;
import it.polimi.ingsw.server.model.collections.Deck;
import it.polimi.ingsw.server.model.weapons.Weapon;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

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
        try {
            for (Weapon.Name name : Weapon.Name.values()) {
                assertNotNull(WeaponFactory.create(name), "Weapon JSON file did not provide a valid configuration for weapon " + name);
            }
        } catch (FileNotFoundException e) {
            fail("Unable to test, file not found\n" + e.toString());
        }
    }

    @Test
    void createDeck() {
        try {
            Deck<Weapon> deck = WeaponFactory.createDeck();
            assertEquals(21, deck.size(), "Not all 21 weapons were loaded");
        } catch (FileNotFoundException e) {
            fail("Unable to test, file not found\n" + e.toString());
        }
    }
}