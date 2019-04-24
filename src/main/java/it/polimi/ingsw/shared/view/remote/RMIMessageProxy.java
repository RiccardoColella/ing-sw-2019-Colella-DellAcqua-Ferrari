package it.polimi.ingsw.shared.view.remote;

import it.polimi.ingsw.shared.messages.Message;

public interface RMIMessageProxy {
    Message receiveMessage() throws InterruptedException;

    void sendMessage(Message message);
}
