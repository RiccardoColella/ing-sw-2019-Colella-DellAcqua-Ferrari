package it.polimi.ingsw.server.model.rewards;

/**
 * This interface represents a reward
 *
 * @author Adriana Ferrari
 */
public interface Reward {

    /**
     * @param index the index on the player board
     * @param isFirst whether or not the reward is for the first attacker
     * @return the reward corresponding to the given index and boolean flag
     */
    int getRewardFor(int index, boolean isFirst);

    /**
     * @param index the index on the player board
     * @return the reward corresponding to the given index and boolean flag
     */
    int getRewardFor(int index);
}
