package it.polimi.ingsw.shared.events.networkevents;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.util.EventObject;

public abstract class NetworkEvent extends EventObject {

    public static Gson gson = new Gson();

    public NetworkEvent() {
        super(new Object());
    }

    public static <T extends NetworkEvent> T fromJson(JsonElement jsonElement, Object eventSource) {

        T event = gson.fromJson(jsonElement, new TypeToken<T>(){}.getType());
        event.setSource(eventSource);

        return event;
    }

    protected void setSource(Object source) {
        this.source = source;
    }
}
