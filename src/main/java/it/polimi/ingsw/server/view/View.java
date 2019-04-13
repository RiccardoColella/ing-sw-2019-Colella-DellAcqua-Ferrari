package it.polimi.ingsw.server.view;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.PlayerInfo;

import java.util.List;

/**
 * This class is an abstract server-side View. It contains all the methods needed for the interaction with the controller
 * and implements all the listeners needed to receive information from the model
 */
public abstract class View {

    public abstract PowerupTile chooseSpawnpoint(List<PowerupTile> powerups);

    public abstract boolean isConnected();

    public abstract PlayerInfo getPlayerInfo();

    public abstract BoardFactory.Preset getChosenPreset();

    public abstract int getChosenSkulls();

    public abstract Match.Mode getChosenMode();
}
