package it.polimi.ingsw.server.model.factories;

import it.polimi.ingsw.server.model.weapons.Weapon;
import it.polimi.ingsw.server.model.weapons.WeaponWithMultipleEffects;

/**
 * This class creates all the 21 weapons of the game
 */
public final class WeaponFactory {

    /**
     * This enum cathegorizes the 21 different weapons
     */
    public enum Name {
        LOCK_RIFLE("Lock Rifle"),
        ELECTROSCYTHE("Electroscythe"),
        MACHINE_GUN("Machine Gun"),
        TRACTOR_BEAM("Tractor Beam"),
        THOR("T.H.O.R."),
        VORTEX_CANNON("Vortex Cannon"),
        FURNACE("Furnace"),
        PLASMA_GUN("Plasma Gun"),
        HEATSEEKER("Heatseeker"),
        WHISPER("Whisper"),
        HELLION("Hellion"),
        FLAMETHROWER("Flamethrower"),
        TWO_X_TWO("2x-2"),
        GRENADE_LAUNCHER("Grenade Launcher"),
        SHOTGUN("Shotgun"),
        ROCKET_LAUNCHER("Rocket Launcher"),
        POWER_GLOVE("Power Glove"),
        RAILGUN("Railgun"),
        SHOCKWAVE("Shockwave"),
        CYBERBLADE("Cyberblade"),
        SLEDGEHAMMER("Sledgehammer");

        private String humanReadableName;

        /**
         * Constructs the enum associated with a human readable name
         *
         * @param humanReadableName
         */
        Name(String humanReadableName) {
            this.humanReadableName = humanReadableName;
        }

        @Override
        public String toString() {
            return humanReadableName;
        }
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
