package it.polimi.ingsw.server.model.factories;

import it.polimi.ingsw.server.model.rewards.RewardFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RewardFactoryTest {

    @Test
    void create() {
        for (RewardFactory.Type type : RewardFactory.Type.values()) {
            assertNotNull(RewardFactory.create(type), "RewardFactory did not provide a valid reward type " + type);
        }
    }
}