package it.polimi.ingsw.server.model.rewards;

/**
 * A reward given when a player makes a double kill
 *
 * @author Adriana Ferrari
 */
public class DoubleKillReward implements Reward {

    /**
     * A fixed amount of points this reward can give
     */
    private final int points;

    /**
     * Constructs a double kill reward
     * @param points the fixed amount of points this reward can give
     */
    public DoubleKillReward(int points) {
        this.points = points;
    }


    /**
     * @param index the index on the player board
     * @param isFirst whether or not the reward is for the first attacker
     * @return the reward corresponding to the given index and boolean flag
     */
    @Override
    public int getRewardFor(int index, boolean isFirst) {
        return points;
    }

    /**
     * @param index the index on the player board
     * @return the reward corresponding to the given index and boolean flag
     */
    @Override
    public int getRewardFor(int index) {
        return getRewardFor(index, false);
    }
}
