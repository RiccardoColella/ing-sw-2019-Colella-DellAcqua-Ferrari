package it.polimi.ingsw.server.model.player;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.SpawnpointBlock;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.battlefield.Direction;
import it.polimi.ingsw.server.model.currency.AmmoCube;
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
import it.polimi.ingsw.server.model.rewards.RewardFactory;
import it.polimi.ingsw.server.model.rewards.Reward;
import it.polimi.ingsw.server.model.weapons.Weapon;

import java.util.*;
import java.util.stream.Collectors;

import static it.polimi.ingsw.server.model.match.Match.Mode.FINAL_FRENZY;

/**
 * This class represents the player entity, storing all info about its status during the match
 */
public class Player implements Damageable, MatchModeChangedListener {

    private static final int MORTAL_DAMAGE = 11;

    private static final int MAX_OVERKILL_DAMAGE = 12;

    private static final int MAX_AMMO_CUBES_ALLOWED_FOR_COLOR = 3;
    private static final int MAX_POWERUPS_ALLOWED = 3;
    private static final int MAX_WEAPONS_ALLOWED = 3;
    private static final int MAX_MARKS_ALLOWED_BY_SAME_PLAYER = 3;

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
     * This property stores the ammoCubes owned by the player
     */
    private List<AmmoCube> ammoCubes = new LinkedList<>();

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

    private final Match match;

    private Reward currentReward = RewardFactory.create(RewardFactory.Type.STANDARD);

    private boolean isAlive = true;

