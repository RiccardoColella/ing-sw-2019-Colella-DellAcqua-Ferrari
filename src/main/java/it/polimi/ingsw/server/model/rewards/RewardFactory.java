package it.polimi.ingsw.server.model.rewards;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.utils.ConfigFileMaker;
import it.polimi.ingsw.utils.EnumValueByString;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Factory that provides rewards for different type of situations
 *
 * @author Adriana Ferrari
 */
public final class RewardFactory {

    public enum Type {
        STANDARD,
        FINAL_FRENZY,
        KILLSHOT,
        DOUBLE_KILL;
    }

    private static final String REWARDS_JSON_PATH = "./config/rewards.json";
    private static final String REWARDS_JSON_PATH_RES = "/config/rewards.json";
    private static Map<RewardFactory.Type, Reward> rewardMap;

    private RewardFactory() {

    }
    /**
     * This method is used to create a reward of the chosen type
     *
     * @param type the enum corresponding to the desired reward object
     * @return the reward
     */
    public static Reward create(Type type) {

        if (rewardMap == null) {
            rewardMap = new EnumMap<>(Type.class);
            JsonElement jsonElement;

            jsonElement = new JsonParser().parse(ConfigFileMaker.load(REWARDS_JSON_PATH, REWARDS_JSON_PATH_RES));

            for (int i = 0; i < Type.values().length; i++) {

                JsonObject fieldJson = jsonElement.getAsJsonArray().get(i).getAsJsonObject();
                JsonArray rewards;
                List<Integer> rewardsInt;
                switch (EnumValueByString.findByString(fieldJson.get("type").getAsString(), Type.class)) {
                    case DOUBLE_KILL:
                        rewardMap.put(Type.DOUBLE_KILL, new DoubleKillReward(fieldJson.get("reward").getAsInt()));
                        break;
                    case KILLSHOT:
                        rewards = fieldJson.get("rewards").getAsJsonArray();
                        rewardsInt = new LinkedList<>();
                        rewards.iterator().forEachRemaining(element -> rewardsInt.add(element.getAsInt()));
                        rewardMap.put(Type.KILLSHOT, new KillshotReward(rewardsInt.stream().mapToInt(num->num).toArray()));
                        break;
                    case FINAL_FRENZY:
                    case STANDARD:
                        rewards = fieldJson.get("rewards").getAsJsonArray();
                        rewardsInt = new LinkedList<>();
                        rewards.iterator().forEachRemaining(element -> rewardsInt.add(element.getAsInt()));
                        rewardMap.put(
                                EnumValueByString.findByString(fieldJson.get("type").getAsString(), Type.class),
                                new PlayerDeathReward(
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
