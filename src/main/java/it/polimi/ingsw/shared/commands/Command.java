package it.polimi.ingsw.shared.commands;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

public class Command {

    private static final Gson gson = new Gson();

    private final String name;
    private final JsonElement payload;

    public Command(String name, Object payload) {
        this.name = name;
        this.payload = new Gson().toJsonTree(payload);
    }

    public String getName() {
        return name;
    }

    public JsonElement getPayload() {
        return payload;
    }

    public static Command fromJson(String json) {
        return gson.fromJson(json, new TypeToken<Command>(){}.getType());
    }

    public String toJson() {
        return gson.toJsonTree(this).toString();
    }
}
