package it.polimi.ingsw.server.model.weapons;

import it.polimi.ingsw.server.model.battlefield.Droppable;
import it.polimi.ingsw.server.model.currency.AmmoCube;

import java.util.Collections;
import java.util.List;

/**
 * This class represents a Weapon Tile
 *
 * @author Adriana Ferrari
 */
public class WeaponTile implements Droppable {
    private boolean loaded = true;
    private final String name;
    private final List<AmmoCube> acquisitionCost;
    private final List<AmmoCube> reloadCost;

    /**
     * This constructor creates a WeaponTile with initial values
     *
     * @param name the name of the weapon
     * @param acquisitionCost a list of AmmoCube used to determine the acquisition cost
     * @param reloadCost a list of AmmoCube used to determine the reload cost
     */
    public WeaponTile(String name, List<AmmoCube> acquisitionCost, List<AmmoCube> reloadCost) {
        this.name = name;
        this.acquisitionCost = Collections.unmodifiableList(acquisitionCost);
        this.reloadCost = Collections.unmodifiableList(reloadCost);
    }

    /**
     * Copy constructor
     *
     * @param weapon the weapon to copy values from
     */
    public WeaponTile(WeaponTile weapon) {
        this(weapon.name, weapon.acquisitionCost, weapon.reloadCost);
    }

    /**
     * @return true if the weapon is loaded
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * @return the weapon name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the acquisition cost
     */
    public List<AmmoCube> getAcquisitionCost() {
        return acquisitionCost;
    }

    /**
     * @return the reload cost
     */
    public List<AmmoCube> getReloadCost() {
        return reloadCost;
    }

    /**
     * Changes the load status of this weapon
     *
     * @param loaded the new status
     */
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }
}
