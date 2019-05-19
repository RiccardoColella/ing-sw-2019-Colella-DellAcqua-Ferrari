package it.polimi.ingsw.server.view.events.listeners;

import it.polimi.ingsw.server.view.events.ViewEvent;

import java.util.EventListener;

public interface ViewListener extends EventListener {
    void onViewDisconnected(ViewEvent e);

    void onViewReady(ViewEvent e);
}
