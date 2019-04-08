package it.polimi.ingsw.server.model.rewards;

public class DoubleKillReward implements Reward {

    private final int points;

    public DoubleKillReward(int points) {
        this.points = points;
    }

    @Override
    public int getRewardFor(int index, boolean isFirst) {
        return points;
    }

    @Override
    public int getRewardFor(int index) {
        return getRewardFor(index, false);
    }
}
