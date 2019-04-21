package it.polimi.ingsw.shared.commands;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class Command {

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
}
