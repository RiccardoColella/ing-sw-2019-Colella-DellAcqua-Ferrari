package it.polimi.ingsw.server.view.events;

import it.polimi.ingsw.server.view.View;

import java.util.EventObject;

/**
 * Event triggered by a view
 */
public class ViewEvent extends EventObject {

    /**
     * Constructs a view event
     *
     * @param source the view which caused the event
     */
    public ViewEvent(View source) {
        super(source);
    }

    /**
     * @return the view which caused the event
     */
    public View getView() {
        return (View)source;
    }
}
