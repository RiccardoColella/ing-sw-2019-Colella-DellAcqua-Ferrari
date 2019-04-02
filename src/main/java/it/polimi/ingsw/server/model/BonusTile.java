package it.polimi.ingsw.server.model;

import java.util.List;

public class BonusTile {

    /**
     * variable set to true if the BonusTile is not full
     */
    private boolean canPick;

    /**
     * Class constructor. With 2 Ammo you can pick a PowerUp
     * @param ammo1
     * @param ammo2
     */
    public BonusTile(Ammo ammo1, Ammo ammo2){
        //TODO all function
        this.canPick = true;
    }

    /**
     * Class constructor. With 3 Ammo you can't pick a PowerUp
     * @param ammo1
     * @param ammo2
     * @param ammo3
     */
    public BonusTile(Ammo ammo1, Ammo ammo2, Ammo ammo3){
        //TODO all function
        this.canPick = false;
    }

    /**
     * Tells all the availables Coin
     * @return the List of available Coins
     */
    public List<Coin> getRewards(){
        //TODO all function
        return null;
    }

    /**
     * Tells if you can pick a Powerup
     * @return true if you can pick a Powerup
     */
    public boolean canPickPowerup(){
        return canPick;
    }
}
