package it.polimi.ingsw.server.view.remote;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.server.controller.exceptions.ViewDisconnectedException;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.PlayerInfo;
import it.polimi.ingsw.server.view.View;
import it.polimi.ingsw.shared.commands.Command;
import it.polimi.ingsw.shared.commands.CommandFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Socket based implementation of the server-side SocketView
 */
public class SocketView extends View implements AutoCloseable {

    private class UncheckedOutputStream {

        private DataOutputStream dataStream;

        private UncheckedOutputStream(OutputStream stream) {
            dataStream = new DataOutputStream(stream);
        }

        private void writeInt(int value) {
            try {
                dataStream.writeInt(value);
            } catch (IOException e) {
                throw new ViewDisconnectedException("Unable to send data");
            }
        }

        private void writeBytes(byte[] bytes) {
            try {
                dataStream.write(bytes);
            } catch (IOException e) {
                throw new ViewDisconnectedException("Unable to send data");
            }
        }
    }
    private class UncheckedInputStream {

        private DataInputStream dataStream;

        private UncheckedInputStream(InputStream stream) {
            dataStream = new DataInputStream(stream);
        }

        private int readInt() {
            try {
                return dataStream.readInt();
            } catch (IOException e) {
                throw new ViewDisconnectedException("Unable to send data");
            }
        }

        private byte[] readBytes(int length) {
            try {
                byte[] received = new byte[length];
                dataStream.read(received);
                return received;
            } catch (IOException e) {
                throw new ViewDisconnectedException("Unable to send data");
            }
        }
    }

    private Socket socket;
    private ExecutorService threadPool = Executors.newSingleThreadExecutor();
    private UncheckedOutputStream outputStream;
    private UncheckedInputStream inputStream;
    private Gson gson = new Gson();

    public SocketView(Socket socket) throws IOException {
        this.socket = socket;

        outputStream = new UncheckedOutputStream(socket.getOutputStream());
        inputStream = new UncheckedInputStream(socket.getInputStream());
        threadPool.execute(this::receiveCommandAsync);
    }

    protected void receiveCommandAsync() {

        enqueueInputCommand(
            CommandFactory.fromJson(
                new String(
                    inputStream.readBytes(inputStream.readInt()),
                    StandardCharsets.UTF_8
                )
            )
        );

        if (!threadPool.isShutdown()) {
            threadPool.execute(this::receiveCommandAsync);
        }
    }

    @Override
    protected void enqueueOutputCommand(Command command) {
        // Fixed charset shared with the client configuration to prevent incompatibility that
        // can be caused by different defaults
        byte[] content = gson.toJson(command).getBytes(StandardCharsets.UTF_8);
        outputStream.writeInt(content.length);
        outputStream.writeBytes(content);
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
        threadPool.shutdown();
        while (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
        }
        socket.close();
    }
}
