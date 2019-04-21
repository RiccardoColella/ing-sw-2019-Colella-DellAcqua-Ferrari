package it.polimi.ingsw.shared.commands;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.shared.Direction;


import java.awt.*;
import java.lang.reflect.Type;
import java.util.Collection;

public class CommandFactory {


    private static Gson gson = new Gson();

    public static Command fromJson(String json) {
        return gson.fromJson(json, new TypeToken<Command>(){}.getType());
    }
}
