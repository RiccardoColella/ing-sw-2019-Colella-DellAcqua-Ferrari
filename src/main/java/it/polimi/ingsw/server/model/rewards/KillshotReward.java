package it.polimi.ingsw.server.model.rewards;

/**
 * A reward given when a player makes a killshot
 *
 * @author Adriana Ferrari
 */
public class KillshotReward implements Reward {
    /**
     * The points of this reward
     */
    private final int[] points;

    /**
     * @param points the points of this reward
     */
    public KillshotReward(int[] points) {
        this.points = points;
    }

    /**
     * @param index the index on the player board
     * @param isFirst whether or not the reward is for the first attacker
     * @return the reward corresponding to the given index and boolean flag
     */
    @Override
    public int getRewardFor(int index, boolean isFirst) {
        return points[index];
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
