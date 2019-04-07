package it.polimi.ingsw.server.model;

import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.collections.Deck;
import it.polimi.ingsw.server.model.currency.BonusTile;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.events.MatchEnded;
import it.polimi.ingsw.server.model.events.MatchModeChanged;
import it.polimi.ingsw.server.model.events.PlayerDied;
import it.polimi.ingsw.server.model.events.PlayerOverkilled;
import it.polimi.ingsw.server.model.events.listeners.MatchEndedListener;
import it.polimi.ingsw.server.model.events.listeners.MatchModeChangedListener;
import it.polimi.ingsw.server.model.events.listeners.PlayerDiedListener;
import it.polimi.ingsw.server.model.events.listeners.PlayerOverkilledListener;
import it.polimi.ingsw.server.model.factories.BonusTileFactory;
import it.polimi.ingsw.server.model.factories.PowerupTileFactory;
import it.polimi.ingsw.server.model.factories.WeaponFactory;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.weapons.Weapon;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents the match, which is the core of the model and contains all the information relative to the game status
 */
public class Match implements PlayerDiedListener, PlayerOverkilledListener {



    /**
     * This enum contains the possible types of match
     */
    public enum Mode {
        STANDARD,
        FINAL_FRENZY,
        SUDDEN_DEATH
    }


    /**
     * This property represents the number of skulls that are still left on the board
     */
    private int skulls;

    /**
     * This property represents the board of the match
     */
    private final Board board;

    /**
     * This property stores the players that are participating in the match
     */
    private List<Player> players;

    /**
     * This property stores the player that is currently playing
     */
    private Player activePlayer;

    /**
     * This property stores the killshots and whether they have overkill
     */
    private List<Killshot> killshots;

    /**
     * This property stores the deck of bonus tiles, which will never run out during the match
     */
    private Deck<BonusTile> bonusDeck;

    /**
     * This property stores the deck of weapons, which might run out during the match because all weapons are either on spawnpoints or owned by players
     */
    private Deck<Weapon> weaponDeck;

    /**
     * This property stores the deck of powerups, which will never run out during the match
     */
    private Deck<PowerupTile> powerupDeck;

    /**
     * This property stores the mode of the match, which can change if final frenzy is triggered
     */
    private Mode mode;

    private int finalFrenzyPlayed;
    private List<MatchEndedListener> matchEndedListeners;
    private List<MatchModeChangedListener> matchModeChangedListeners;
    /**
     * This constructor creates a new match from scratch
     * @param players the players who are joining this match
     * @param board the board that was chosen for the match
     * @param skulls an int representing the number of skulls
     * @param mode the initial match mode
     */
    public Match(List<Player> players, Board board, int skulls, Mode mode) {
        this.skulls = skulls;
        this.players = players;
        this.board = board;
        this.activePlayer = this.players.get(0);
        this.killshots = new LinkedList<>();
        this.bonusDeck = BonusTileFactory.createDeck();
        this.weaponDeck = WeaponFactory.createDeck();
        this.powerupDeck = PowerupTileFactory.createDeck();
        this.mode = mode;
        this.matchEndedListeners = new ArrayList<>();
        this.matchModeChangedListeners = new ArrayList<>();
        this.finalFrenzyPlayed = 0;
    }

    //TODO: add second constructor to restart a saved match given the name of the file

    /**
     * This method saves the state of the match on a file
     * @param filename a String with the name of the file
     */
    public void save(String filename) {
        //Advanced functionality to implement later
    }

    /**
     * This method gets the bonus tile deck
     * @return the Deck of BonusTile
     */
    public Deck<BonusTile> getBonusDeck() {
        return this.bonusDeck;
    }

    /**
     * This method gets the weapon deck
     * @return the Deck of Weapon
     */
    public Deck<Weapon> getWeaponDeck() {
        return this.weaponDeck;
    }

    /**
     * This method gets the powerup tile deck
     * @return the Deck of PowerupTile
     */
    public Deck<PowerupTile> getPowerupDeck() {
        return this.powerupDeck;
    }

    /**
     * This method gets the active player
     * @return the Player that is currently playing
     */
    public Player getActivePlayer() {
        return this.activePlayer;
    }

    /**
     * This method gets the killshots that were given and whether they have overkill
     * @return a Map of DamageToken associated with a Boolean, which is true if overkill is present
     */
    public List<Killshot> getKillshots() {
        return this.killshots;
    }

    /**
     * This method allows to add a new killshot token to the match
     * @param killshot the DamageToken representing the killshot
     * @param isOverkill true if overkill is present, false otherwise
     */
    public void addKillshot(DamageToken killshot, boolean isOverkill) {
        killshots.add(new Killshot(killshot, isOverkill));
    }

    /**
     * This method gets the current mode of the match
     * @return a Mode representing the current status of the match (final frenzy or standard)
     */
    public Mode getMode() {
        return this.mode;
    }

