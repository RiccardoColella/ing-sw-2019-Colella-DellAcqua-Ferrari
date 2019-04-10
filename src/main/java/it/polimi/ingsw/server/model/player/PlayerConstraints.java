package it.polimi.ingsw.server.model.player;

import java.util.Map;

public class PlayerConstraints {

    private Map<String, Integer> constraints;

    PlayerConstraints(Map<String, Integer> constraints) {
        this.constraints = constraints;
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
