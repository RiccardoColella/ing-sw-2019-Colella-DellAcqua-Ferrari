package it.polimi.ingsw.server.model.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ActionTileFactoryTest {

    /**
     * Testing that the Factory can provide an instance for each possible type of ActionTile
     */
    @Test
    void create() {
        for (ActionTile.Type type : ActionTile.Type.values()) {
            assertNotNull(ActionTileFactory.create(type), "ActionTile JSON file did not provide a valid configuration for action tile " + type);
        }
    }
}