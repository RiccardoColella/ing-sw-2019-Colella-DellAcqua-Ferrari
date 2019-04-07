package it.polimi.ingsw.server.model.player;

import it.polimi.ingsw.server.model.DamageToken;
import it.polimi.ingsw.server.model.Damageable;
import it.polimi.ingsw.server.model.Match;
import it.polimi.ingsw.server.model.battlefield.Direction;
import it.polimi.ingsw.server.model.currency.Ammo;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.events.MatchModeChanged;
import it.polimi.ingsw.server.model.events.PlayerDied;
import it.polimi.ingsw.server.model.events.PlayerOverkilled;
import it.polimi.ingsw.server.model.events.PlayerReborn;
import it.polimi.ingsw.server.model.events.listeners.MatchModeChangedListener;
import it.polimi.ingsw.server.model.events.listeners.PlayerDiedListener;
import it.polimi.ingsw.server.model.events.listeners.PlayerOverkilledListener;
import it.polimi.ingsw.server.model.events.listeners.PlayerRebornListener;
import it.polimi.ingsw.server.model.exceptions.MissingOwnershipException;
import it.polimi.ingsw.server.model.exceptions.UnauthorizedGrabException;
import it.polimi.ingsw.server.model.factories.ActionTileFactory;
import it.polimi.ingsw.server.model.weapons.Weapon;

import java.util.*;
import java.util.stream.Collectors;

import static it.polimi.ingsw.server.model.Match.Mode.FINAL_FRENZY;

/**
 * This class represents the player entity, storing all info about its status during the match
 */
public class Player implements Damageable, MatchModeChangedListener {
    /**
     * This property represents the reward achievable for killing the player in standard mode
     */
    private static final int[] STANDARD_REWARD = {8, 6, 4, 2, 1, 1};

    /**
     * This property represents the reward achievable for killing the player in final frenzy mode
     */
    private static final int[] FINAL_FRENZY_REWARD = {2, 1, 1, 1};

    private static final int MORTAL_DAMAGE = 11;
    /**
     * This property represents the marks received by the player during its current life
     */
    private List<DamageToken> marks = new LinkedList<>();

    /**
     * This property represents the damage received by the player during its current life
     */
    private List<DamageToken> damageTokens = new LinkedList<>();

    /**
     * This property stores the weapons owned by the player
     */
    private List<Weapon> weapons = new LinkedList<>();

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
    private List<Ammo> ammos = new LinkedList<>();

    /**
     * This property stores the powerups owned by the player
     */
    private List<PowerupTile> powerups = new LinkedList<>();

    /**
     * This property stores the basic player info
     */
    private PlayerInfo info;

    /**
     * This property stores the action that the player is currently doing, null if it is not doing anything
     */
    private CompoundAction activeAction;

    /**
     * This property stores the weapon the player is currently using
     */
    private Weapon activeWeapon;

    private List<PlayerDiedListener> playerDiedListeners = new ArrayList<>();
    private List<PlayerOverkilledListener> playerOverkilledListeners = new ArrayList<>();
    private List<PlayerRebornListener> playerRebornListeners = new ArrayList<>();

    private Match match;

    private int[] currentReward = STANDARD_REWARD;

    private boolean firstBloodMatters = true;

    private boolean isAlive = true;

