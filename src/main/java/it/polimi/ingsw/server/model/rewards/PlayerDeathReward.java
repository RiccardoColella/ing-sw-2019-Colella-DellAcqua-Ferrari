package it.polimi.ingsw.server.model.rewards;

public class PlayerDeathReward implements Reward {

    private final int[] points;
    private final int firstBloodPoints;

    public PlayerDeathReward(int[] points, int firstBloodPoints) {
        this.points = points;
        this.firstBloodPoints = firstBloodPoints;
    }

    @Override
    public int getRewardFor(int index, boolean isFirst) {
        index = index > points.length ? points.length - 1 : index;
        int extra = isFirst ? firstBloodPoints : 0;
        return points[index] + extra;
    }

    @Override
    public int getRewardFor(int index) {
        return getRewardFor(index, false);
    }
}
