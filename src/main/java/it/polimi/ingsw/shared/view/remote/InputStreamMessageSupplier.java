package it.polimi.ingsw.shared.view.remote;

import it.polimi.ingsw.shared.messages.Message;
import it.polimi.ingsw.utils.function.TimeoutSupplier;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class InputStreamMessageSupplier implements TimeoutSupplier<Message> {

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final DataInputStream inputStream;
    private final ExecutorService threadPool = Executors.newSingleThreadExecutor();

    public InputStreamMessageSupplier(DataInputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public Message get(int timeout, TimeUnit unit) throws IOException, TimeoutException {
        try {
            return threadPool.submit(() -> {
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
            }).get(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warning("Thread interrupted " + e);
            throw new IOException("Unable to communicate, thread interrupted " + e, e);
        } catch (ExecutionException e) {
            throw new IOException("Unable to receive data " + e);
        }
    }
}