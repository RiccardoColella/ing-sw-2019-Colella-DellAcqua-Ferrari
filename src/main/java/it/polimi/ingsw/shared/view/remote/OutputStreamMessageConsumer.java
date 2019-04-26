package it.polimi.ingsw.shared.view.remote;

import com.google.gson.Gson;
import it.polimi.ingsw.shared.messages.Message;
import it.polimi.ingsw.utils.function.UnsafeConsumer;
import it.polimi.ingsw.utils.function.exceptions.UnsafeConsumerException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class OutputStreamMessageConsumer implements UnsafeConsumer<Message> {

    private final Gson gson = new Gson();
    private final DataOutputStream outputStream;

    public OutputStreamMessageConsumer(DataOutputStream outputStream) {
        this.outputStream = outputStream;
    }


    @Override
    public void accept(Message message) {
        try {
            // Fixed charset shared with the client configuration to prevent incompatibility that
            // can be caused by different defaults
            byte[] content = gson.toJson(message).getBytes(StandardCharsets.UTF_8);
            outputStream.writeInt(content.length);
            outputStream.write(content);
        } catch (IOException e) {
            throw new UnsafeConsumerException("Unable to write data to the output stream " + e);
        }
    }
}