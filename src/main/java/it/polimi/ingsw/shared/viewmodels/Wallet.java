package it.polimi.ingsw.shared.viewmodels;

import it.polimi.ingsw.server.model.currency.CurrencyColor;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Wallet {
    private List<String> loadedWeapons;
    private List<String> unloadedWeapons;
    private List<CurrencyColor> ammoCubes;
    private List<Powerup> powerups;

    public Wallet() {
        loadedWeapons = new LinkedList<>();
        unloadedWeapons = new LinkedList<>();
        ammoCubes = new LinkedList<>();
        powerups = new LinkedList<>();
    }

    public List<String> getLoadedWeapons() {
        return loadedWeapons;
    }

    public void setLoadedWeapons(List<String> loadedWeapons) {
        this.loadedWeapons = loadedWeapons;
    }

    public List<String> getUnloadedWeapons() {
        return unloadedWeapons;
    }

    public void setUnloadedWeapons(List<String> unloadedWeapons) {
        this.unloadedWeapons = unloadedWeapons;
    }

    public List<CurrencyColor> getAmmoCubes() {
        return ammoCubes;
    }

    public void setAmmoCubes(List<CurrencyColor> ammoCubes) {
        this.ammoCubes = ammoCubes;
    }

    public List<Powerup> getPowerups() {
        return powerups;
    }

    public void setPowerups(List<Powerup> powerups) {
        this.powerups = powerups;
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
