package it.polimi.ingsw.server.model.factories;

import it.polimi.ingsw.server.model.rewards.Reward;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RewardFactoryTest {

    @Test
    void create() {
        for (Reward.Type type : Reward.Type.values()) {
            assertNotNull(RewardFactory.create(type), "RewardFactory did not provide a valid reward type " + type);
        }
    }
}