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

    private List<String> weaponsOnLeftSpawnpoint;

    private List<String> weaponsOnRightSpawnpoint;

    private List<String> weaponsOnTopSpawnpoint;

    public GameRepresentation(MatchStarted e){

        this.preset = e.getPreset();
        this.players = new LinkedList<>(e.getOpponents());
        this.players.add(0, e.getSelf());
        this.skulls = e.getSkulls();
        this.weaponsOnLeftSpawnpoint = new ArrayList<>(e.getWeaponLeft());
        this.weaponsOnTopSpawnpoint = new ArrayList<>(e.getWeaponTop());
        this.weaponsOnRightSpawnpoint = new ArrayList<>(e.getWeaponRight());

    }

    private Player selectPlayer(Player playerToSelect){
        for (Player player : players){
            if (player == playerToSelect){
                return player;
            }
        }
        throw new IllegalArgumentException("Player " + playerToSelect.getNickname() + " not found in players");
    }

    public void movePlayer(Player player, int r, int c){
        selectPlayer(player).setLocation(new Point(r, c));
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
        playerWallet.getLoadedWeapons().add(weapon);
        // Setting unloaded weapons
        playerWallet.getUnloadedWeapons().remove(weapon);
    }

    void setPlayerWeaponUnloaded(Player playerWeaponUnloaded, String weapon){
        Wallet playerWallet = selectPlayer(playerWeaponUnloaded).getWallet();
        // Setting weapon unloaded
        playerWallet.getUnloadedWeapons().add(weapon);
        // Setting loaded weapons
        playerWallet.getLoadedWeapons().remove(weapon);
    }

    void grabPlayerWeapon(Player player, String weapon, int r, int c){
        // Here we select the spawnpoint from which remove the weapon grabbed
        if (r == 0){
            weaponsOnLeftSpawnpoint.remove(weapon);
        } else if (c == 0){
            weaponsOnTopSpawnpoint.remove(weapon);
        } else weaponsOnRightSpawnpoint.remove(weapon);
        // then we add the weapon to player's wallet
        selectPlayer(player).getWallet().getLoadedWeapons().add(weapon);
    }

    void dropPlayerWeapon(Player player, String weapon, int r, int c){
        // Here we select the spawnpoint to which add the weapon dropped
        if (r == 0){
            weaponsOnLeftSpawnpoint.add(weapon);
        } else if (c == 0){
            weaponsOnTopSpawnpoint.add(weapon);
        } else weaponsOnRightSpawnpoint.add(weapon);
        // then we remove the weapon from player's wallet
        if (selectPlayer(player).getWallet().getLoadedWeapons().contains(weapon)){
            selectPlayer(player).getWallet().getLoadedWeapons().remove(weapon);
        } else selectPlayer(player).getWallet().getUnloadedWeapons().remove(weapon);
    }



}