    /**
     * This constructor creates a player from the basic info: the player will be empty and ready to start a new match
     * @param info a PlayerInfo object containing the basic info
     */
    public Player(PlayerInfo info) {
        this.info = info;
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
     * @param newTokens a list of DamageToken that should be given to the Damageable
     */
    @Override
    public void addDamageTokens(List<DamageToken> newTokens) {
        Player shooter = newTokens.get(0).getAttacker();
        List<DamageToken> extraDamage = new LinkedList<>();

        this.getMarks()
                .stream()
                .filter(mark -> mark.getAttacker() == shooter)
                .forEach(extraDamage::add); // if the player had marks from the attacker, they become damage

        if (!extraDamage.isEmpty()) {
            newTokens.addAll(extraDamage); // marks are added to the new damage
            this.getMarks().removeAll(extraDamage); // and they are removed from the mark list
        }

        if (this.damageTokens.size() < MORTAL_DAMAGE + 1) {
            if (newTokens.size() + this.damageTokens.size() <= MORTAL_DAMAGE + 1) { // any extra damage after overkill will not be counted
                this.damageTokens.addAll(newTokens);
            } else {
                int last = (MORTAL_DAMAGE + 1) - this.damageTokens.size();
                this.damageTokens.addAll(newTokens.subList(0, last));
            }
            // >= because both PlayerDied and PlayerOverkilled can be caused by the same attack
            if (isAlive && this.damageTokens.size() >= MORTAL_DAMAGE) {
                isAlive = false;
                notifyPlayerDied(shooter);
            }
            if (this.damageTokens.size() == MORTAL_DAMAGE + 1) {
                notifyPlayerOverkilled(shooter);
            }
        }
    }


    /**
     * This method assigns a single damage token to a Damageable
     *
     * @param damageToken the DamageToken that should be given to the Damageable
     */
    @Override
    public void addDamageToken(DamageToken damageToken) {
        this.addDamageTokens(new LinkedList<>(Collections.singletonList(damageToken)));
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
        return new ArrayList<>();
    }

    /**
     * This method selects a weapon from the loaded ones owned by the player and sets it as active
     * @param weapon a loaded weapon owned by the player
     */
    public void chooseWeapon(Weapon weapon) {
        if (this.weapons.contains(weapon) && weapon.isLoaded()) {
            this.activeWeapon = weapon;
        }
    }

    /**
     * This method resets the active weapon of the player putting it back to null
     */
    public void putAwayActiveWeapon() {
        this.activeWeapon = null;
    }

    /**
     * This method shows the weapon currently used by the player
     * @return the weapon currently used by the player, null if the player is not shooting
     */
    public Optional<Weapon> getActiveWeapon() {
        return Optional.ofNullable(this.activeWeapon);
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
        match.getBoard().movePlayer(this, direction);
    }

    /**
     * This method allows the player to buy a new weapon when he already has 3, exchanging it for one of those he owns
     * @param weapon the Weapon that the player is buying
     * @param ammos the cost the player is paying with ammos
     * @param powerups the cost the player is paying with powerups
     * @param discardedWeapon the Weapon the player is giving up for the new one
     *
     */
    public void grabWeapon(Weapon weapon, List<Ammo> ammos, List<PowerupTile> powerups, Weapon discardedWeapon) {
        if (this.weapons.size() == 3) {
            weapons.remove(discardedWeapon);
        }
        try {
            grabWeapon(weapon, ammos, powerups);
        } catch (MissingOwnershipException ex) {
            weapons.add(discardedWeapon);
            throw ex;
        }
        discardedWeapon.setLoaded(false);
        //TODO: put the discarded weapon back into the spawnpoint
    }

    /**
     * This method allows the player to buy a new weapon, given that he has less than 3 weapons
     * @param weapon the Weapon that the player is buying
     * @param ammos the cost the player is paying with ammos
     * @param powerups the cost the player is paying with powerups
     */
    public void grabWeapon(Weapon weapon, List<Ammo> ammos, List<PowerupTile> powerups) {
        if (this.weapons.size() < 3) {
            this.pay(ammos, powerups);
            weapon.setLoaded(true);
            weapons.add(weapon);
        } else throw new UnauthorizedGrabException("Player already has 3 weapons and needs to drop one in order to buy one");
    }

    /**
     * This method allows the player to grab a new powerup
     * @param powerup the Powerup the player is grabbing
     */
    public void grabPowerup(PowerupTile powerup) {
        if (this.powerups.size() < 3) {
            this.powerups.add(powerup);
        } else throw new UnauthorizedGrabException("Player already had 3 powerups");
    }

    /**
     * This method allows the player to gram new ammos
     * @param ammos a list containing the ammos the player is grabbing
     */
    public void grabAmmos(List<Ammo> ammos) {
            for (Ammo newAmmo : ammos) {
                if (this.ammos.stream()
                              .filter(a -> a.equalsTo(newAmmo))
                              .count() < 3) {
                    this.ammos.add(newAmmo);
                }
            }
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    /**
     * This method allows the player to reload a weapon it owns
     * @param weapon the Weapon that shall be reloaded
     * @param ammos the cost the player is paying with ammos
     * @param powerups the cost the player is paying with powerups
     */
    public void reload(Weapon weapon, List<Ammo> ammos, List<PowerupTile> powerups) {
        if (!weapon.isLoaded()) {
            pay(ammos, powerups);
            weapon.setLoaded(true);
        }
    }

    public void addSkull() {
        skulls++;
    }

    public void addMarks(List<DamageToken> marks) {
        for (DamageToken mark : marks) {
            if (this.marks.stream()
                          .filter(t -> t.getAttacker() == mark.getAttacker())
                          .count() < 3) {
                this.marks.add(mark);
            }
        }
    }

    public void addMark(DamageToken mark) {
        this.addMarks(new LinkedList<>(Collections.singletonList(mark)));
    }

    public void addPlayerDiedListener(PlayerDiedListener listener) {
        playerDiedListeners.add(listener);
    }

    public void addPlayerOverkilledListener(PlayerOverkilledListener listener) {
        playerOverkilledListeners.add(listener);
    }

    public void addPlayerRebornListener(PlayerRebornListener listener) {
        playerRebornListeners.add(listener);
    }

    @Override
    public void onMatchModeChanged(MatchModeChanged event) {
        switch (event.getMode()) {
            case FINAL_FRENZY:
                if (this.damageTokens.isEmpty()) {
                    this.currentReward = FINAL_FRENZY_REWARD;
                    this.firstBloodMatters = false;
                }
                break;
            case STANDARD:
            case SUDDEN_DEATH:
                this.currentReward = STANDARD_REWARD;
                this.firstBloodMatters = true;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void pay(List<Ammo> ammos, List<PowerupTile> powerups) {
        for (Ammo spentAmmo : ammos) {
            Optional<Ammo> paidAmmo = this.ammos.stream().filter(ownedAmmo -> ownedAmmo.equalsTo(spentAmmo)).findAny();
            if (paidAmmo.isPresent()) {
                this.ammos.remove(paidAmmo.get());
            } else throw new MissingOwnershipException("Player can't afford this weapon, missing ammos");
        }

        for (PowerupTile spentPowerup : powerups) {
            Optional<PowerupTile> paidPowerup = this.powerups.stream().filter(ownedAmmo -> ownedAmmo.equalsTo(spentPowerup)).findAny();
            if (paidPowerup.isPresent()) {
                this.powerups.remove(paidPowerup.get());
            } else throw new MissingOwnershipException("Player can't afford this weapon, missing powerups");
        }
    }

    public void pay(List<Coin> coins) {
        this.pay(
                coins.stream()
                        .filter(coin -> coin instanceof Ammo)
                        .map(coin -> (Ammo) coin)
                        .collect(Collectors.toList()),
                coins.stream()
                        .filter(coin -> coin instanceof PowerupTile)
                        .map(coin -> (PowerupTile) coin)
                        .collect(Collectors.toList())
                );
    }

    public int[] getCurrentReward() {
        return this.currentReward;
    }

    /**
     * This method determines the status of a player at a given time, based on:
     * - player's health
     * - current match mode
     * - final frenzy turns
     * Then the ActionTileFactory is called to return the correspondent set of macro actions
     *
     * @return the action tile representing the available macro actions the player can do
     */
    public ActionTile getAvailableMacroActions() {

        if (match.getMode() != FINAL_FRENZY) {

            if (this.damageTokens.size() > 5) {
                return ActionTileFactory.create(ActionTile.Type.ADRENALINE_2);
            } else if (this.damageTokens.size() > 2) {
                return ActionTileFactory.create(ActionTile.Type.ADRENALINE_1);
            } else {
                return ActionTileFactory.create(ActionTile.Type.STANDARD);
            }
        } else {
            if (match.getPlayers().get(0) == this || match.getPlayersWhoDidFinalFrenzyTurn().contains(match.getPlayers().get(0))) {
                return ActionTileFactory.create(ActionTile.Type.FINAL_FRENZY_SINGLE_MODE);
            } else {
                return ActionTileFactory.create(ActionTile.Type.FINAL_FRENZY_DOUBLE_MODE);
            }
        }
    }

    public boolean firstBloodMatters() {
        return firstBloodMatters;
    }

    public boolean isAlive() {
        return isAlive;
    }

    // Called by the Controller after it has determined (asking the human player) where it should spawn, based on a Powerup selection
    public void bringBackToLife() {
        isAlive = true;
        damageTokens.clear();
        if (match.getMode() == FINAL_FRENZY) { // when a player dies during final frenzy, he flips his board
            this.firstBloodMatters = false;
            this.currentReward = FINAL_FRENZY_REWARD;
        }
        notifyPlayerBroughtBackToLife();
    }

    private void notifyPlayerBroughtBackToLife() {
        PlayerReborn e = new PlayerReborn(this);
        playerRebornListeners.forEach(l -> l.onPlayerReborn(e));
    }


    private void notifyPlayerOverkilled(Player killer) {
        PlayerOverkilled e = new PlayerOverkilled(this, killer);
        playerOverkilledListeners.forEach(l -> l.onPlayerOverkilled(e));
    }

    private void notifyPlayerDied(Player killer) {
        PlayerDied e = new PlayerDied(this, killer);
        this.playerDiedListeners.forEach(listener -> listener.onPlayerDied(e));
    }
}
