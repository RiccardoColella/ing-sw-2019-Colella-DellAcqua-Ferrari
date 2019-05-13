package it.polimi.ingsw.shared.viewmodels;

import it.polimi.ingsw.server.model.player.PlayerColor;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class Player {

    private final String nickname;
    private final PlayerColor color;
    private final Wallet wallet;
    private int skulls;
    private List<PlayerColor> damage;
    private List<PlayerColor> marks;
    private Point location;
    private boolean boardFlipped;
    private boolean tileFlipped;

    public Player(String nickname, PlayerColor color, Wallet wallet) {
        this.color = color;
        this.nickname = nickname;
        this.wallet = wallet;
        this.skulls = 0;
        this.damage = new LinkedList<>();
        this.marks = new LinkedList<>();
        location = null;
        boardFlipped = false;
        tileFlipped = false;
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
        return skulls;
    }

    public List<PlayerColor> getDamage() {
        return damage;
    }

    public List<PlayerColor> getMarks() {
        return marks;
    }

    public void setDamage(List<PlayerColor> damage) {
        this.damage = damage;
    }

    public void setMarks(List<PlayerColor> marks) {
        this.marks = marks;
    }

    public void setSkulls(int skulls) {
        this.skulls = skulls;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public boolean isBoardFlipped() {
        return boardFlipped;
    }

    public void setBoardFlipped(boolean boardFlipped) {
        this.boardFlipped = boardFlipped;
    }

    public boolean isTileFlipped() {
        return tileFlipped;
    }

    public void setTileFlipped(boolean tileFlipped) {
        this.tileFlipped = tileFlipped;
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
            return other.hashCode() == this.hashCode();
        }
    }
}
