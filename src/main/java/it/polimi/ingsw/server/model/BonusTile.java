package it.polimi.ingsw.server.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the tiles that are picked during the game when a grab is executed on a turret block
 */
public class BonusTile {

    /**
     * variable set to true if the BonusTile is not full
     */
    private final boolean canPick;

    /**
     * This property stores the ammos that can be picked by who has this tile
     */
    private final List<Ammo> ammos;

    /**
     * Class constructor. With 2 Ammo you can pick a PowerUp
     * @param ammo1 the first Ammo cube
     * @param ammo2 the second Ammo cube
     */
    public BonusTile(Ammo ammo1, Ammo ammo2) {
        //TODO all function
        this.canPick = true;
        this.ammos = new ArrayList<>();
        this.ammos.add(ammo1);
        this.ammos.add(ammo2);
    }

    /**
     * Class constructor. With 3 Ammo you can't pick a PowerUp
     * @param ammo1 the first Ammo cube
     * @param ammo2 the second Ammo cube
     * @param ammo3 the third Ammo cube
     */
    public BonusTile(Ammo ammo1, Ammo ammo2, Ammo ammo3) {
        //TODO all function
        this.canPick = false;
        this.ammos = new ArrayList<>();
        this.ammos.add(ammo1);
        this.ammos.add(ammo2);
        this.ammos.add(ammo3);
    }

    /**
     * Tells all the availables Ammo
     * @return the List of available Ammo
     */
    public List<Ammo> getRewards() {
        //TODO all function
        return this.ammos;
    }

    /**
     * Tells if you can pick a Powerup
     * @return true if you can pick a Powerup
     */
    public boolean canPickPowerup() {
        return canPick;
    }
}
