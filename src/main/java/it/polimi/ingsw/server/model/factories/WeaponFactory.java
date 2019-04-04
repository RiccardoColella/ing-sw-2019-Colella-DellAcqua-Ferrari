package it.polimi.ingsw.server.model.factories;

import it.polimi.ingsw.server.model.Weapon;

/**
 * This class creates all the 21 weapons of the game
 */
public final class WeaponFactory {

    /**
     * This enum cathegorizes the 21 different weapons
     */
    public enum Name {
        LOCK_RIFLE,
        ELECTROSCYTHE,
        MACHINE_GUN,
        TRACTOR_BEAM,
        THOR,
        VORTEX_CANNON,
        FURNACE,
        PLASMA_GUN,
        HEATSEEKER,
        WHISPER,
        HELLION,
        FLAMETHROWER,
        TWO_X_TWO,
        GRENADE_LAUNCHER,
        SHOTGUN,
        ROCKET_LAUNCHER,
        POWER_GLOVE,
        RAILGUN,
        SHOCKWAVE,
        CYBERBLADE,
        SLEDGEHAMMER
    }


    /**
     * Private empty constructor because this class should not have instances
     */
    private WeaponFactory() {

    }
    /**
     * This method is used to create any weapon
     * @param name the enum corresponding to the desired weapon
     * @return the weapon, ready to be bought
     */
    public static Weapon create(Name name) {
        return null;
    }
}
