package it.polimi.ingsw.shared;

import it.polimi.ingsw.client.io.listeners.MessageDispatcherStoppedListener;
import it.polimi.ingsw.shared.events.MessageDispatcherStopped;
import it.polimi.ingsw.shared.messages.Message;
import it.polimi.ingsw.utils.function.IOConsumer;
import it.polimi.ingsw.utils.function.IOSupplier;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Logger;


/**
 * This class asynchronously dispatches the messages from the input and output queues provided a supplier and a consumer for those messages
 *
 * @author Carlo Dell'Acqua
 */
public class MessageDispatcher implements AutoCloseable {
    /**
     * Timeout needed to prevent deadlocks
     */
    private static final int TAKE_TIMEOUT_MILLISECONDS = 1000;

    /**
     * Logging utility
     */
    protected final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * The supplier of input messages
     */
    private final IOSupplier<Message> inputMessageSupplier;
    /**
     * The consumer of output messages
     */
    private final IOConsumer<Message> outputMessageConsumer;

    /**
     * The input queue
     */
    private InputMessageQueue inputMessageQueue;
    /**
     * The output queue
     */
    private BlockingQueue<Message> outputMessageQueue;
    /**
     * The thread pools that executes the IO tasks in background
     */
    private final ExecutorService receiveThreadPool = Executors.newSingleThreadExecutor();
    private final ExecutorService sendThreadPool = Executors.newSingleThreadExecutor();

    /**
     * A list of listeners for the event MessageDispatcherStopped
     */
    private Set<MessageDispatcherStoppedListener> stoppedListeners = new HashSet<>();

    /**
     * Constructs a MessageDispatcher class that manages the passed queues
     *
     * @param inputMessageQueue the input message queue that will be filled by messages received by the supplier
     * @param outputMessageQueue the output message queue that will be emptied by the output message consumer
     * @param inputMessageSupplier the object that will provide messages
     * @param outputMessageConsumer the object that will consume messages
     */
    public MessageDispatcher(InputMessageQueue inputMessageQueue, BlockingQueue<Message> outputMessageQueue, IOSupplier<Message> inputMessageSupplier, IOConsumer<Message> outputMessageConsumer) {
        this.inputMessageQueue = inputMessageQueue;
        this.outputMessageQueue = outputMessageQueue;
        this.inputMessageSupplier = inputMessageSupplier;
        this.outputMessageConsumer = outputMessageConsumer;
        receiveThreadPool.execute(this::receiveMessageAsync);
        sendThreadPool.execute(this::sendMessageAsync);
    }

    /**
     * Receives messages from the supplier and put them into the inputMessageQueue
     */
    private void receiveMessageAsync() {
        try {
            inputMessageQueue.enqueue(
                    inputMessageSupplier.get(TAKE_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)
            );
        } catch (IOException e) {
            logger.warning("Unable to receive data " + e);
            stop();
        } catch (TimeoutException ignored) {
            // No data received within the timeout
        }

        synchronized (receiveThreadPool) {
            if (!receiveThreadPool.isShutdown()) {
                receiveThreadPool.execute(this::receiveMessageAsync);
            }
        }
    }

    /**
     * Takes messages from the outputMessageQueue and uses the outputMessageConsumer to consume them
     */
    private void sendMessageAsync() {
        try {
            Message message = outputMessageQueue.poll(TAKE_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
            if (message != null) {
                outputMessageConsumer.accept(message);
            }
            synchronized (sendThreadPool) {
                if (!sendThreadPool.isShutdown()) {
                    sendThreadPool.execute(this::sendMessageAsync);
                }
            }
        } catch (InterruptedException e) {
            logger.warning("Thread interrupted " + e);
            stop();
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            logger.warning("Unable to send data " + e);
            stop();
        }
    }

    /**
     * Adds a listener for the stop event
     *
     * @param l the new listener
     */
    public void addMessageDispatcherStoppedListener(MessageDispatcherStoppedListener l) {
        this.stoppedListeners.add(l);
    }

    /**
     * Unsubscribe the specified listener from the event
     *
     * @param l the listener to remove
     */
    public void removeMessageDispatcherStoppedListener(MessageDispatcherStoppedListener l) {
        this.stoppedListeners.remove(l);
    }

    /**
     * Notifies the stop event to all the subscribed listeners
     */
    private void notifyMessageDispatcherStopped() {
        MessageDispatcherStopped e = new MessageDispatcherStopped(this);
        stoppedListeners.forEach(l -> l.onMessageDispatcherStopped(e));
    }

    /**
     * Shuts down the thread pools
     */
    private void stop() {
        synchronized (receiveThreadPool) {
            receiveThreadPool.shutdown();
        }
        synchronized (sendThreadPool) {
            sendThreadPool.shutdown();
        }
    }

    /**
     * Closes this object and stops the background tasks execution
     *
     * @throws InterruptedException if the thread was forced to stop
     */
    @Override
    public void close() throws InterruptedException {
        stop();
        try {
            while (
                    !receiveThreadPool.awaitTermination(5, TimeUnit.SECONDS)
                    && !sendThreadPool.awaitTermination(5, TimeUnit.SECONDS)
            ) {
                logger.warning("Thread pools haven't shut down yet, waiting...");
            }
            notifyMessageDispatcherStopped();
        } catch (InterruptedException ex) {
            logger.warning("Unexpected thread interruption, unable to correctly shut down the threadPools and notify this to listeners");
            throw ex;
        }
    }
}
