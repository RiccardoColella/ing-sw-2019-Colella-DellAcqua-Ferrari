package it.polimi.ingsw.server.model;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class Player implements Damageable{
    /**
     * This property represents the reward achievable for killing the player in standard mode
     */
    private static final int[] STANDARD_REWARD = {8, 6, 4, 2, 1, 1};

    /**
     * This property represents the reward achievable for killing the player in final frenzy mode
     */
    private static final int[] FINAL_FRENZY_REWARD = {2, 1, 1, 1};

    /**
     * This property represents the marks received by the player during its current life
     */
    private List<DamageToken> marks;

    /**
     * This property represents the damage received by the player during its current life
     */
    private List<DamageToken> damageTokens;

    /**
     * This property stores the weapons owned by the player
     */
    private List<Weapon> weapons;

    /**
     * This property stores the points earned by the player
     */
    private int points;

    /**
     * This property stores the skulls owned by the player
     */
    private int skulls;

    /**
     * This property stores the ammos owned by the player
     */
    private List<Ammo> ammos;

    /**
     * This property stores the powerups owned by the player
     */
    private List<PowerupTile> powerups;

    /**
     * This property stores the basic player info
     */
    private PlayerInfo info;

    /**
     * This property stores the action that the player is currently doing, null if it is not doing anything
     */
    private CompoundAction activeAction;

    /**
     * This constructor creates a player from the basic info: the player will be empty and ready to start a new match
     * @param info a PlayerInfo object containing the basic info
     */
    public Player(PlayerInfo info) {
        this.info = info;
        this.marks = new LinkedList<>();
        this.damageTokens = new LinkedList<>();
        this.points = 0;
        this.skulls = 0;
        this.ammos = new LinkedList<>();
        this.powerups = new LinkedList<>();
        this.weapons = new LinkedList<>();
        this.activeAction = null;
    }

    /**
     * This constructor creates a player given all info about its status: the player will be restored to an existing status and ready to continue a saved match
     * @param info a PlayerInfo object containing the basic info
     * @param damageTokens a list containing the damage of the player
     * @param marks a list containing the marks of the player
     * @param weapons a list containing the weapons owned by the player
     * @param points an int representing the points scored by the player so far
     * @param skulls an int representing the skulls owned by the player
     * @param ammos a list containing the ammos owned by the player
     * @param powerups a list containing the powerups owned by the player
     * @param activeAction the action the player is currently doing
     */
    public Player(PlayerInfo info, List<DamageToken> damageTokens, List<DamageToken> marks, List<Weapon> weapons, int points, int skulls, List<Ammo> ammos, List<PowerupTile> powerups, CompoundAction activeAction) {
        this.info = info;
        this.damageTokens = damageTokens;
        this.marks = marks;
        this.weapons = weapons;
        this.powerups = powerups;
        this.points = points;
        this.skulls = skulls;
        this.ammos = ammos;
        this.activeAction = activeAction;
    }

    /**
     * This method returns the damage tokens received so far by this Damageable
     *
     * @return a list containing the DamageToken received so far, the list will be empty if there are none
     */
    @Override
    public List<DamageToken> getDamageTokens() {
        return this.damageTokens;
    }

    /**
     * This method assigns damage tokens to a Damageable
     *
     * @param damageTokens a list of DamageToken that should be given to the Damageable
     */
    @Override
    public void addDamageTokens(@NotNull List<DamageToken> damageTokens) {

    }

    /**
     * This method returns the marks received so far by this player
     * @return a list of DamageToken containing the marks received so far, the list will be empty if there are none
     */
    public List<DamageToken> getMarks() {
        return this.marks;
    }

    /**
     * This method returns the weapons owned by this player
     * @return a list of the weapons owned by this player
     */
    public List<Weapon> getWeapons() {
        return this.weapons;
    }

    /**
     * This method calculates the actions that the player can choose from for what to do next
     * @return a list of BasicAction representing the actions available to the player
     */
    public List<BasicAction> getAvailableActions() {
        return null;
    }

    /**
     * This method selects a weapon from the loaded ones owned by the player and sets it as active
     * @param weapon a loaded weapon owned by the player
     */
    public void chooseWeapon(Weapon weapon) {

    }

    /**
     * This method shows the weapon currently used by the player
     * @return the weapon currently used by the player, null if the player is not shooting
     */
    public Weapon getActiveWeapon() {
        return null;
    }

    /**
     * This method gets the point earned by the player
     * @return an int representing the points earned by the player
     */
    public int getPoints() {
        return this.points;
    }

    /**
     * This methods adds new points to the points previously earned by the player
     * @param points an int representing the points that should be added
     */
    public void addPoints(int points) {
        this.points += points;
    }

    /**
     * This method gets the color of the player's pawn
     * @return the PlayerColor corresponding to the player's pawn
     */
    public PlayerColor getColor() {
        return this.info.getColor();
    }

    /**
     * This method gets the ammos currently owned by the player
     * @return a list of the ammos the player owns
     */
    public List<Ammo> getAmmos() {
        return this.ammos;
    }

    /**
     * This method gets the powerups currently owned by the player
     * @return a list of the powerups the player owns
     */
    public List<PowerupTile> getPowerups() {
        return this.powerups;
    }

    /**
     * This method gets the player's nickname
     * @return a string representing the player's nickname
     */
    public String getNickname() {
        return this.info.getNickname();
    }

    /**
     * This method gets the skulls currently owned by the player
     * @return an int representing the number of skulls owned by the player
     */
    public int getSkulls() {
        return this.skulls;
    }

    /**
     * This method gets the action the player is currently doing
     * @return the CompoundAction the player is currently doing, or null if it's not doing anything
     */
    public CompoundAction getActiveAction() {
        return this.activeAction;
    }

    /**
     * This method sets a new active action for the player
     * @param activeAction the CompoundAction the player has started doing
     */
    public void setActiveAction(CompoundAction activeAction) {
        this.activeAction = activeAction;
    }

    /**
     * This method sets the direction the player should be moved to
     * @param direction the Direction the player should be moved to
     */
    public void move(Direction direction) {

    }

    /**
     * This method allows the player to buy a new weapon
     * @param weapon the Weapon that the player is buying
     * @param ammos the cost the player is paying with ammos
     * @param powerups the cost the player is paying with powerups
     * @param discardedWeapon the Weapon the player is giving up for the new one, if it already has the maximum number allowed
     */
    public void grabWeapon(Weapon weapon, List<Ammo> ammos, List<PowerupTile> powerups, Weapon discardedWeapon) {

    }

    /**
     * This method allows the player to grab a new powerup
     * @param powerup the Powerup the player is grabbing
     */
    public void grabPowerup(PowerupTile powerup) {

    }

    /**
     * This method allows the player to gram new ammos
     * @param ammos a list containing the ammos the player is grabbing
     */
    public void grabAmmos(List<Ammo> ammos) {

    }

    /**
     * This method allows the player to reload a weapon it owns
     * @param weapon the Weapon that shall be reloaded
     * @param ammos the cost the player is paying with ammos
     * @param powerups the cost the player is paying with powerups
     */
    public void reload(Weapon weapon, List<Ammo> ammos, List<PowerupTile> powerups) {

    }

    /**
     * This method allows the player to use a weapon to shoot
     * @param weapon the Weapon the player wants to use
     * @param ammos the cost the player is paying with ammos
     * @param powerups the cost the player is paying with powerups
     */
    public void shoot(Weapon weapon, List<Ammo> ammos, List<PowerupTile> powerups) {

    }

}
