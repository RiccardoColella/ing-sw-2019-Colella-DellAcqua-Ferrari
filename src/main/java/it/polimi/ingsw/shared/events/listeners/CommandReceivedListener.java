package it.polimi.ingsw.shared.events.listeners;

import it.polimi.ingsw.shared.events.CommandReceived;

import java.util.EventListener;

public interface CommandReceivedListener extends EventListener {
    void onCommandReceived(CommandReceived e);
}
