package it.polimi.ingsw.server.model;

public class DamageToken {
    /**
     * Player who damages
     */
    private Player attacker;

    /**
     * Class constructor
     * @param attacker
     */
    public DamageToken(Player attacker){
        //TODO all function
        this.attacker = attacker;
    }

    /**
     * Tells the who is attacking
     * @return the attacker
     */
    public Player getAttacker(){
        //TODO all function
        return attacker;
    }
}
