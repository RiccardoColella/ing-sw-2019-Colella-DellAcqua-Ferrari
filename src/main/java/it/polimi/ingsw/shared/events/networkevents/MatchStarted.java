package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.shared.datatransferobjects.BonusTile;
import it.polimi.ingsw.shared.datatransferobjects.Player;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MatchStarted extends NetworkEvent {


    private final BoardFactory.Preset preset;
    private final List<Player> opponents;
    private final Player self;
    private final List<String> weaponTop;
    private final List<String> weaponRight;
    private final List<String> weaponLeft;
    private final int skulls;
    private final Player currentActivePlayer;
    private final Set<BonusTile> turretBonusTiles;

    public MatchStarted(int skulls, BoardFactory.Preset preset, Player self, List<Player> opponents, List<String> weaponTop, List<String> weaponRight, List<String> weaponLeft, Player currentActivePlayer, Set<BonusTile> turretBonusTiles) {
        this.preset = preset;
        this.opponents = opponents;
        this.self = self;
        this.weaponTop = weaponTop;
        this.weaponRight = weaponRight;
        this.weaponLeft = weaponLeft;
        this.skulls = skulls;
        this.currentActivePlayer = currentActivePlayer;
        this.turretBonusTiles = turretBonusTiles;
    }

    public BoardFactory.Preset getPreset() {
        return preset;
    }

    public List<Player> getOpponents() {
        return opponents;
    }

    public Player getSelf() {
        return self;
    }

    public List<String> getWeaponLeft() {
        return weaponLeft;
    }

    public List<String> getWeaponRight() {
        return weaponRight;
    }

    public List<String> getWeaponTop() {
        return weaponTop;
    }

    public int getSkulls() {
        return skulls;
    }

    public Player getCurrentActivePlayer() {
        return currentActivePlayer;
    }

    public Set<BonusTile> getTurretBonusTiles() {
        return turretBonusTiles;
    }
}
