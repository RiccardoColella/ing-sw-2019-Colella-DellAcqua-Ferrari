package it.polimi.ingsw.shared.events.listeners;

import it.polimi.ingsw.shared.events.MessageReceived;

import java.util.EventListener;

public interface EventMessageReceivedListener extends EventListener {
    void onEventMessageReceived(MessageReceived e);
}
