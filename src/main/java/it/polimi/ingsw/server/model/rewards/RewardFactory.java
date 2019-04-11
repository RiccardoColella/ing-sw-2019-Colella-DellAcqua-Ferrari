package it.polimi.ingsw.server.model.rewards;

import com.google.gson.*;
import it.polimi.ingsw.server.model.exceptions.MissingConfigurationFileException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class RewardFactory {

    public enum Type {
        STANDARD,
        FINAL_FRENZY,
        KILLSHOT,
        DOUBLE_KILL;

        public static Type findByString(String s) {
            for (Type type: Type.values()) {
                if (s.equals(type.toString())) {
                    return type;
                }
            }

            throw new IllegalArgumentException();
        }
    }

    private static final String REWARDS_JSON_FILENAME = "./resources/rewards.json";
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
            try {
                jsonElement = new JsonParser().parse(new FileReader(new File(REWARDS_JSON_FILENAME)));
            } catch (IOException e) {
                throw new MissingConfigurationFileException("Unable to read Rewards configuration file");
            }



            for (int i = 0; i < Type.values().length; i++) {

                JsonObject fieldJson = jsonElement.getAsJsonArray().get(i).getAsJsonObject();
                JsonArray rewards;
                List<Integer> rewardsInt;
                switch (Type.findByString(fieldJson.get("type").getAsString())) {
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
                                Type.findByString(fieldJson.get("type").getAsString()),
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