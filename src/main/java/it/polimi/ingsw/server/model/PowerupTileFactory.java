package it.polimi.ingsw.server.model;

/**
 * This class creates all the powerup tiles needed in the game
 */
public final class PowerupTileFactory {

    /**
     * Private empty constructor because this class should not have instances
     */
    private PowerupTileFactory() {

    }

    /**
     * This method creates a new powerup tile given its type and its color
     * @param type a value of the PowerupType enum
     * @param color a value of the CoinColor enum
     * @return a new powerup tile with the given characteristics
     */
    public static PowerupTile create(PowerupType type, CoinColor color) {
        String name;
        switch (type) {
            case NEWTON:
                name = "Newton";
                break;
            case TELEPORTER:
                name = "Teleporter";
                break;
            case TAGBACK_GRENADE:
                name = "Tagback Grenade";
                break;
            case TARGETING_SCOPE:
                name = "Targeting Scope";
                break;
            default:
                name = "";
                //exception should be thrown
                break;
        }
        return new PowerupTile(color, name, type);
    }
}
