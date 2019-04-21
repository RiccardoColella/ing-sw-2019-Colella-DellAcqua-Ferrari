package it.polimi.ingsw.shared.commands;

public enum ClientApi {

    DIRECTION_QUESTION("DirectionQ"),
    BLOCK_QUESTION("BlockQ");

    private String name;

    ClientApi(String name) {
        this.name = name;
    }

}

