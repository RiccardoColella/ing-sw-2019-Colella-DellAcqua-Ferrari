package it.polimi.ingsw.server.model.rewards;

/**
 * A reward given on player death
 *
 * @author Adriana Ferrari
 */
public class PlayerDeathReward implements Reward {

    /**
     * The points of this reward
     */
    private final int[] points;
    /**
     * Extra first-blood points
     */
    private final int firstBloodPoints;

    /**
     * Constructs a player death reward object
     *
     * @param points the points this reward can give
     * @param firstBloodPoints extra first-blood points
     */
    public PlayerDeathReward(int[] points, int firstBloodPoints) {
        this.points = points;
        this.firstBloodPoints = firstBloodPoints;
    }

    /**
     * @param index the index on the player board
     * @param isFirst whether or not the reward is for the first attacker
     * @return the reward corresponding to the given index and boolean flag
     */
    @Override
    public int getRewardFor(int index, boolean isFirst) {
        index = index > points.length ? points.length - 1 : index;
        int extra = isFirst ? firstBloodPoints : 0;
        return points[index] + extra;
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
