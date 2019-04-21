package it.polimi.ingsw.server.view.remote;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.PlayerInfo;
import it.polimi.ingsw.server.view.View;
import it.polimi.ingsw.shared.commands.Command;

import java.net.Socket;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RMISocketFactory;
import java.util.List;

/**
 * RMIConnector based implementation of the server-side SocketView
 */
public class RMIView extends View {

    public RMIView() { }

    @Override
    public PowerupTile chooseSpawnpoint(List<PowerupTile> powerups) {
        return null;
    }

    @Override
    public PlayerInfo getPlayerInfo() {
        return null;
    }

    @Override
    public BoardFactory.Preset getChosenPreset() {
        return null;
    }

    @Override
    public int getChosenSkulls() {
        return 0;
    }

    @Override
    public Match.Mode getChosenMode() {
        return null;
    }
}
