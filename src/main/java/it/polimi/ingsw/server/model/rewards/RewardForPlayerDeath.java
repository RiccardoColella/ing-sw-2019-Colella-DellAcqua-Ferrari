package it.polimi.ingsw.server.model.rewards;

public class RewardForPlayerDeath implements Reward {

    private final int[] points;
    private final int firstBloodPoints;

    public RewardForPlayerDeath(int[] points, int firstBloodPoints) {
        this.points = points;
        this.firstBloodPoints = firstBloodPoints;
    }

    @Override
    public int getRewardFor(int index, boolean ...isFirst) {
        index = index > points.length ? points.length - 1 : index;
        int extra = isFirst[0] ? firstBloodPoints : 0;
        return points[index] + extra;
    }
}
