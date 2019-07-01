package it.polimi.ingsw.shared.events.networkevents;

import it.polimi.ingsw.shared.datatransferobjects.Player;

/**
 * Network event carrying information about a player's weapon
 *
 * @author Carlo Dell'Acqua
 */
public class PlayerWeaponEvent extends PlayerEvent {

    /**
     * The weapon name
     */
    private final String weaponName;

    /**
     * Constructs a player's weapon event
     *
     * @param owner the player who owns the weapon
     * @param weaponName the weapon name
     */
    public PlayerWeaponEvent(Player owner, String weaponName) {
        super(owner);
        this.weaponName = weaponName;
    }

    /**
     * @return the weapon name
     */
    public String getWeaponName() {
        return weaponName;
    }
}
