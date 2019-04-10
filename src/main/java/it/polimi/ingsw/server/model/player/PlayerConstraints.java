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

    public int getMaxAmmoCubesOfAColor() {
        return constraints.getOrDefault("maxAmmoCubesOfAColor", 3);
    }

    public int getMaxWeaponsForPlayer() {
        return constraints.getOrDefault("maxWeaponsForPlayer", 3);
    }

    public int getMaxMarksFromPlayer() {
        return constraints.getOrDefault("maxMarksFromPlayer", 3);
    }

    public int getMaxPowerupsForPlayer() {
        return constraints.getOrDefault("maxPowerupsForPlayer", 3);
    }

    public int getMortalDamage() {
        return constraints.getOrDefault("mortalDamage", 11);
    }

    public int getMaxDamage() {
        return constraints.getOrDefault("maxDamage", 12);
    }

    public int getFirstAdrenalineTrigger() {
        return constraints.getOrDefault("firstAdrenalineTrigger", 3);
    }

    public int getSecondAdrenalineTrigger() {
        return constraints.getOrDefault("secondAdrenalineTrigger", 3);
    }
}
