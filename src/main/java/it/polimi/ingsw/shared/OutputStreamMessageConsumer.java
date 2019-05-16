package it.polimi.ingsw.shared;

import it.polimi.ingsw.shared.messages.Message;
import it.polimi.ingsw.utils.function.IOConsumer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Message consumer based on output streams. Messages will be written in a portable format into the given output stream
 *
 * @author Carlo Dell'Acqua
 */
public class OutputStreamMessageConsumer implements IOConsumer<Message> {
    /**
     * The output stream to write messages to
     */
    private final DataOutputStream outputStream;

    /**
     * Constructs a message consumer which uses a data output stream to write messages in a portable format
     *
     * @param outputStream the stream to write data to
     */
    public OutputStreamMessageConsumer(DataOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * Accepts a message and write it out to the output stream
     *
     * @param message the message to consume
     * @throws IOException if an error occurs while sending data to the stream
     */
    @Override
    public void accept(Message message) throws IOException {
        // Fixed charset shared with the client configuration to prevent incompatibility that
        // can be caused by different defaults
        byte[] content = message.toJson().getBytes(StandardCharsets.UTF_8);
        outputStream.writeInt(content.length);
        outputStream.write(content);
        outputStream.flush();
    }
}