package it.polimi.ingsw.server.model.rewards;

public interface Reward {

    int getRewardFor(int index, boolean isFirst);

    int getRewardFor(int index);
}
