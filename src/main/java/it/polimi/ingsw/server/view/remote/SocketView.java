package it.polimi.ingsw.server.view.remote;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.PlayerInfo;
import it.polimi.ingsw.server.view.View;
import it.polimi.ingsw.shared.view.remote.InputStreamMessageSupplier;
import it.polimi.ingsw.shared.view.remote.MessageDispatcher;
import it.polimi.ingsw.shared.view.remote.OutputStreamMessageConsumer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Socket based implementation of the server-side View
 */
public class SocketView extends View implements AutoCloseable {


    private Socket socket;
    private MessageDispatcher messageDispatcher;

    public SocketView(Socket socket, int answerTimeout, TimeUnit answerTimeoutUnit) throws IOException {
        super(answerTimeout, answerTimeoutUnit);

        this.socket = socket;
        messageDispatcher = new MessageDispatcher(
                inputMessageQueue,
                outputMessageQueue,
                new InputStreamMessageSupplier(new DataInputStream(socket.getInputStream())),
                new OutputStreamMessageConsumer(new DataOutputStream(socket.getOutputStream()))
        );
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
        messageDispatcher.close();
        socket.close();
    }
}
