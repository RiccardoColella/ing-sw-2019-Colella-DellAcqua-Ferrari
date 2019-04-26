package it.polimi.ingsw.shared.view.remote.events.listeners;

import it.polimi.ingsw.shared.view.remote.events.MessageDispatcherStopped;

import java.util.EventListener;

public interface MessageDispatcherStoppedListener extends EventListener {
    void onMessageDispatcherStopped(MessageDispatcherStopped e);
}
