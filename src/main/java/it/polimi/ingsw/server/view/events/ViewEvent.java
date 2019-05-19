package it.polimi.ingsw.server.view.events;

import it.polimi.ingsw.server.view.View;

import java.util.EventObject;

public class ViewEvent extends EventObject {

    public ViewEvent(View source) {
        super(source);
    }

    public View getView() {
        return (View)source;
    }
}