    /**
     * This method gets the skulls that are yet to be taken
     * @return an int representing the number of skulls left
     */
    public int getRemainingSkulls() {
        return this.skulls;
    }

    /**
     * This method gets the board used in the match
     * @return the Board used in the match
     */
    public Board getBoard() {
        return this.board;
    }

    /**
     * This method is used when the active player has finished its turn and a new active player needs to be set
     */
    public void changeTurn() {
        int activePlayerIndex = this.players.indexOf(this.activePlayer);
        this.activePlayer = (activePlayerIndex == this.players.size() - 1) ? this.players.get(0) : this.players.get(activePlayerIndex + 1);
    }

    /**
     * This method must be called by the Controller before "changeTurn" in order to score points, then it should bring the returned list of dead players back to life
     *
     * @return the deadPlayers list waiting for a user decision to respawn
     */
    public List<Player> endTurn() {
        List<Player> deadPlayers = this.getPlayers()
                .stream()
                .filter(player -> !player.isAlive())
                .collect(Collectors.toList());

        // In case of multiple killshots, the active player who dealt those attacks gets an extra point
        if (deadPlayers.size() > 1) {
            this.activePlayer.addPoints(1);
        }

        // Now we can bring back to life those players
        for (Player deadPlayer : deadPlayers) {
            scorePoints(deadPlayer);
            if (this.skulls > 0) {
                this.skulls--;
                deadPlayer.addSkull();
            }
        }

        if (this.skulls == 0 && this.mode == Mode.STANDARD) {
            this.mode = Mode.FINAL_FRENZY;
            notifyMatchModeChanged();
        } else if (this.mode == Mode.SUDDEN_DEATH) {
            notifyMatchEnded();
        }

        return deadPlayers;
    }

    /**
     * This method is called when a player dies
     *
     * @param event the event corresponding to the player's death
     */
    @Override
    public void onPlayerDied(PlayerDied event) {
        this.killshots.add(new Killshot(new DamageToken(event.getKiller()), false));
    }


    /**
     * This method is called when a dead player gets overkilled
     *
     * @param event the event corresponding to the player's overkill
     */
    @Override
    public void onPlayerOverkilled(PlayerOverkilled event) {
        // killshots.size() > 0 because an overkill implies a death and thus a killshot
        this.killshots.get(this.killshots.size() - 1).markAsOverkill();
        event.getKiller().addMarks(Collections.singletonList(new DamageToken(event.getVictim())));
    }



    /**
     * This method triggers the MatchModeChanged event and sends it to its listeners
     */
    private void notifyMatchModeChanged() {
        MatchModeChanged e = new MatchModeChanged(this, this.mode);
        this.matchModeChangedListeners.forEach(l -> l.onMatchModeChanged(e));
    }

    /**
     * This method triggers the MatchEnded event and sends it to its listeners
     */
    private void notifyMatchEnded() {
        MatchEnded e = new MatchEnded(this, this.players);
        this.matchEndedListeners.forEach(l -> l.onMatchEnded(e));
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    public void addMatchEndedListener(MatchEndedListener listener) {
        this.matchEndedListeners.add(listener);
    }

    public void addMatchModeChangedListener(MatchModeChangedListener listener) {
        this.matchModeChangedListeners.add(listener);
    }

    private void scorePoints(Player victim) {
        if (victim.firstBloodMatters()) {
            victim.getDamageTokens().get(0).getAttacker().addPoints(1);
        }
        List<Player> scoringPlayers = this.players.stream()
                .filter(player -> player != victim && victim.getDamageTokens().stream().anyMatch(damageToken -> damageToken.getAttacker() == player))
                .sorted((a, b) -> {

                    List<DamageToken> tokensA = new LinkedList<>();
                    List<DamageToken> tokensB = new LinkedList<>();
                    victim.getDamageTokens().forEach(token -> {
                        if (token.getAttacker() == a) {
                            tokensA.add(token);
                        } else if (token.getAttacker() == b) {
                            tokensB.add(token);
                        }
                    });

                    int sizeComparison = Integer.compare(tokensB.size(), tokensA.size());
                    int timeComparison = Integer.compare(victim.getDamageTokens().indexOf(tokensA.get(0)), victim.getDamageTokens().indexOf(tokensB.get(0)));

                    return sizeComparison != 0 ?
                            sizeComparison :
                            timeComparison;
                })
                .collect(Collectors.toList());

        int maxIndex = victim.getCurrentReward().length;
        int currentIndex = victim.getSkulls();
        for (Player scoringPlayer : scoringPlayers) {
            if (maxIndex > currentIndex) {
                scoringPlayer.addPoints(victim.getCurrentReward()[currentIndex]);
            } else {
                scoringPlayer.addPoints(1); // any scoring player gets at list one point
            }
            currentIndex++;
        }
    }


}
