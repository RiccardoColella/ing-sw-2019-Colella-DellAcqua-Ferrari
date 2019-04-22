package it.polimi.ingsw.server.model.currency;

import java.util.ArrayList;
import java.util.Collections;
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
        List<AmmoCube> cubes = new ArrayList<>();
        cubes.add(ammoCube1);
        cubes.add(ammoCube2);
        this.ammoCubes = Collections.unmodifiableList(cubes);
    }

    /**
     * Class constructor. With 3 AmmoCube you can't pick a PowerUp
     * @param ammoCube1 the first AmmoCube cube
     * @param ammoCube2 the second AmmoCube cube
     * @param ammoCube3 the third AmmoCube cube
     */
    public BonusTile(AmmoCube ammoCube1, AmmoCube ammoCube2, AmmoCube ammoCube3) {
        this.canPick = false;
        List<AmmoCube> cubes = new ArrayList<>();
        cubes.add(ammoCube1);
        cubes.add(ammoCube2);
        cubes.add(ammoCube3);
        this.ammoCubes = Collections.unmodifiableList(cubes);
    }

    /**
     * Copy constructor
     * @param copy the BonusTile that will be copied
     */
    public BonusTile(BonusTile copy) {
        this.canPick = copy.canPick;
        this.ammoCubes = Collections.unmodifiableList(copy.ammoCubes);
    }

    /**
     * Tells all the available AmmoCubes
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
