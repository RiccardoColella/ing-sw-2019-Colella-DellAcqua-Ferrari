package it.polimi.ingsw.client.ui.cli;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.shared.events.networkevents.MatchStarted;
import it.polimi.ingsw.shared.events.networkevents.PlayerHealthChanged;
import it.polimi.ingsw.shared.events.networkevents.PlayerMoved;
import it.polimi.ingsw.shared.viewmodels.Player;
import it.polimi.ingsw.shared.viewmodels.Wallet;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GameRepresentation {

    private BoardFactory.Preset preset;

    private List<Player> players;

    private int skulls;

    private List<String> weaponsOnRedSpawnpoint;

    private List<String> weaponsOnYellowSpawnpoint;

    private List<String> weaponsOnBlueSpawnpoint;

    public GameRepresentation(MatchStarted e){

        this.preset = e.getPreset();
        this.players = new LinkedList<>(e.getOpponents());
        this.players.add(0, e.getSelf());
        this.skulls = e.getSkulls();
        this.weaponsOnRedSpawnpoint = new ArrayList<>(e.getWeaponLeft());
        this.weaponsOnBlueSpawnpoint = new ArrayList<>(e.getWeaponTop());
        this.weaponsOnYellowSpawnpoint = new ArrayList<>(e.getWeaponRight());

    }

    private Player selectPlayer(Player playerToSelect){
        for (Player player : players){
            if (player == playerToSelect){
                return player;
            }
        }
        throw new IllegalArgumentException("Player " + playerToSelect.getNickname() + " not found in players");
    }

    public void movePlayer(PlayerMoved e){
        selectPlayer(e.getPlayer()).setLocation(new Point(e.getRow(), e.getColumn()));
    }

    void setPlayerWallet(Player player, Wallet newWallet){
        Wallet playerWallet = selectPlayer(player).getWallet();
        playerWallet.setAmmoCubes(newWallet.getAmmoCubes());
        playerWallet.setPowerups(newWallet.getPowerups());
        playerWallet.setLoadedWeapons(newWallet.getLoadedWeapons());
        playerWallet.setUnloadedWeapons(newWallet.getUnloadedWeapons());
    }

    void updatePlayerHealth(PlayerHealthChanged e){
        Player player = selectPlayer(e.getPlayer());
        player.setDamage(e.getDamages());
        player.setMarks(e.getMarks());
        player.setSkulls(e.getSkulls());
    }

    void setPlayerWeaponLoaded(Player playerWhoReloaded, String weapon){
        Wallet playerWallet = selectPlayer(playerWhoReloaded).getWallet();
        // Setting loaded weapons
        List<String> loadedWeapons = new LinkedList<>(playerWallet.getLoadedWeapons());
        loadedWeapons.add(weapon);
        playerWallet.setLoadedWeapons(loadedWeapons);
        // Setting unloaded weapons
        List<String> unloadedWeapons = new LinkedList<>(playerWallet.getUnloadedWeapons());
        unloadedWeapons.remove(weapon);
        playerWallet.setUnloadedWeapons(unloadedWeapons);
    }

}
