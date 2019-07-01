package it.polimi.ingsw.shared.datatransferobjects;

import it.polimi.ingsw.server.model.currency.CurrencyColor;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represents a player's wallet
 *
 * @author Adriana Ferrari
 */
public class Wallet implements Serializable {
    /**
     * The loaded weapons
     */
    private List<String> loadedWeapons;
    /**
     * The unloaded weapons
     */
    private List<String> unloadedWeapons;
    /**
     * The ammo cubes
     */
    private List<CurrencyColor> ammoCubes;
    /**
     * The powerups
     */
    private List<Powerup> powerups;

    /**
     * Constructs a wallet
     *
     * @param loadedWeapons the loaded weapons
     * @param unloadedWeapons the unloaded weapons
     * @param ammoCubes the ammo cubes
     * @param powerups the powerups
     */
    public Wallet(List<String> loadedWeapons, List<String> unloadedWeapons, List<CurrencyColor> ammoCubes, List<Powerup> powerups) {
        this.loadedWeapons = loadedWeapons;
        this.unloadedWeapons = unloadedWeapons;
        this.ammoCubes = ammoCubes;
        this.powerups = powerups;
    }

    /**
     * @return the loaded weapons
     */
    public List<String> getLoadedWeapons() {
        return loadedWeapons;
    }

    /**
     * @return the unloaded weapons
     */
    public List<String> getUnloadedWeapons() {
        return unloadedWeapons;
    }

    /**
     * @return the ammo cubes
     */
    public List<CurrencyColor> getAmmoCubes() {
        return ammoCubes;
    }

    /**
     * @return the powerups
     */
    public List<Powerup> getPowerups() {
        return powerups;
    }

    /**
     * Overrides the default hashCode method
     *
     * @return the hash code of this object
     */
    @Override
    public int hashCode() {
        return Stream.concat(
                Stream.concat(loadedWeapons.stream().map(x -> (Object)x), unloadedWeapons.stream().map(x -> (Object)x)),
                Stream.concat(ammoCubes.stream().map(x -> (Object)x), powerups.stream().map(x -> (Object)x))
        )
                .collect(Collectors.toList())
                .hashCode();
    }


    /**
     * Overrides the default equals method
     *
     * @return true if this object is equal to the one passed as a parameter
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Wallet)) {
            return false;
        } else {
            return loadedWeapons.equals(((Wallet) other).loadedWeapons)
                    && unloadedWeapons.equals(((Wallet) other).unloadedWeapons)
                    && ammoCubes.equals(((Wallet) other).ammoCubes)
                    && powerups.equals(((Wallet) other).powerups);
        }
    }
}
