package it.polimi.ingsw.server.view.remote;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.PlayerInfo;
import it.polimi.ingsw.server.view.View;
import it.polimi.ingsw.shared.view.remote.SocketMessageManager;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

/**
 * Socket based implementation of the server-side SocketView
 */
public class SocketView extends View implements AutoCloseable {

    private Socket socket;
    private SocketMessageManager socketMessageManager;

    public SocketView(Socket socket) throws IOException {
        this.socket = socket;
        socketMessageManager = new SocketMessageManager(socket, inputMessageQueue, outputMessageQueue);
    }

    @Override
    public PowerupTile chooseSpawnpoint(List<PowerupTile> powerups) {
        return null;
    }

    // TODO: Verify that the socket is automatically closed
    @Override
    public boolean isConnected() {
        return super.isConnected() && socket.isConnected();
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


    @Override
    public void close() throws Exception {
        socketMessageManager.close();
        socket.close();
    }
}