    /**
     * This constructor creates a player from the basic info: the player will be empty and ready to start a new match
     * @param match the match this new player belongs to
     * @param info a PlayerInfo object containing the basic info
     */
    public Player(Match match, PlayerInfo info) {
        this.match = match;
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

        if (this.damageTokens.size() < MAX_OVERKILL_DAMAGE) {
            if (newTokens.size() + this.damageTokens.size() <= MAX_OVERKILL_DAMAGE) { // any extra damage after overkill will not be counted
                this.damageTokens.addAll(newTokens);
            } else {
                int last = (MAX_OVERKILL_DAMAGE) - this.damageTokens.size();
                this.damageTokens.addAll(newTokens.subList(0, last));
            }
            // >= because both PlayerDied and PlayerOverkilled can be caused by the same attack
            if (isAlive && this.damageTokens.size() >= MORTAL_DAMAGE) {
                isAlive = false;
                notifyPlayerDied(shooter);
            }
            if (this.damageTokens.size() == MAX_OVERKILL_DAMAGE) {
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
     * This method gets the ammoCubes currently owned by the player
     * @return a list of the ammoCubes the player owns
     */
    public List<AmmoCube> getAmmoCubes() {
        return this.ammoCubes;
    }

    /**
     * This method gets the powerups currently owned by the player
     * @return a list of the powerups the player owns
     */
    public List<PowerupTile> getPowerups() {
        return this.powerups;
    }

    /**
     * This method gets the player's information
     * @return a PlayerInfo object
     */
    public PlayerInfo getPlayerInfo() {
        return this.info;
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
    public Optional<CompoundAction> getActiveAction() {
        return Optional.ofNullable(this.activeAction);
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
     * This method allows the player to buy a new weapon when he already has the maximum allowed, exchanging it for one of those he owns
     * @param weapon the Weapon that the player is buying
     * @param ammoCubes the cost the player is paying with ammoCubes
     * @param powerups the cost the player is paying with powerups
     * @param discardedWeapon the Weapon the player is giving up for the new one
     *
     */
    public void grabWeapon(Weapon weapon, List<AmmoCube> ammoCubes, List<PowerupTile> powerups, Weapon discardedWeapon) {
        if (this.weapons.size() == MAX_WEAPONS_ALLOWED) {
            weapons.remove(discardedWeapon);
        }
        try {
            grabWeapon(weapon, ammoCubes, powerups);
        } catch (MissingOwnershipException ex) {
            weapons.add(discardedWeapon);
            throw ex;
        }
        discardedWeapon.setLoaded(false);
        Optional<Block> spawnpoint = this.match.getBoard().findPlayer(this);
        if (spawnpoint.isPresent()) {
            ((SpawnpointBlock) spawnpoint.get()).dropWeapon(weapon);
        } else {
            throw new UnauthorizedGrabException("Player is trying to grab a weapon from a non-existent spawnpoint");
        }
    }

    /**
     * This method allows the player to buy a new weapon, given that he has less than the maximum weapons allowed
     * @param weapon the Weapon that the player is buying
     * @param ammoCubes the cost the player is paying with ammoCubes
     * @param powerups the cost the player is paying with powerups
     */
    public void grabWeapon(Weapon weapon, List<AmmoCube> ammoCubes, List<PowerupTile> powerups) {
        if (this.weapons.size() < MAX_WEAPONS_ALLOWED) {
            this.pay(ammoCubes, powerups);
            weapon.setLoaded(true);
            weapons.add(weapon);
        } else throw new UnauthorizedGrabException("Player already has " + MAX_WEAPONS_ALLOWED + " weapons and needs to drop one in order to buy one");
    }

    /**
     * This method allows the player to grab a new powerup
     * @param powerup the Powerup the player is grabbing
     */
    public void grabPowerup(PowerupTile powerup) {
        if (this.powerups.size() < MAX_POWERUPS_ALLOWED) {
            this.powerups.add(powerup);
        } else throw new UnauthorizedGrabException("Player already had " + MAX_POWERUPS_ALLOWED +" powerups");
    }

    /**
     * This method allows the player to gram new ammoCubes
     * @param ammoCubes a list containing the ammoCubes the player is grabbing
     */
    public void grabAmmoCubes(List<AmmoCube> ammoCubes) {
            for (AmmoCube newAmmoCube : ammoCubes) {
                if (this.ammoCubes.stream()
                              .filter(a -> a.equalsTo(newAmmoCube))
                              .count() < MAX_AMMO_CUBES_ALLOWED_FOR_COLOR) {
                    this.ammoCubes.add(newAmmoCube);
                }
            }
    }

    /**
     * This method allows the player to reload a weapon it owns
     * @param weapon the Weapon that shall be reloaded
     * @param ammoCubes the cost the player is paying with ammoCubes
     * @param powerups the cost the player is paying with powerups
     */
    public void reload(Weapon weapon, List<AmmoCube> ammoCubes, List<PowerupTile> powerups) {
        if (!weapon.isLoaded()) {
            pay(ammoCubes, powerups);
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
                          .count() < MAX_MARKS_ALLOWED_BY_SAME_PLAYER) {
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
                    this.currentReward = RewardFactory.create(RewardFactory.Type.FINAL_FRENZY);
                }
                break;
            case STANDARD:
            case SUDDEN_DEATH:
                this.currentReward = RewardFactory.create(RewardFactory.Type.STANDARD);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void pay(List<AmmoCube> ammoCubes, List<PowerupTile> powerups) {
        for (AmmoCube spentAmmoCube : ammoCubes) {
            Optional<AmmoCube> paidAmmo = this.ammoCubes.stream().filter(ownedAmmo -> ownedAmmo.equalsTo(spentAmmoCube)).findAny();
            if (paidAmmo.isPresent()) {
                this.ammoCubes.remove(paidAmmo.get());
            } else throw new MissingOwnershipException("Player can't afford this weapon, missing ammoCubes");
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
                        .filter(coin -> coin instanceof AmmoCube)
                        .map(coin -> (AmmoCube) coin)
                        .collect(Collectors.toList()),
                coins.stream()
                        .filter(coin -> coin instanceof PowerupTile)
                        .map(coin -> (PowerupTile) coin)
                        .collect(Collectors.toList())
                );
    }

    public Reward getCurrentReward() {
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


    public boolean isAlive() {
        return isAlive;
    }

    // Called by the Controller after it has determined (asking the human player) where it should spawn, based on a Powerup selection
    public void bringBackToLife() {
        isAlive = true;
        damageTokens.clear();
        if (match.getMode() == FINAL_FRENZY) { // when a player dies during final frenzy, he flips his board
            this.currentReward = RewardFactory.create(RewardFactory.Type.FINAL_FRENZY);
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
