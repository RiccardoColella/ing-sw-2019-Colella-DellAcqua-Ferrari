package it.polimi.ingsw.server.model.weapons;

import it.polimi.ingsw.server.model.currency.AmmoCube;

import java.util.Collections;
import java.util.List;

public class Weapon {
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
         *
         * @param humanReadableName a human readable name for the weapon
         */
        Name(String humanReadableName) {
            this.humanReadableName = humanReadableName;
        }

        @Override
        public String toString() {
            return humanReadableName;
        }
    }

    private boolean loaded = true;
    private final Name name;
    private final List<AmmoCube> acquisitionCost;
    private final List<AmmoCube> reloadCost;

    /**
     * This constructor creates a Weapon with initial values
     *
     * @param name the name of the weapon
     * @param acquisitionCost a list of AmmoCube used to determine the acquisition cost
     * @param reloadCost a list of AmmoCube used to determine the reload cost
     */
    public Weapon(Name name, List<AmmoCube> acquisitionCost, List<AmmoCube> reloadCost) {
        this.name = name;
        this.acquisitionCost = Collections.unmodifiableList(acquisitionCost);
        this.reloadCost = Collections.unmodifiableList(reloadCost);
    }

    /**
     * Copy constructor
     *
     * @param weapon the weapon to copy values from
     */
    public Weapon(Weapon weapon) {
        this(weapon.name, weapon.acquisitionCost, weapon.reloadCost);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public Name getName() {
        return name;
    }

    public List<AmmoCube> getAcquisitionCost() {
        return acquisitionCost;
    }

    public List<AmmoCube> getReloadCost() {
        return reloadCost;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }
}
