package it.polimi.ingsw.client.io;

import it.polimi.ingsw.shared.commands.Command;
import it.polimi.ingsw.shared.events.listeners.CommandReceivedListener;
import it.polimi.ingsw.utils.io.UncheckedInputStream;
import it.polimi.ingsw.utils.io.UncheckedOutputStream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class SocketConnector extends Connector implements AutoCloseable {

    private final UncheckedOutputStream outputStream;
    private final UncheckedInputStream inputStream;
    private Socket socket;

    private final List<CommandReceivedListener> listeners = new LinkedList<>();

    public SocketConnector(InetSocketAddress address) throws IOException {

        socket = new Socket();
        socket.connect(address);

        outputStream = new UncheckedOutputStream(socket.getOutputStream());
        inputStream = new UncheckedInputStream(socket.getInputStream());

        startReceivingCommands();
    }


    @Override
    protected Command receiveCommand() {
        return Command.fromJson(
            new String(
                inputStream.readBytes(inputStream.readInt()),
                StandardCharsets.UTF_8
            )
        );
    }

    @Override
    public void sendCommand(Command command) {
        byte[] content = command.toJson().getBytes(StandardCharsets.UTF_8);
        outputStream.writeInt(content.length);
        outputStream.writeBytes(content);
    }

    @Override
    public void close() throws Exception {
        super.close();
        socket.close();
    }
}

