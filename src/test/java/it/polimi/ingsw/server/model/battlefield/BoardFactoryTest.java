package it.polimi.ingsw.server.model.battlefield;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardFactoryTest {

    @Test
    void create() {
        for (BoardFactory.Preset preset : BoardFactory.Preset.values()) {
            assertNotNull(BoardFactory.create(preset), "BoardFactory did not provide a valid configuration for board preset " + preset);
        }
    }
}