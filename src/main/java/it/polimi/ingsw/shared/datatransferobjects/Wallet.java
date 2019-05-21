package it.polimi.ingsw.shared.datatransferobjects;

import it.polimi.ingsw.server.model.currency.CurrencyColor;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Wallet implements Serializable {
    private List<String> loadedWeapons;
    private List<String> unloadedWeapons;
    private List<CurrencyColor> ammoCubes;
    private List<Powerup> powerups;

    public Wallet(List<String> loadedWeapons, List<String> unloadedWeapons, List<CurrencyColor> ammoCubes, List<Powerup> powerups) {
        this.loadedWeapons = loadedWeapons;
        this.unloadedWeapons = unloadedWeapons;
        this.ammoCubes = ammoCubes;
        this.powerups = powerups;
    }

    public List<String> getLoadedWeapons() {
        return loadedWeapons;
    }

    public List<String> getUnloadedWeapons() {
        return unloadedWeapons;
    }

    public List<CurrencyColor> getAmmoCubes() {
        return ammoCubes;
    }

    public List<Powerup> getPowerups() {
        return powerups;
    }

    @Override
    public int hashCode() {
        return Stream.concat(
                Stream.concat(loadedWeapons.stream().map(x -> (Object)x), unloadedWeapons.stream().map(x -> (Object)x)),
                Stream.concat(ammoCubes.stream().map(x -> (Object)x), powerups.stream().map(x -> (Object)x))
        )
                .collect(Collectors.toList())
                .hashCode();
    }

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
