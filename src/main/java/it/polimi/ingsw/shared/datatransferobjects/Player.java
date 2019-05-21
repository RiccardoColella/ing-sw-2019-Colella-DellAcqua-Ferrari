package it.polimi.ingsw.shared.datatransferobjects;

import it.polimi.ingsw.server.model.player.PlayerColor;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Player implements Serializable {

    private final String nickname;
    private final PlayerColor color;
    private final Wallet wallet;
    private final PlayerHealth health;
    private boolean isTileFlipped;
    private boolean isBoardFlipped;

    public Player(String nickname, PlayerColor color, Wallet wallet, PlayerHealth health, boolean isTileFlipped, boolean isBoardFlipped) {
        this.nickname = nickname;
        this.color = color;
        this.wallet = wallet;
        this.health = health;
        this.isTileFlipped = isTileFlipped;
        this.isBoardFlipped = isBoardFlipped;
    }

    public String getNickname() {
        return nickname;
    }

    public PlayerColor getColor() {
        return color;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public int getSkulls() {
        return health.getSkulls();
    }

    public List<PlayerColor> getDamage() {
        return health.getDamages();
    }

    public List<PlayerColor> getMarks() {
        return health.getMarks();
    }

    public PlayerHealth getHealth() {
        return health;
    }

    public boolean isTileFlipped() {
        return isTileFlipped;
    }

    public boolean isBoardFlipped() {
        return isBoardFlipped;
    }

    @Override
    public int hashCode() {
        return nickname.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Player)) {
            return false;
        } else {
            return this.nickname.equals(((Player) other).nickname);
        }
    }
}
