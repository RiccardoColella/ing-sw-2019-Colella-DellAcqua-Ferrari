package it.polimi.ingsw.server.model;

import it.polimi.ingsw.server.model.exceptions.UnknownEnumException;

import java.util.*;
import java.util.List;

/**
 * This class represents the match, which is the core of the model and contains all the information relative to the game status
 */
public class Match {

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
    private Map<DamageToken, Boolean> killshots;

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
    private MatchMode matchMode;

    /**
     * This constructor creates a new match from scratch
     * @param playersInfo the PlayerInfo storing basic info about the players
     * @param preset the BoardPreset that was chosen for the match
     * @param skulls an int representing the number of skulls
     */
    public Match(List<PlayerInfo> playersInfo, BoardPreset preset, int skulls) throws UnknownEnumException {
        this.skulls = skulls;
        this.players = new ArrayList<>();
        for (PlayerInfo info : playersInfo) {
            this.players.add(new Player(info));
        }
        this.board = new Board(preset);
        this.activePlayer = this.players.get(0);
        this.killshots = new HashMap<>();
        //CREATING THE DECK OF BONUS TILES:
        List<BonusTile> bonusCards = new LinkedList<>();
        // 36 bonus card, 18 with 3 ammos, 18 2 ammos + powerup
        // 2 ammos + powerup: 2 with 2 ammos of the same color for each color (= 6 cards), 4 for every combination (= 12 cards) RY RB BY
        // 3 ammos: 3 for each combo of 2 ammos of the same color + 1 different color (YBB, YRR, BYY, BRR, RYY, RBB) (= 18 cards)
        for (CoinColor mainColor : CoinColor.values()) {
            for (CoinColor secondColor : CoinColor.values()) {
                if (mainColor != secondColor) {
                    for (int i = 0; i < 2; i++) {
                        bonusCards.add(BonusTileFactory.create(mainColor, mainColor, secondColor));
                        bonusCards.add(BonusTileFactory.create(mainColor, secondColor));
                    }
                    bonusCards.add(BonusTileFactory.create(mainColor, mainColor, secondColor));
                }
            }
            for (int i = 0; i < 2; i++) {
                bonusCards.add(BonusTileFactory.create(mainColor, mainColor));
            }
        }
        this.bonusDeck = new Deck<>(true, bonusCards);

        //CREATING THE WEAPON DECK:
        List<Weapon> weaponCards = new LinkedList<>();
        for (WeaponName name : WeaponName.values()) {
            weaponCards.add(WeaponFactory.create(name));
        }
        this.weaponDeck = new Deck<>(false, weaponCards);

        //CREATING THE POWERUP DECK
        List<PowerupTile> powerupCards = new LinkedList<>();
        for (PowerupType type : PowerupType.values()) {
            for (CoinColor color : CoinColor.values()) {
                for (int i = 0; i < 2; i++) {
                    powerupCards.add(PowerupTileFactory.create(type, color));
                }
            }
        }
        this.powerupDeck = new Deck<>(true, powerupCards);
        this.matchMode = MatchMode.STANDARD;
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
    public Map<DamageToken, Boolean> getKillshots() {
        return this.killshots;
    }

    /**
     * This method allows to add a new killshot token to the match
     * @param killshot the DamageToken representing the killshot
     * @param hasOverkill true if overkill is present, false otherwise
     */
    public void addKillshot(DamageToken killshot, boolean hasOverkill) {
        killshots.put(killshot, hasOverkill);
    }

    /**
     * This method gets the current mode of the match
     * @return a MatchMode representing the current status of the match (final frenzy or standard)
     */
    public MatchMode getMatchMode() {
        return this.matchMode;
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
}
