package it.polimi.ingsw.server.model.factories;

import it.polimi.ingsw.server.model.player.ActionTile;
import it.polimi.ingsw.server.model.player.ActionTileFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActionTileFactoryTest {

    @Test
    void create() {
        for (ActionTile.Type type : ActionTile.Type.values()) {
            assertNotNull(ActionTileFactory.create(type), "ActionTile JSON file did not provide a valid configuration for action tile " + type);
        }
    }
}