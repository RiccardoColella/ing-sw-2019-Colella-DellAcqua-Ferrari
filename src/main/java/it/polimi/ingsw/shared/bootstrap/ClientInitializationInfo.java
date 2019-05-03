package it.polimi.ingsw.shared.bootstrap;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.match.Match;

/**
 * Shared object that all clients should instantiate and send to the server as the first event message.
 * It contains the initial preference for the player
 *
 * @author Carlo Dell'Acqua
 */
public class ClientInitializationInfo {
    /**
     * Board preset
     */
    private BoardFactory.Preset preset;
    /**
     * Number of skulls
     */
    private int skulls;
    /**
     * Match mode
     */
    private Match.Mode mode;
    /**
     * Nickname
     */
    private String nickname;

    /**
     * Constructs an informational object containing the player's preferences
     *
     * @param nickname the chosen nickname
     * @param preset the chosen preset
     * @param skulls the chosen initial number of skulls
     * @param mode the chosen match mode
     */
    public ClientInitializationInfo(String nickname, BoardFactory.Preset preset, int skulls, Match.Mode mode) {
        this.nickname = nickname;
        this.preset = preset;
        this.skulls = skulls;
        this.mode = mode;
    }

    /**
     * @return the chosen preset
     */
    public BoardFactory.Preset getPreset() {
        return preset;
    }

    /**
     * @return the chosen number of skulls
     */
    public int getSkulls() {
        return skulls;
    }

    /**
     * @return the chosen match mode
     */
    public Match.Mode getMode() {
        return mode;
    }

    /**
     * @return the chosen nickname
     */
    public String getNickname() {
        return nickname;
    }
}
