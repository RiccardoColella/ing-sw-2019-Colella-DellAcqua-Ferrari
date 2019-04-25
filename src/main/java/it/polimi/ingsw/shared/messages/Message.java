package it.polimi.ingsw.shared.messages;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable {

    public enum Type {
        QUESTION,
        ANSWER,
        EVENT
    }

    private static transient final Gson gson = new Gson();

    private final String name;
    private final String payload;
    private final String streamId;
    private final Type type;

    protected Message(String name, Object payload, String streamId, Type type) {
        this.name = name;
        this.payload = new Gson().toJsonTree(payload).toString();
        this.streamId = streamId;
        this.type = type;
    }

    public static Message createQuestion(String name, Object payload) {
        return new Message(name, payload, UUID.randomUUID().toString(), Type.QUESTION);
    }
    public static Message createAnswer(String name, Object payload, String streamId) {
        return new Message(name, payload, streamId, Type.ANSWER);
    }
    public static Message createEvent(String name, Object payload) {
        return new Message(name, payload, "event", Type.EVENT);
    }

    public String getName() {
        return name;
    }

    public JsonElement getPayload() {
        return gson.fromJson(payload, new TypeToken<JsonElement>(){}.getType());
    }

    public static Message fromJson(String json) {
        return gson.fromJson(json, new TypeToken<Message>(){}.getType());
    }

    public String toJson() {
        return gson.toJsonTree(this).toString();
    }

    public String getStreamId() {
        return streamId;
    }

    public Type getType() {
        return type;
    }
}
