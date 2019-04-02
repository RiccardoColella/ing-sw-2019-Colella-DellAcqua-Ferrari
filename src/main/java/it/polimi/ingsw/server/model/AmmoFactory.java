package it.polimi.ingsw.server.model;

public class AmmoFactory {

    /**
     * Class constructor
     */
    public AmmoFactory() {
    }


    /**
     * Creates a new Ammo, given the color
     * @param color color of the Ammo to be created
     * @return the new Ammo
     */
    public static Ammo create(CoinColor color) {
        Ammo ammo = new Ammo(color);
        return ammo;
    }
}