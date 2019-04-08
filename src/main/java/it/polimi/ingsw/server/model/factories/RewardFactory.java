package it.polimi.ingsw.server.model.factories;

import com.google.gson.*;
import it.polimi.ingsw.server.model.rewards.DoubleKillReward;
import it.polimi.ingsw.server.model.rewards.KillshotReward;
import it.polimi.ingsw.server.model.rewards.Reward;
import it.polimi.ingsw.server.model.exceptions.MissingConfigurationFileException;
import it.polimi.ingsw.server.model.rewards.RewardForPlayerDeath;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class RewardFactory {
    private static final String REWARDS_JSON_FILENAME = "./resources/rewards.json";
    private static Map<Reward.Type, Reward> rewardMap;

    private RewardFactory() {

    }
    /**
     * This method is used to create a reward of the chosen type
     *
     * @param type the enum corresponding to the desired reward object
     * @return the reward
     */
    public static Reward create(Reward.Type type) {

        if (rewardMap == null) {
            rewardMap = new EnumMap<>(Reward.Type.class);
            JsonElement jsonElement;
            try {
                jsonElement = new JsonParser().parse(new FileReader(new File(REWARDS_JSON_FILENAME)));
            } catch (IOException e) {
                throw new MissingConfigurationFileException("Unable to read Rewards configuration file");
            }



            for (int i = 0; i < Reward.Type.values().length; i++) {

                JsonObject fieldJson = jsonElement.getAsJsonArray().get(i).getAsJsonObject();
                JsonArray rewards;
                List<Integer> rewardsInt;
                switch (Reward.Type.findByString(fieldJson.get("type").getAsString())) {
                    case DOUBLE_KILL:
                        rewardMap.put(Reward.Type.DOUBLE_KILL, new DoubleKillReward(fieldJson.get("reward").getAsInt()));
                        break;
                    case KILLSHOT:
                        rewards = fieldJson.get("rewards").getAsJsonArray();
                        rewardsInt = new LinkedList<>();
                        rewards.iterator().forEachRemaining(element -> rewardsInt.add(element.getAsInt()));
                        rewardMap.put(Reward.Type.KILLSHOT, new KillshotReward(rewardsInt.stream().mapToInt(num->num).toArray()));
                        break;
                    case FINAL_FRENZY:
                    case STANDARD:
                        rewards = fieldJson.get("rewards").getAsJsonArray();
                        rewardsInt = new LinkedList<>();
                        rewards.iterator().forEachRemaining(element -> rewardsInt.add(element.getAsInt()));
                        rewardMap.put(
                                Reward.Type.findByString(fieldJson.get("type").getAsString()),
                                new RewardForPlayerDeath(
                                        rewardsInt.stream().mapToInt(num->num).toArray(),
                                        fieldJson.get("firstBlood").getAsInt()
                                ));
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
            }
        }

        return rewardMap.get(type);
    }
}
