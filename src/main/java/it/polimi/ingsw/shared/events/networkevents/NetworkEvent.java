package it.polimi.ingsw.shared.events.networkevents;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.EventObject;

/**
 * Generic network event for data transfer between the server and the clients
 *
 * @author Carlo Dell'Acqua
 */
public abstract class NetworkEvent extends EventObject {


    /**
     * JSON utility
     */
    public static Gson gson = new Gson();

    /**
     * Constructs a network event
     */
    public NetworkEvent() {
        super(new Object());
    }

    /**
     * Deserialize a network event from a JsonElement, returning an EventObject with the desired source object
     * @param jsonElement the jsonElement to deserialize
     * @param eventSource the event source of the returned event
     * @param eventClass the event class to use to deserialize the jsonElement
     * @param <T> the type of event that is being deserialized
     * @return an EventObject with the desired source object
     */
    public static <T extends NetworkEvent> T fromJson(JsonElement jsonElement, Object eventSource, Class<T> eventClass) {

        T event = gson.fromJson(jsonElement, eventClass);
        event.setSource(eventSource);

        return event;
    }

    /**
     * Changes the source of this event
     *
     * @param source the new source object
     */
    protected void setSource(Object source) {
        this.source = source;
    }
}
