package it.polimi.ingsw.server.model.rewards;

public class KillshotReward implements Reward {
    private final int[] points;

    public KillshotReward(int[] points) {
        this.points = points;
    }

    @Override
    public int getRewardFor(int index, boolean ...isFirst) {
        return points[index];
    }
}
