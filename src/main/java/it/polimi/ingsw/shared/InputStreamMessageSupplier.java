package it.polimi.ingsw.shared;

import it.polimi.ingsw.shared.messages.Message;
import it.polimi.ingsw.utils.function.IOSupplier;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * An input message supplier that waits for data to be available into a given input stream, parses it and then return a Message instance
 *
 * @author Carlo Dell'Acqua
 */
public class InputStreamMessageSupplier implements IOSupplier<Message>, AutoCloseable {
    /**
     * Logging utility
     */
    protected final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Input stream to read from
     */
    private final DataInputStream inputStream;

    /**
     * Thread pool that runs the background tasks
     */
    private final ExecutorService threadPool = Executors.newSingleThreadExecutor();

    /**
     * A future that will hold the result of the input stream reading and parsing
     */
    private Future<Message> messageReceiveTask;

    /**
     * Constructs a message supplier based on input stream
     *
     * @param inputStream the input stream to read data from
     */
    public InputStreamMessageSupplier(DataInputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Submit a task that will wait until there is a Message in the input stream
     *
     * @return a future object holding the promise of a Message
     */
    private Future<Message> submitMessageReceiveTask() {
        return threadPool.submit(() -> {
            byte[] buffer = new byte[inputStream.readInt()];
            inputStream.readFully(buffer);
            return Message.fromJson(
                    new String(
                            buffer,
                            StandardCharsets.UTF_8
                    )
            );
        });
    }

    /**
     * Return a message read from the input stream if available
     *
     * @param timeout the maximum time to wait for a message to be available
     * @param unit the measurement unit of the timeout
     * @return a Message read from the input stream
     * @throws IOException if the input stream cannot be read
     * @throws TimeoutException if no message is available within the specified timeout
     */
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

    /**
     * Stops the thread pool to prevent further Future from being submitted
     */
    private void stop() {
        synchronized (threadPool) {
            threadPool.shutdown();
        }
    }

    /**
     * Closes this object and shut down the associated thread pool
     *
     * @throws InterruptedException if the thread is forced to stop
     */
    @Override
    public void close() throws InterruptedException {
        stop();
        while (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
            logger.warning("Thread pool hasn't shut down yet, waiting...");
        }
    }
}