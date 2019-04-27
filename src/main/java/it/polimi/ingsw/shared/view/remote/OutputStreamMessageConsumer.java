package it.polimi.ingsw.shared.view.remote;

import com.google.gson.Gson;
import it.polimi.ingsw.shared.messages.Message;
import it.polimi.ingsw.utils.function.TimeoutConsumer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class OutputStreamMessageConsumer implements TimeoutConsumer<Message> {
    /**
     * JSON conversion utility
     */
    private final Gson gson = new Gson();
    private final DataOutputStream outputStream;

    public OutputStreamMessageConsumer(DataOutputStream outputStream) {
        this.outputStream = outputStream;
    }


    @Override
    public void accept(Message message) throws IOException {
        // Fixed charset shared with the client configuration to prevent incompatibility that
        // can be caused by different defaults
        byte[] content = message.toJson().getBytes(StandardCharsets.UTF_8);
        outputStream.writeInt(content.length);
        outputStream.write(content);
    }
}