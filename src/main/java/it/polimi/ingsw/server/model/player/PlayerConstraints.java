package it.polimi.ingsw.server.model.player;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the set of constraints needed for the instantiation of a Player
 */
public class PlayerConstraints {

    private Map<String, Integer> constraints;

    /**
     * This constructor instantiates a PlayerConstraints object giving the ability to change the default values
     *
     * @param constraints a map containing the keys for overriding the defaults
     */
    public PlayerConstraints(Map<String, Integer> constraints) {
        this.constraints = constraints;
    }

    /**
     * Default constructor, all getters values will default to the official Adrenaline game values
     */
    public PlayerConstraints() {
        this.constraints = new HashMap<>();
    }

    /**
     * @return the max ammo cubes of the same color that a player can own
     */
    public int getMaxAmmoCubesOfAColor() {
        return constraints.getOrDefault("maxAmmoCubesOfAColor", 3);
    }

    /**
     * @return the maximum amount of weapon a player can own
     */
    public int getMaxWeaponsForPlayer() {
        return constraints.getOrDefault("maxWeaponsForPlayer", 3);
    }

    /**
     * @return the maximum amount of mark a player can receive from the same opponent
     */
    public int getMaxMarksFromPlayer() {
        return constraints.getOrDefault("maxMarksFromPlayer", 3);
    }

    /**
     * @return the maximum amount of powerups that a player can own
     */
    public int getMaxPowerupsForPlayer() {
        return constraints.getOrDefault("maxPowerupsForPlayer", 3);
    }

    /**
     * @return the number of damage tokens that determines a mortal damage
     */
    public int getMortalDamage() {
        return constraints.getOrDefault("mortalDamage", 11);
    }

    /**
     * @return the maximum number of damage tokens a player can have on his board
     */
    public int getMaxDamage() {
        return constraints.getOrDefault("maxDamage", 12);
    }

    /**
     * @return the number of damage tokens a player must have to trigger his first adrenaline mode
     */
    public int getFirstAdrenalineTrigger() {
        return constraints.getOrDefault("firstAdrenalineTrigger", 3);
    }

    /**
     * @return the number of damage tokens a player must have to trigger his second adrenaline mode
     */
    public int getSecondAdrenalineTrigger() {
        return constraints.getOrDefault("secondAdrenalineTrigger", 6);
    }
}
