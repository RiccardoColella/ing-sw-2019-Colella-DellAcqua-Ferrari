package it.polimi.ingsw.client.io;

import it.polimi.ingsw.shared.InputMessageQueue;
import it.polimi.ingsw.shared.events.MessageReceived;
import it.polimi.ingsw.shared.events.listeners.EventMessageReceivedListener;
import it.polimi.ingsw.shared.events.listeners.QuestionMessageReceivedListener;
import it.polimi.ingsw.shared.messages.Message;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * This class represents the entity which manages the IO of messages between
 * the client and the server
 *
 * @author Carlo Dell'Acqua
 */
public abstract class Connector implements AutoCloseable {
    /**
     * Logging utility
     */
    protected final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Timeout needed to prevent deadlocks
     */
    protected static final int DEQUEUE_TIMEOUT_MILLISECONDS = 1000;

    /**
     * The message queue which accumulates input messages
     */
    protected InputMessageQueue inputMessageQueue = new InputMessageQueue();

    /**
     * The message queue which stores output messages ready to be sent
     */
    protected LinkedBlockingQueue<Message> outputMessageQueue = new LinkedBlockingQueue<>();

    /**
     * The thread pool that schedules the execution of the receiveAsync method for events and questions
     */
    private final ExecutorService threadPool = Executors.newFixedThreadPool(2);

    /**
     * Event message listeners
     */
    private List<EventMessageReceivedListener> eventListeners = new LinkedList<>();

    /**
     * Question message listeners
     */
    private List<QuestionMessageReceivedListener> questionListeners = new LinkedList<>();

    /**
     * Base constructor that automatically start the receiving tasks
     */
    public Connector() {
        threadPool.execute(() -> receiveAsync(Message.Type.EVENT));
        threadPool.execute(() -> receiveAsync(Message.Type.QUESTION));
    }

    /**
     * Receive events and questions reading them from the input queues
     *
     * @param type the type of message (question or event) to wait
     */
    private void receiveAsync(Message.Type type) {

        try {
            switch (type) {
                case EVENT:
                    try {
                        notifyEventMessageReceivedListeners(
                            inputMessageQueue
                                    .dequeueEvent(DEQUEUE_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)
                        );
                    } catch (TimeoutException ignored) {
                        // No message received within the timeout
                    }
                    break;
                case QUESTION:
                    try {
                        notifyQuestionMessageReceivedListeners(
                                inputMessageQueue
                                        .dequeueQuestion(DEQUEUE_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)
                        );
                    } catch (TimeoutException ignored) {
                        // No message received within the timeout
                    }
                    break;
            }

            synchronized (threadPool) {
                if (!threadPool.isShutdown()) {
                    threadPool.execute(() -> receiveAsync(type));
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.warning("Thread interrupted " + ex);
        }
    }

    /**
     * Sends messages in a virtual way, enqueueing them into the output message queue
     *
     * @param message the message to send
     */
    public void sendMessage(Message message) {
        outputMessageQueue.add(message);
    }

    /**
     * Adds a listener of input question messages
     *
     * @param l the listener
     */
    public void addQuestionMessageReceivedListener(QuestionMessageReceivedListener l) {
        questionListeners.add(l);
    }

    /**
     * Removes a listener of input question messages
     *
     * @param l the listener
     */
    public void removeQuestionMessageReceivedListener(QuestionMessageReceivedListener l) {
        questionListeners.remove(l);
    }

    /**
     * Notifies question listeners that a message has been received
     * @param message the received question message
     */
    private void notifyQuestionMessageReceivedListeners(Message message) {
        MessageReceived e = new MessageReceived(this, message);
        questionListeners.forEach(l -> l.onQuestionMessageReceived(e));
    }

    /**
     * Adds a listener of input event messages
     *
     * @param l the listener
     */
    public void addEventMessageReceivedListener(EventMessageReceivedListener l) {
        eventListeners.add(l);
    }

    /**
     * Removes a listener of input event messages
     *
     * @param l the listener
     */
    public void removeEventMessageReceivedListener(EventMessageReceivedListener l) {
        eventListeners.remove(l);
    }

    /**
     * Notifies event listeners that a message has been received
     *
     * @param message the received event message
     */
    private void notifyEventMessageReceivedListeners(Message message) {
        MessageReceived e = new MessageReceived(this, message);
        eventListeners.forEach(l -> l.onEventMessageReceived(e));
    }

    /**
     * Closes this object and stops the background threads execution
     *
     * @throws Exception if the closing process is forced to stop
     */
    @Override
    public void close() throws Exception {
        synchronized (threadPool) {
            threadPool.shutdown();
        }
        while (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
            logger.warning("Thread pool hasn't shut down yet, waiting...");
        }
    }
}
