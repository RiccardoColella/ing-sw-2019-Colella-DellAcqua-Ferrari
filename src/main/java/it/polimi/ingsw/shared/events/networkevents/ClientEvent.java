package it.polimi.ingsw.shared.events.networkevents;

/**
 * Network event carrying information about a client
 *
 * @author Carlo Dell'Acqua
 */
public class ClientEvent extends NetworkEvent {

    /**
     * The nickname of the client
     */
    private final String nickname;

    /**
     * Constructs a client event
     *
     * @param nickname the nickname of the client
     */
    public ClientEvent(String nickname) {
        this.nickname = nickname;
    }

    /**
     * @return the nickname of the client
     */
    public String getNickname() {
        return nickname;
    }
}
