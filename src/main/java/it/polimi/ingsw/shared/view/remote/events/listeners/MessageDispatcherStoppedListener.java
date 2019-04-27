package it.polimi.ingsw.shared.view.remote.events.listeners;

import it.polimi.ingsw.shared.view.remote.events.MessageDispatcherStopped;

import java.util.EventListener;

/**
 * Event listener for the MessageDispatcherStopped event
 *
 * @author Carlo Dell'Acqua
 */
public interface MessageDispatcherStoppedListener extends EventListener {
    void onMessageDispatcherStopped(MessageDispatcherStopped e);
}
