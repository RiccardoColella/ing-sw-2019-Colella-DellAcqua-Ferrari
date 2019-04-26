package it.polimi.ingsw.shared.view.remote;

import it.polimi.ingsw.shared.messages.Message;
import it.polimi.ingsw.utils.function.TimeoutSupplier;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class InputStreamMessageSupplier implements TimeoutSupplier<Message>, AutoCloseable {

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final DataInputStream inputStream;
    private final ExecutorService threadPool = Executors.newSingleThreadExecutor();
    private Future<Message> messageReceiveTask;

    public InputStreamMessageSupplier(DataInputStream inputStream) {
        this.inputStream = inputStream;
    }

    private Future<Message> submitMessageReceiveTask() {
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
        });
    }

    @Override
    public Message get(int timeout, TimeUnit unit) throws IOException, TimeoutException {
        try {
            if (messageReceiveTask == null) {
                synchronized (threadPool) {
                    if (!threadPool.isShutdown()) {
                        messageReceiveTask = submitMessageReceiveTask();
                    }
                }
            }
            Message message = messageReceiveTask.get(timeout, unit);
            synchronized (threadPool) {
                if (!threadPool.isShutdown()) {
                    messageReceiveTask = submitMessageReceiveTask();
                }
            }
            return message;
        } catch (InterruptedException e) {
            stop();
            Thread.currentThread().interrupt();
            throw new IOException("Unable to communicate, thread interrupted " + e, e);
        } catch (ExecutionException e) {
            stop();
            throw new IOException("Unable to receive data " + e);
        }
    }

    private void stop() {
        synchronized (threadPool) {
            threadPool.shutdown();
        }
    }

    @Override
    public void close() throws Exception {
        stop();
        while (!threadPool.awaitTermination(1, TimeUnit.SECONDS)) {
            logger.warning("Thread pool hasn't shut down yet, waiting...");
        }
    }
}