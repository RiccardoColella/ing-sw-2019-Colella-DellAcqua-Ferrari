package it.polimi.ingsw.server.model.player;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.currency.AmmoCube;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.events.*;
import it.polimi.ingsw.server.model.events.listeners.*;
import it.polimi.ingsw.server.model.exceptions.MissingOwnershipException;
import it.polimi.ingsw.server.model.exceptions.UnauthorizedExchangeException;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.rewards.Reward;
import it.polimi.ingsw.server.model.rewards.RewardFactory;
import it.polimi.ingsw.server.model.weapons.WeaponTile;
import it.polimi.ingsw.shared.Direction;

import java.util.*;
import java.util.stream.Collectors;

import static it.polimi.ingsw.server.model.match.Match.Mode.FINAL_FRENZY;

/**
 * This class represents the player entity, storing all info about its status during the match
 */
public class Player implements Damageable, MatchModeChangedListener {

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
    private List<WeaponTile> weapons = new LinkedList<>();

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
    private WeaponTile activeWeapon;

    /**
     * This property stores the listeners to the PlayerDied event
     */
    private List<PlayerDiedListener> playerDiedListeners = new ArrayList<>();

    /**
     * This property stores the listeners to the PlayerOverkilled event
     */
    private List<PlayerOverkilledListener> playerOverkilledListeners = new ArrayList<>();

    /**
     * This property stores the listeners to the PlayerReborn event
     */
    private List<PlayerRebornListener> playerRebornListeners = new ArrayList<>();

    /**
     * This property stores the listeners to the PlayerDamaged event
     */
    private List<PlayerDamagedListener> playerDamagedListeners = new ArrayList<>();

    /**
     * This property stores the match the Player is participating to
     */
    private final Match match;

    /**
     * This property stores the reward that will be given to who kills or damages the player
     */
    private Reward currentReward = RewardFactory.create(RewardFactory.Type.STANDARD);

    /**
     * This property represents the life status of the player
     */
    private boolean isAlive = true;

    /**
     * This property stores the constraints given to the player, such as how much damage can be received or how much AmmoCubes can be owned
     */
    private final PlayerConstraints constraints;

