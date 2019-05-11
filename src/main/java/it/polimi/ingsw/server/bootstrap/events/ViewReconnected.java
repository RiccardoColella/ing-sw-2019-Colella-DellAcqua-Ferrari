package it.polimi.ingsw.server.bootstrap.events;

import it.polimi.ingsw.server.view.View;
import it.polimi.ingsw.utils.events.ConsumableEventObject;

/**
 * This event is meant to be fired when a View reconnects. This situation can be identified, for example, by comparing
 * the nickname provided by the connected view which is a unique identifier
 *
 * @author Carlo Dell'Acqua
 */
public class ViewReconnected extends ConsumableEventObject {

    /**
     * The reconnected view
     */
    private final transient View view;

    /**
     * Constructs the event
     *
     * @param source the source object that creates the event
     * @param view the reconnected view
     */
    public ViewReconnected(Object source, View view) {
        super(source);
        this.view = view;
    }

    /**
     * @return the reconnected view
     */
    public View getView() {
        return view;
    }
}
