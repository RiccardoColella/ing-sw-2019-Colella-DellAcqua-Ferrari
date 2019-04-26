package it.polimi.ingsw.shared.view.remote;

import it.polimi.ingsw.shared.messages.Message;
import it.polimi.ingsw.utils.function.UnsafeSupplier;
import it.polimi.ingsw.utils.function.exceptions.UnsafeSupplierException;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class InputStreamMessageSupplier implements UnsafeSupplier<Message> {

    private final DataInputStream inputStream;

    public InputStreamMessageSupplier(DataInputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public Message get() {
        try {
            byte[] buffer = new byte[inputStream.readInt()];
            if (buffer.length != inputStream.read(buffer)) {
                throw new IOException("Expected and actual buffer data sizes differ");
            }
            return Message.fromJson(
                    new String(
                            buffer,
                            StandardCharsets.UTF_8
                    )
            );
        } catch (IOException e) {
            throw new UnsafeSupplierException("Unable to read data from the input stream " + e);
        }
    }
}