    /**
     * This constructor creates a player from the basic info: the player will be empty and ready to start a new match
     * @param match the match this new player belongs to
     * @param info a PlayerInfo object containing the basic info
     * @param constraints a PlayerConstraints object containing a set of rules regarding the health and the scoring
     */
    public Player(Match match, PlayerInfo info, PlayerConstraints constraints) {
        this.match = match;
        this.info = info;
        this.constraints = constraints;
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
     * This method assigns damage tokens to a Player. It checks how much damage can still be added and notifies
     * the listeners in case of death or overkill
     * @param newTokens a list of DamageToken that should be given to the Player
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

        //checking whether the player can still receive some damage
        if (this.damageTokens.size() < constraints.getMaxDamage()) {
            if (newTokens.size() + this.damageTokens.size() <= constraints.getMaxDamage()) {
                //all new damage can be added
                this.damageTokens.addAll(newTokens);
            } else {
                //any extra damage after overkill will not be counted
                int last = (constraints.getMaxDamage()) - this.damageTokens.size();
                this.damageTokens.addAll(newTokens.subList(0, last));
            }
            // >= because both PlayerDied and PlayerOverkilled can be caused by the same attack
            if (isAlive && this.damageTokens.size() >= constraints.getMortalDamage()) {
                isAlive = false;
                notifyPlayerDied(shooter);
            }
            //if the player received damage and his damage is equal to the max receivable, he has been overkilled
            if (this.damageTokens.size() == constraints.getMaxDamage()) {
                notifyPlayerOverkilled(shooter);
            }
        }

        notifyPlayerDamaged(shooter);
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
    public List<WeaponTile> getWeapons() {
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
    public void chooseWeapon(WeaponTile weapon) {
        //the weapon will be set as active only if it is loaded and it belongs to the player
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
    public Optional<WeaponTile> getActiveWeapon() {
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
     * @param weapon the WeaponTile that the player is buying
     * @param ammoCubes the cost the player is paying with ammoCubes
     * @param powerups the cost the player is paying with powerups
     * @param discardedWeapon the WeaponTile the player is giving up for the new one
     *
     */
    public void grabWeapon(WeaponTile weapon, List<AmmoCube> ammoCubes, List<PowerupTile> powerups, WeaponTile discardedWeapon) {
        if (this.weapons.size() == constraints.getMaxWeaponsForPlayer()) {
            //the weapon should only be discarded if the player already has the maximum number available
            if (!weapons.remove(discardedWeapon)) {
                //if the player did not own the discarded weapon, an exception is thrown
                throw new IllegalArgumentException("Discarded weapon does not belong to the player");
            }
        } else throw new IllegalArgumentException("Player should not discard a weapon if he only owns " + this.weapons.size() + " and the maximum is " + this.constraints.getMaxWeaponsForPlayer());

        if (this.weapons.size() < constraints.getMaxWeaponsForPlayer()) {
            //now the weapon can be grabbed if the player has enough money to pay for it
            grabWeapon(weapon, ammoCubes, powerups);
        } else {
            //if the player could not pay for the weapon, the discarded weapon is given back to him
            weapons.add(discardedWeapon);
            throw new UnauthorizedExchangeException("Player already has " + constraints.getMaxWeaponsForPlayer() + " weapons and needs to drop one in order to buy one");
        }

        //the discarded weapon is put back to the spawnpoint
        Optional<Block> spawnpoint = this.match.getBoard().findPlayer(this);
        if (spawnpoint.isPresent()) {
            discardedWeapon.setLoaded(false);
            spawnpoint.get().drop(weapon);
        } else {
            throw new UnauthorizedExchangeException("Player is trying to put a weapon into a nonexisting block");
        }
    }

    /**
     * This method allows the player to buy a new weapon when he already has the maximum allowed, exchanging it for one of those he owns
     * @param weapon the WeaponTile that the player is buying
     * @param coins the list of Coins the player is using to pay
     * @param discardedWeapon the WeaponTile the player is giving up for the new one
     */
    public void grabWeapon(WeaponTile weapon, List<Coin> coins, WeaponTile discardedWeapon) {
        if (this.weapons.size() == constraints.getMaxWeaponsForPlayer()) {
            //the weapon should only be discarded if the player already has the maximum number available
            if (!weapons.remove(discardedWeapon)) {
                //if the player did not own the discarded weapon, an exception is thrown
                throw new IllegalArgumentException("Discarded weapon does not belong to the player");
            }
        } else throw new IllegalArgumentException("Player should not discard a weapon if he only owns " + this.weapons.size() + " and the maximum is " + this.constraints.getMaxWeaponsForPlayer());

        if (this.weapons.size() < constraints.getMaxWeaponsForPlayer()) {
            //now the weapon can be grabbed if the player has enough money to pay for it
            grabWeapon(weapon, coins);
        } else {
            //if the player could not pay for the weapon, the discarded weapon is given back to him
            weapons.add(discardedWeapon);
            throw new UnauthorizedExchangeException("Player already has " + constraints.getMaxWeaponsForPlayer() + " weapons and needs to drop one in order to buy one");
        }

        //the discarded weapon is put back to the spawnpoint
        Optional<Block> spawnpoint = this.match.getBoard().findPlayer(this);
        if (spawnpoint.isPresent()) {
            discardedWeapon.setLoaded(false);
            spawnpoint.get().drop(weapon);
        } else {
            throw new UnauthorizedExchangeException("Player is trying to put a weapon into a nonexisting block");
        }
    }

    /**
     * This method allows the player to buy a new weapon, given that he has less than the maximum weapons allowed
     * @param weapon the WeaponTile that the player is buying
     * @param ammoCubes the cost the player is paying with ammoCubes
     * @param powerups the cost the player is paying with powerups
     */
    public void grabWeapon(WeaponTile weapon, List<AmmoCube> ammoCubes, List<PowerupTile> powerups) {
        if (this.weapons.size() < constraints.getMaxWeaponsForPlayer()) {
            //if the player can grab more weapons, he pays and gets the weapon
            this.pay(ammoCubes, powerups);
            weapon.setLoaded(true);
            weapons.add(weapon);
        } else throw new UnauthorizedExchangeException("Player already has " + constraints.getMaxWeaponsForPlayer() + " weapons and needs to drop one in order to buy one");
    }

    /**
     * This method allows the player to buy a new weapon, given that he has less than the maximum weapons allowed
     * @param weapon the WeaponTile that the player is buying
     * @param coins the list of Coins the player is using to pay
     */
    public void grabWeapon(WeaponTile weapon, List<Coin> coins) {
        if (this.weapons.size() < constraints.getMaxWeaponsForPlayer()) {
            //if the player can grab more weapons, he pays and gets the weapon
            this.pay(coins);
            weapon.setLoaded(true);
            weapons.add(weapon);
        } else throw new UnauthorizedExchangeException("Player already has " + constraints.getMaxWeaponsForPlayer() + " weapons and needs to drop one in order to buy one");
    }

    /**
     * This method allows the player to grab a new powerup
     * @param powerup the Powerup the player is grabbing
     */
    public void grabPowerup(PowerupTile powerup) {
        //player is allowed to grab a powerup only if he has not reached the max available
        if (this.powerups.size() < constraints.getMaxPowerupsForPlayer()) {
            this.powerups.add(powerup);
        } else throw new UnauthorizedExchangeException("Player already had " + constraints.getMaxPowerupsForPlayer() +" powerups");
    }

    /**
     * This method allows the player to gram new ammoCubes
     * @param ammoCubes a list containing the ammoCubes the player is grabbing
     */
    public void grabAmmoCubes(List<AmmoCube> ammoCubes) {
        //a player can always try to grab ammo cubes, but they will only be added if the max allowed has not been reached
        for (AmmoCube newAmmoCube : ammoCubes) {
            if (this.ammoCubes.stream()
                          .filter(a -> a.equalsTo(newAmmoCube))
                          .count() < constraints.getMaxAmmoCubesOfAColor()) {
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
    public void reload(WeaponTile weapon, List<AmmoCube> ammoCubes, List<PowerupTile> powerups) {
        if (!weapon.isLoaded() && this.getWeapons().contains(weapon)) {
            pay(ammoCubes, powerups);
            weapon.setLoaded(true);
        } else throw new IllegalArgumentException("Weapon could not be loaded");
    }

    /**
     * This method allows the player to reload a weapon it owns
     * @param weapon the Weapon that shall be reloaded
     * @param coins the cost the player is paying with ammoCubes and powerups
     */
    public void reload(WeaponTile weapon, List<Coin> coins) {
        if (!weapon.isLoaded() && this.getWeapons().contains(weapon)) {
            pay(coins);
            weapon.setLoaded(true);
        } else throw new IllegalArgumentException("Weapon could not be loaded");
    }

    /**
     * This method adds a skull to the player
     */
    public void addSkull() {
        skulls++;
    }

    /**
     * This method adds marks to the player
     * @param marks a list of DamageToken representing the marks to add
     */
    public void addMarks(List<DamageToken> marks) {
        //adding marks to the player if the attacker had not already given him the max allowed
        for (DamageToken mark : marks) {
            if (this.marks.stream()
                          .filter(t -> t.getAttacker() == mark.getAttacker())
                          .count() < constraints.getMaxMarksFromPlayer()) {
                this.marks.add(mark);
            }
        }
    }

    /**
     * This method adds a mark to the player
     * @param mark the DamageToken representing the mark
     */
    public void addMark(DamageToken mark) {
        this.addMarks(new LinkedList<>(Collections.singletonList(mark)));
    }

    /**
     * This method adds a new listener of PlayerDied event to the list
     * @param listener the PlayerDiedListener to add
     */
    public void addPlayerDiedListener(PlayerDiedListener listener) {
        if (!this.playerDiedListeners.contains(listener)) {
            playerDiedListeners.add(listener);
        }
    }

    /**
     * This method adds a new listener of PlayerDamaged event to the list
     * @param listener the PlayerDiedListener to add
     */
    public void addPlayerDamagedListener(PlayerDamagedListener listener) {
        if (!this.playerDamagedListeners.contains(listener)) {
            playerDamagedListeners.add(listener);
        }
    }

    /**
     * This method adds a new listener of PlayerOverkilled event to the list
     * @param listener the PlayerOverkilledListener to add
     */
    public void addPlayerOverkilledListener(PlayerOverkilledListener listener) {
        if (!this.playerOverkilledListeners.contains(listener)) {
            playerOverkilledListeners.add(listener);
        }
    }

    /**
     * This method adds a new listener of PlayerReborn event to the list
     * @param listener the PlayerRebornListener to add
     */
    public void addPlayerRebornListener(PlayerRebornListener listener) {
        if (!this.playerRebornListeners.contains(listener)) {
            playerRebornListeners.add(listener);
        }
    }

    /**
     * This method is called when the match mode changes, so players check if they need to change their reward
     * @param event the MatchModeChanged event
     */
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
                throw new IllegalArgumentException("Unrecognizable match mode");
        }
    }

    /**
     * This method handles payment given a list of ammo cubes and a list of powerups
     * @param spentAmmoCubes the list of ammo cubes spent by the player
     * @param spentPowerups the list of powerups spent by the player
     */
    private void pay(List<AmmoCube> spentAmmoCubes, List<PowerupTile> spentPowerups) {
        //saving the ammo cubes spent because if the player can't complete the payment correctly, the initial situation
        //will be restored
        List<AmmoCube> alreadyPaidAmmoCubes = new LinkedList<>();

        for (AmmoCube spentAmmoCube : spentAmmoCubes) {
            Optional<AmmoCube> paidAmmo = this.ammoCubes.stream().filter(ownedAmmo -> ownedAmmo.equalsTo(spentAmmoCube)).findAny();
            if (paidAmmo.isPresent()) {
                //if the player owns the ammo, it is removed from his wallet
                this.ammoCubes.remove(paidAmmo.get());
                alreadyPaidAmmoCubes.add(paidAmmo.get());
            } else {
                //already paid ammo cubes are given back to the player and an exception is thrown
                grabAmmoCubes(alreadyPaidAmmoCubes);
                throw new MissingOwnershipException("Player can't afford this weapon, missing ammoCubes");
            }
        }

        //as for ammo cubes, spent powerups are temporarily stored
        List<PowerupTile> alreadyPaidPowerups = new LinkedList<>();
        for (PowerupTile spentPowerup : spentPowerups) {
            Optional<PowerupTile> paidPowerup = this.powerups.stream().filter(ownedAmmo -> ownedAmmo.equalsTo(spentPowerup)).findAny();
            if (paidPowerup.isPresent()) {
                //if the player owns the powerup, it is removed from his wallet
                this.powerups.remove(paidPowerup.get());
                alreadyPaidPowerups.add(paidPowerup.get());
            } else {
                //all already paid ammo cubes and powerups are given back to the player and an exception is thrown
                grabAmmoCubes(alreadyPaidAmmoCubes);
                alreadyPaidPowerups.forEach(this::grabPowerup);
                throw new MissingOwnershipException("Player can't afford this weapon, missing powerups");
            }
        }
    }

    /**
     * This method supports a generic payment
     * @param coins the list of coins used for the payment
     */
    public void pay(List<Coin> coins) {
        //the private pay method is called, after dividing coins in ammo cubes and powerups
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

    /**
     * This method gets the current reward for damaging the player
     * @return the current Reward
     */
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
            //if the mode is not final frenzy, the action tile is determined by the adrenaline triggers
            if (this.damageTokens.size() >= constraints.getSecondAdrenalineTrigger()) {
                return ActionTileFactory.create(ActionTile.Type.ADRENALINE_2);
            } else if (this.damageTokens.size() >= constraints.getFirstAdrenalineTrigger()) {
                return ActionTileFactory.create(ActionTile.Type.ADRENALINE_1);
            } else {
                return ActionTileFactory.create(ActionTile.Type.STANDARD);
            }
        } else {
            //if the mode is final frenzy, the action tile is determined by the player's turn
            if (match.getPlayers().get(0) == this || match.getPlayersWhoDidFinalFrenzyTurn().contains(match.getPlayers().get(0))) {
                return ActionTileFactory.create(ActionTile.Type.FINAL_FRENZY_SINGLE_MODE);
            } else {
                return ActionTileFactory.create(ActionTile.Type.FINAL_FRENZY_DOUBLE_MODE);
            }
        }
    }

    /**
     * This method gets the life status of the player
     * @return true if player is currently alive, false if he is dead
     */
    public boolean isAlive() {
        return isAlive;
    }

    /**
     * This method allows for a player to be brought back to life
     */
    // Called by the Controller after it has determined (asking the human player) where it should spawn, based on a Powerup selection
    public void bringBackToLife() {
        isAlive = true;
        damageTokens.clear();
        if (match.getMode() == FINAL_FRENZY) { // when a player dies during final frenzy, he flips his board
            this.currentReward = RewardFactory.create(RewardFactory.Type.FINAL_FRENZY);
        }
        notifyPlayerBroughtBackToLife();
    }

    /**
     * This method notifies all PlayerRebornListeners
     */
    private void notifyPlayerBroughtBackToLife() {
        PlayerReborn e = new PlayerReborn(this);
        playerRebornListeners.forEach(l -> l.onPlayerReborn(e));
    }

    /**
     * This method notifies all PlayerOverkilledListeners
     * @param killer the killer that overkilled this player
     */
    private void notifyPlayerOverkilled(Player killer) {
        PlayerOverkilled e = new PlayerOverkilled(this, killer);
        playerOverkilledListeners.forEach(l -> l.onPlayerOverkilled(e));
    }

    /**
     * This method notifies all PlayerDiedListeners
     * @param killer the killer that killed this player
     */
    private void notifyPlayerDied(Player killer) {
        PlayerDied e = new PlayerDied(this, killer);
        this.playerDiedListeners.forEach(listener -> listener.onPlayerDied(e));
    }

    /**
     * This method notifies all PlayerDamagedListeners
     * @param attacker the attacker that attacked this player
     */
    private void notifyPlayerDamaged(Player attacker) {
        PlayerDamaged e = new PlayerDamaged(this, attacker);
        this.playerDamagedListeners.forEach(listener -> listener.onPlayerDamaged(e));
    }

    /**
     * This method gets the constraints given to the player
     * @return a PlayerConstraints object
     */
    public PlayerConstraints getConstraints() {
        return constraints;
    }

    /**
     * This method gets the listeners of PlayerDied event
     * @return a list of PlayerDiedListener
     */
    public List<PlayerDiedListener> getPlayerDiedListeners() {
        return this.playerDiedListeners;
    }

    /**
     * This method gets the listeners of PlayerOverkilled event
     * @return a list of PlayerOverkilledListener
     */
    public List<PlayerOverkilledListener> getPlayerOverkilledListeners() {
        return this.playerOverkilledListeners;
    }

    /**
     * This method gets the listeners of PlayerReborn event
     * @return a list of PlayerRebornListener
     */
    public List<PlayerRebornListener> getPlayerRebornListeners() {
        return this.playerRebornListeners;
    }

    public Match getMatch() {
        return match;
    }

    public Block getBlock() {
        return match.getBoard().findPlayer(this).orElseThrow(() -> new IllegalStateException("Player is not on the board"));
    }

    public List<Direction> getAvailableDirections(){
        return match.getBoard().getAvailableDirections(this.getBlock());
    }

    public boolean isOnASpawnpoint(){
        return match.getBoard().isOnASpawnpoint(this.getBlock());
    }
}
