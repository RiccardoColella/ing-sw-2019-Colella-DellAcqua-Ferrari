package it.polimi.ingsw.shared.viewmodels;

public class Token {

    private final Player attacker;

    public Token(Player attacker) {
        this.attacker = attacker;
    }

    public Player getAttacker() {
        return attacker;
    }
}
