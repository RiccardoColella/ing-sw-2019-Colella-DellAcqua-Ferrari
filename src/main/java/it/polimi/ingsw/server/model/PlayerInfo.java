package it.polimi.ingsw.server.model;

public class PlayerInfo {

    /**
     * This property contains the nickname chosen by the player
     */
    private String nickname;

    /**
     * This property contains the color of the player's pawn
     */
    private PlayerColor color;

    /**
     * This constructor creates a player info object given the nickname and the color
     * @param nickname the player's nickname
     * @param color the color of the player's pawn
     */
    public PlayerInfo(String nickname, PlayerColor color) {
        this.nickname = nickname;
        this.color = color;
    }

    /**
     * This method gets the player's nickname
     * @return a string corresponding to the player's nickname
     */
    public String getNickname() {
        return this.nickname;
    }

    /**
     * This method gets the color of the player's pawn
     * @return the value of PlayerColor corresponding to the player's pawn
     */
    public PlayerColor getColor() {
        return this.color;
    }

}
