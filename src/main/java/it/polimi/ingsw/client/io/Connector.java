package it.polimi.ingsw.client.io;

import com.google.gson.Gson;
import it.polimi.ingsw.client.io.listeners.DuplicatedNicknameListener;
import it.polimi.ingsw.shared.InputMessageQueue;
import it.polimi.ingsw.shared.bootstrap.ClientInitializationInfo;
import it.polimi.ingsw.shared.events.MatchStarted;
import it.polimi.ingsw.shared.events.MessageReceived;
import it.polimi.ingsw.client.io.listeners.MatchListener;
import it.polimi.ingsw.client.io.listeners.QuestionMessageReceivedListener;
import it.polimi.ingsw.shared.messages.ClientApi;
import it.polimi.ingsw.shared.messages.Message;
import it.polimi.ingsw.shared.messages.ServerApi;

import java.util.HashSet;
import java.util.Set;
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
     * JSON conversion utility
     */
    private static Gson gson = new Gson();

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
     * Question message listeners
     */
    private Set<QuestionMessageReceivedListener> questionListeners = new HashSet<>();


    /**
     * The following sets contain the listeners for all possible events that the connector can raise
     */
    private Set<MatchListener> matchListeners = new HashSet<>();
    private Set<DuplicatedNicknameListener> duplicatedNicknameListeners = new HashSet<>();
    // TODO: add the other sets


    /**
     * Initializes the connector and its IO queues
     *
     * @param clientInitializationInfo the user preferences for the match
     */
    protected void initialize(ClientInitializationInfo clientInitializationInfo) {
        outputMessageQueue.add(Message.createEvent(ServerApi.VIEW_INIT_EVENT, clientInitializationInfo));
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
     * Notifies event listeners that a message has been received
     *
     * @param message the received event message
     */
    private void notifyEventMessageReceivedListeners(Message message) {

        ClientApi eventType = message.getNameAsEnum(ClientApi.class);

        switch (eventType) {
            case MATCH_STARTED_EVENT: {
                MatchStarted e = gson.fromJson(message.getPayload(), MatchStarted.class);
                matchListeners.forEach(l -> l.onMatchStarted(e));
                break;
            }

            case DUPLICATE_NICKNAME_EVENT: {
                duplicatedNicknameListeners.forEach(DuplicatedNicknameListener::onDuplicatedNickname);
                break;
            }

            default: {
                throw new UnsupportedOperationException("Event \"" + eventType + "\" not supported");
            }
        }
    }

    public void addMatchListener(MatchListener l) {
        matchListeners.add(l);
    }

    public void removeMatchListener(MatchListener l) {
        matchListeners.remove(l);
    }

    public void addDuplicatedNicknameListener(DuplicatedNicknameListener l) {
        duplicatedNicknameListeners.add(l);
    }

    public void removeDuplicatedNicknameListener(DuplicatedNicknameListener l) {
        duplicatedNicknameListeners.remove(l);
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
