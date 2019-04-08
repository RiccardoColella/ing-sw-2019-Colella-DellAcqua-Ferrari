package it.polimi.ingsw.server.model.currency;

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
     * This property stores the ammoCubes that can be picked by who has this tile
     */
    private final List<AmmoCube> ammoCubes;

    /**
     * Class constructor. With 2 AmmoCube you can pick a PowerUp
     * @param ammoCube1 the first AmmoCube cube
     * @param ammoCube2 the second AmmoCube cube
     */
    public BonusTile(AmmoCube ammoCube1, AmmoCube ammoCube2) {
        this.canPick = true;
        this.ammoCubes = new ArrayList<>();
        this.ammoCubes.add(ammoCube1);
        this.ammoCubes.add(ammoCube2);
    }

    /**
     * Class constructor. With 3 AmmoCube you can't pick a PowerUp
     * @param ammoCube1 the first AmmoCube cube
     * @param ammoCube2 the second AmmoCube cube
     * @param ammoCube3 the third AmmoCube cube
     */
    public BonusTile(AmmoCube ammoCube1, AmmoCube ammoCube2, AmmoCube ammoCube3) {
        this.canPick = false;
        this.ammoCubes = new ArrayList<>();
        this.ammoCubes.add(ammoCube1);
        this.ammoCubes.add(ammoCube2);
        this.ammoCubes.add(ammoCube3);
    }

    /**
     * Tells all the availables AmmoCube
     * @return the List of available AmmoCube
     */
    public List<AmmoCube> getRewards() {
        return this.ammoCubes;
    }

    /**
     * Tells if you can pick a Powerup
     * @return true if you can pick a Powerup
     */
    public boolean canPickPowerup() {
        return canPick;
    }
}
