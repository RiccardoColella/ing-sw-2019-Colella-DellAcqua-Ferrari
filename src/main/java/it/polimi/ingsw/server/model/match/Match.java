package it.polimi.ingsw.server.model.match;

import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.collections.Deck;
import it.polimi.ingsw.server.model.currency.BonusTile;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.events.MatchEnded;
import it.polimi.ingsw.server.model.events.MatchModeChanged;
import it.polimi.ingsw.server.model.events.PlayerDied;
import it.polimi.ingsw.server.model.events.PlayerOverkilled;
import it.polimi.ingsw.server.model.events.listeners.MatchEndedListener;
import it.polimi.ingsw.server.model.events.listeners.MatchModeChangedListener;
import it.polimi.ingsw.server.model.events.listeners.PlayerDiedListener;
import it.polimi.ingsw.server.model.events.listeners.PlayerOverkilledListener;
import it.polimi.ingsw.server.model.currency.BonusTileFactory;
import it.polimi.ingsw.server.model.currency.PowerupTileFactory;
import it.polimi.ingsw.server.model.player.DamageToken;
import it.polimi.ingsw.server.model.player.PlayerFactory;
import it.polimi.ingsw.server.model.rewards.RewardFactory;
import it.polimi.ingsw.server.model.weapons.WeaponFactory;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.player.PlayerInfo;
import it.polimi.ingsw.server.model.rewards.Reward;
import it.polimi.ingsw.server.model.weapons.Weapon;

import java.util.*;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
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
    private final List<Player> players;

    /**
     * This property stores the player that is currently playing
     */
    private Player activePlayer;

    /**
     * This property stores the killshots and whether they have overkill
     */
    private final List<Killshot> killshots;

    /**
     * This property stores the deck of bonus tiles, which will never run out during the match
     */
    private final Deck<BonusTile> bonusDeck;

    /**
     * This property stores the deck of weapons, which might run out during the match because all weapons are either on spawnpoints or owned by players
     */
    private final Deck<Weapon> weaponDeck;

    /**
     * This property stores the deck of powerups, which will never run out during the match
     */
    private final Deck<PowerupTile> powerupDeck;

    /**
     * This property stores the mode of the match, which can change if final frenzy is triggered
     */
    private Mode mode;

    private List<Player> playersWhoDidFinalFrenzyTurn;
    private List<MatchEndedListener> matchEndedListeners;
    private List<MatchModeChangedListener> matchModeChangedListeners;

    /**
     * This constructor creates a new match from scratch
     *
     * @param playerInfoList the playerInfoList containing the information to create the players
     * @param board the board that was chosen for the match
     * @param skulls an int representing the number of skulls
     * @param mode the initial match mode
     * @param playerSupplier a bi-function which provides a Player instance given this match and a PlayerInfo object
     */
    public Match(List<PlayerInfo> playerInfoList, Board board, int skulls, Mode mode, BiFunction<Match, PlayerInfo, Player> playerSupplier) {
        this.skulls = skulls;
        this.players = Collections.unmodifiableList(playerInfoList.stream().map(info -> playerSupplier.apply(this, info)).collect(Collectors.toList()));
        this.board = board;
        this.activePlayer = this.players.get(0);
        this.killshots = new LinkedList<>();
        this.bonusDeck = BonusTileFactory.createDeck();
        this.weaponDeck = WeaponFactory.createDeck();
        this.powerupDeck = PowerupTileFactory.createDeck();
        this.mode = mode;
        this.matchEndedListeners = new ArrayList<>();
        this.matchModeChangedListeners = new ArrayList<>();
        this.playersWhoDidFinalFrenzyTurn = new LinkedList<>();
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
            this.activePlayer.addPoints(RewardFactory.create(RewardFactory.Type.DOUBLE_KILL).getRewardFor(0));
        }

        // Now we can bring back to life those players
        for (Player deadPlayer : deadPlayers) {
            scoreVictimPoints(deadPlayer);
            if (this.skulls > 0) {
                this.skulls--;
                deadPlayer.addSkull();
            }
        }

        if (this.skulls == 0 && this.mode == Mode.STANDARD) {
            this.mode = Mode.FINAL_FRENZY;
            notifyMatchModeChanged();
        } else if (this.skulls == 0 && this.mode == Mode.SUDDEN_DEATH) {
            notifyMatchEnded();
        } else if (this.mode == Mode.FINAL_FRENZY) {
            playersWhoDidFinalFrenzyTurn.add(activePlayer);
            if (playersWhoDidFinalFrenzyTurn.size() == players.size()) {
                notifyMatchEnded();
            }
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

    private int getKillshotCount(Player player) {
        int score = 0;
        for (Killshot killshot : this.killshots) {
            if (killshot.getDamageToken().getAttacker() == player) {
                if (killshot.isOverkill()) {
                    score++;
                }
                score++;
            }
        }
        return score;
    }

    /**
     * This method triggers the MatchEnded event and sends it to its listeners
     */
    private void notifyMatchEnded() {
        //Scoring player boards that still have damage
        List<Player> playersWithDamage = players.stream().filter(p -> !p.getDamageTokens().isEmpty() && p.isAlive()).collect(Collectors.toList());
        playersWithDamage.forEach(this::scoreVictimPoints);

        scoreKillshots();
        List<Player> rankings = this.players.stream().sorted((a, b) -> {
            int absoluteScore = b.getPoints() - a.getPoints(); // if a has more points than b, a is ahead of b (and vice-versa)
            int killshotScore = this.getKillshotCount(b) - getKillshotCount(a);
            int firstComparator = 0;
            for (Killshot killshot : killshots) {
                if (killshot.getDamageToken().getAttacker() == a) {
                    firstComparator = -1;
                    break;
                } else if (killshot.getDamageToken().getAttacker() == b) {
                    firstComparator = 1;
                }
            }
            int tieBreaker = killshotScore != 0 ? killshotScore : firstComparator;
            return absoluteScore != 0 ? absoluteScore : tieBreaker;
        }).collect(Collectors.toList());
        MatchEnded e = new MatchEnded(this, rankings);
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

    private void scoreVictimPoints(Player victim) {
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
        int currentIndex = victim.getSkulls();
        for (Player scoringPlayer : scoringPlayers) {
            scoringPlayer.addPoints(victim.getCurrentReward().getRewardFor(currentIndex, scoringPlayer == victim.getDamageTokens().get(0).getAttacker()));
            currentIndex++;
        }
    }

    private void scoreKillshots() {
        Reward rewards = RewardFactory.create(RewardFactory.Type.KILLSHOT);
        List<Player> scoringPlayers = this.players.stream()
                .filter(player -> killshots.stream().map(k -> k.getDamageToken().getAttacker()).anyMatch(killer -> killer == player))
                .sorted((a, b) -> {
                    int amountComparator = getKillshotCount(b) - getKillshotCount(a);
                    int firstComparator = 0;
                    for (Killshot killshot : killshots) {
                        if (killshot.getDamageToken().getAttacker() == a) {
                            firstComparator = -1;
                        } else if (killshot.getDamageToken().getAttacker() == b) {
                            firstComparator = 1;
                        }
                        if (firstComparator != 0) {
                            break;
                        }
                }
            return amountComparator != 0 ? amountComparator : firstComparator;
        }).collect(Collectors.toList());
        for (Player player : scoringPlayers) {
            player.addPoints(rewards.getRewardFor(scoringPlayers.indexOf(player)));
        }
    }

    public List<Player> getPlayersWhoDidFinalFrenzyTurn() {
        return playersWhoDidFinalFrenzyTurn;
    }
}