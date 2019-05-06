package it.polimi.ingsw.client.viewmodels;

import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.utils.Tuple;

import java.util.LinkedList;
import java.util.List;

public class Wallet {
    private List<String> loadedWeapons;
    private List<String> unloadedWeapons;
    private List<CurrencyColor> ammoCubes;
    private List<Tuple<String, CurrencyColor>> powerups;
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

    public List<Tuple<String, CurrencyColor>> getPowerups() {
        return powerups;
    }

    public void setPowerups(List<Tuple<String, CurrencyColor>> powerups) {
        this.powerups = powerups;
    }
}
