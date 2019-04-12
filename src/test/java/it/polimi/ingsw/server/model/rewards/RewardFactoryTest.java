package it.polimi.ingsw.server.model.rewards;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RewardFactoryTest {

    /**
     * This test verifies that create() returns a valid non-null Reward for each enum value
     */
    @Test
    void create() {
        for (RewardFactory.Type type : RewardFactory.Type.values()) {
            assertNotNull(RewardFactory.create(type), "RewardFactory did not provide a valid reward type " + type);
        }
    }
}