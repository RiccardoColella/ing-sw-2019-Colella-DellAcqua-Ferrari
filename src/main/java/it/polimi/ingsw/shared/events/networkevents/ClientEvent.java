package it.polimi.ingsw.shared.events.networkevents;

public class ClientEvent extends NetworkEvent {


    private final String nickname;

    public ClientEvent(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }
}
