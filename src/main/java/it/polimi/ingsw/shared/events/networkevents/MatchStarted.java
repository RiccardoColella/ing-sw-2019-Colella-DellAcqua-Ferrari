package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.shared.datatransferobjects.BonusTile;
import it.polimi.ingsw.shared.datatransferobjects.Player;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Network event carrying information about a new match being started
 *
 * @author Carlo Dell'Acqua
 */
public class MatchStarted extends NetworkEvent {

    /**
     * The board preset
     */
    private final BoardFactory.Preset preset;
    /**
     * The opponents of the "self"
     */
    private final List<Player> opponents;
    /**
     * The recipient of this event
     */
    private final Player self;
    /**
     * The weapon on the spawnpoint located on the top of the board
     */
    private final List<String> weaponTop;
    /**
     * The weapon on the spawnpoint located on the right of the board
     */
    private final List<String> weaponRight;
    /**
     * The weapon on the spawnpoint located on the left of the board
     */
    private final List<String> weaponLeft;
    /**
     * The number of skulls
     */
    private final int skulls;
    /**
     * The current active player
     */
    private final Player currentActivePlayer;
    /**
     * The bonus tiles on the various turrets
     */
    private final Set<BonusTile> turretBonusTiles;

    /**
     *
     * @param skulls the number of skulls
     * @param preset the board preset
     * @param self the "self" relative to the player which will receive the event
     * @param opponents the opponents of the "self"
     * @param weaponTop the weapon on the spawnpoint located on the top of the board
     * @param weaponRight the weapon on the spawnpoint located on the right of the board
     * @param weaponLeft the weapon on the spawnpoint located on the left of the board
     * @param currentActivePlayer the active player for the first turn
     * @param turretBonusTiles the bonus tiles on the various turrets
     */
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

    /**
     * @return the board preset
     */
    public BoardFactory.Preset getPreset() {
        return preset;
    }

    /**
     * @return the opponents of "self"
     */
    public List<Player> getOpponents() {
        return opponents;
    }

    /**
     * @return the self, which should correspond to the player who receives this event
     */
    public Player getSelf() {
        return self;
    }

    /**
     * @return the weapon on the spawnpoint located on the left of the board
     */
    public List<String> getWeaponLeft() {
        return weaponLeft;
    }

    /**
     * @return the weapon on the spawnpoint located on the right of the board
     */
    public List<String> getWeaponRight() {
        return weaponRight;
    }

    /**
     * @return the weapon on the spawnpoint located on the top of the board
     */
    public List<String> getWeaponTop() {
        return weaponTop;
    }

    /**
     * @return the number of skulls
     */
    public int getSkulls() {
        return skulls;
    }

    /**
     * @return the active player for the first turn
     */
    public Player getCurrentActivePlayer() {
        return currentActivePlayer;
    }

    /**
     * @return the bonus tiles on the various turrets
     */
    public Set<BonusTile> getTurretBonusTiles() {
        return turretBonusTiles;
    }
}
