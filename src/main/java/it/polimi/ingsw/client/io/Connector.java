package it.polimi.ingsw.client.io;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.client.io.listeners.*;
import it.polimi.ingsw.server.model.player.BasicAction;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.shared.InputMessageQueue;
import it.polimi.ingsw.shared.bootstrap.ClientInitializationInfo;
import it.polimi.ingsw.shared.events.networkevents.*;
import it.polimi.ingsw.shared.messages.ClientApi;
import it.polimi.ingsw.shared.messages.Message;
import it.polimi.ingsw.shared.messages.ServerApi;
import it.polimi.ingsw.shared.messages.templates.Answer;
import it.polimi.ingsw.shared.messages.templates.Question;
import it.polimi.ingsw.shared.viewmodels.Powerup;

import java.awt.*;
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
    private Set<BoardListener> boardListeners = new HashSet<>();
    private Set<PlayerListener> playerListeners = new HashSet<>();


    /**
     * Initializes the connector and its IO queues
     *
     * @param clientInitializationInfo the user preferences for the match
     */
    protected void initialize(ClientInitializationInfo clientInitializationInfo) {
        outputMessageQueue.add(Message.createEvent(ServerApi.VIEW_INIT_EVENT, clientInitializationInfo));
        threadPool.execute(() -> receiveAsync(Message.Type.EVENT));
    }

    public void startListeningToQuestions() {
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

    private <T> void enqueueAnswer(T choice, String flowId) {
        outputMessageQueue
                .add(Message.createAnswer(ServerApi.ANSWER, new Answer<>(choice), flowId));
    }

    /**
     * Notifies question listeners that a message has been received
     * @param message the received question message
     */
    private void notifyQuestionMessageReceivedListeners(Message message) {
        ClientApi questionType = message.getNameAsEnum(ClientApi.class);
        switch (questionType) {
            case DIRECTION_QUESTION: {
                Question<Direction> question = Question.fromJson(message.getPayload(), new TypeToken<Question<Direction>>(){}.getType());
                questionListeners.forEach(l -> l.onDirectionQuestion(
                        question,
                        choice -> enqueueAnswer(choice, message.getFlowId())
                        )
                );
                break;
            }
            case ATTACK_QUESTION: {
                Question<String> question = Question.fromJson(message.getPayload(), new TypeToken<Question<String>>(){}.getType());
                questionListeners.forEach(l -> l.onAttackQuestion(
                        question,
                        choice -> enqueueAnswer(choice, message.getFlowId())
                        )
                );
                break;
            }
            case BASIC_ACTION_QUESTION: {
                Question<BasicAction> question = Question.fromJson(message.getPayload(), new TypeToken<Question<BasicAction>>(){}.getType());
                questionListeners.forEach(l -> l.onBasicActionQuestion(
                        question,
                        choice -> enqueueAnswer(choice, message.getFlowId())
                        )
                );
                break;
            }
            case BLOCK_QUESTION: {
                Question<Point> question = Question.fromJson(message.getPayload(), new TypeToken<Question<Point>>(){}.getType());
                questionListeners.forEach(l -> l.onBlockQuestion(
                        question,
                        choice -> enqueueAnswer(choice, message.getFlowId())
                        )
                );
                break;
            }
            case PAYMENT_METHOD_QUESTION: {
                Question<String> question = Question.fromJson(message.getPayload(), new TypeToken<Question<String>>(){}.getType());
                questionListeners.forEach(l -> l.onPaymentMethodQuestion(
                        question,
                        choice -> enqueueAnswer(choice, message.getFlowId())
                        )
                );
                break;
            }
            case POWERUP_QUESTION: {
                Question<Powerup> question = Question.fromJson(message.getPayload(), new TypeToken<Question<Powerup>>(){}.getType());
                questionListeners.forEach(l -> l.onPowerupQuestion(
                        question,
                        choice -> enqueueAnswer(choice, message.getFlowId())
                        )
                );
                break;
            }
            case WEAPON_CHOICE_QUESTION: {
                Question<String> question = Question.fromJson(message.getPayload(), new TypeToken<Question<String>>(){}.getType());
                questionListeners.forEach(l -> l.onWeaponQuestion(
                        question,
                        choice -> enqueueAnswer(choice, message.getFlowId())
                        )
                );
                break;
            }
            case RELOAD_QUESTION: {
                Question<String> question = Question.fromJson(message.getPayload(), new TypeToken<Question<String>>(){}.getType());
                questionListeners.forEach(l -> l.onReloadQuestion(
                        question,
                        choice -> enqueueAnswer(choice, message.getFlowId())
                        )
                );
                break;
            }
            case SPAWNPOINT_QUESTION: {
                Question<Powerup> question = Question.fromJson(message.getPayload(), new TypeToken<Question<Powerup>>(){}.getType());
                questionListeners.forEach(l -> l.onSpawnpointQuestion(
                        question,
                        choice -> enqueueAnswer(choice, message.getFlowId())
                        )
                );
                break;
            }
            case TARGET_QUESTION: {
                Question<String> question = Question.fromJson(message.getPayload(), new TypeToken<Question<String>>(){}.getType());
                questionListeners.forEach(l -> l.onTargetQuestion(
                        question,
                        choice -> enqueueAnswer(choice, message.getFlowId())
                        )
                );
                break;
            }
        }

    }

    /**
     * Notifies event listeners that a message has been received
     *
     * @param message the received event message
     */
    private void notifyEventMessageReceivedListeners(Message message) {

        ClientApi eventType = message.getNameAsEnum(ClientApi.class);
        switch (eventType) {
            case DUPLICATE_NICKNAME_EVENT: {
                duplicatedNicknameListeners.forEach(DuplicatedNicknameListener::onDuplicatedNickname);
                break;
            }

            case MATCH_STARTED_EVENT: {
                MatchStarted e = MatchStarted.fromJson(message.getPayload(), this, MatchStarted.class);
                matchListeners.forEach(l -> l.onMatchStarted(e));
                break;
            }
            case MATCH_ENDED_EVENT: {
                MatchEnded e = MatchEnded.fromJson(message.getPayload(), this, MatchEnded.class);
                matchListeners.forEach(l -> l.onMatchEnded(e));
                break;
            }
            case MATCH_MODE_CHANGED_EVENT: {
                MatchModeChanged e = MatchModeChanged.fromJson(message.getPayload(), this, MatchModeChanged.class);
                matchListeners.forEach(l -> l.onMatchModeChanged(e));
                break;
            }
            case MATCH_KILLSHOT_TRACK_CHANGED_EVENT: {
                KillshotTrackChanged e = KillshotTrackChanged.fromJson(message.getPayload(), this, KillshotTrackChanged.class);
                matchListeners.forEach(l -> l.onKillshotTrackChanged(e));
                break;
            }

            case PLAYER_MOVED_EVENT: {
                PlayerMoved e = PlayerMoved.fromJson(message.getPayload(), this, PlayerMoved.class);
                boardListeners.forEach(l -> l.onPlayerMoved(e));
                break;
            }
            case PLAYER_TELEPORTED_EVENT: {
                PlayerMoved e = PlayerMoved.fromJson(message.getPayload(), this, PlayerMoved.class);
                boardListeners.forEach(l -> l.onPlayerTeleported(e));
                break;
            }

            case PLAYER_DIED_EVENT: {
                PlayerEvent e = PlayerEvent.fromJson(message.getPayload(), this, PlayerEvent.class);
                playerListeners.forEach(l -> l.onPlayerDied(e));
                break;
            }
            case PLAYER_REBORN_EVENT: {
                PlayerEvent e = PlayerEvent.fromJson(message.getPayload(), this, PlayerEvent.class);
                playerListeners.forEach(l -> l.onPlayerReborn(e));
                break;
            }
            case PLAYER_BOARD_FLIPPED_EVENT: {
                PlayerEvent e = PlayerEvent.fromJson(message.getPayload(), this, PlayerEvent.class);
                playerListeners.forEach(l -> l.onPlayerBoardFlipped(e));
                break;
            }
            case PLAYER_WALLET_CHANGED_EVENT: {
                PlayerWalletChanged e = PlayerWalletChanged.fromJson(message.getPayload(), this, PlayerWalletChanged.class);
                playerListeners.forEach(l -> l.onPlayerWalletChanged(e));
                break;
            }
            case PLAYER_HEALTH_CHANGED_EVENT: {
                PlayerHealthChanged e = PlayerHealthChanged.fromJson(message.getPayload(), this, PlayerHealthChanged.class);
                playerListeners.forEach(l -> l.onPlayerHealthChanged(e));
                break;
            }
            case WEAPON_RELOADED_EVENT: {
                WeaponEvent e = WeaponEvent.fromJson(message.getPayload(), this, WeaponEvent.class);
                playerListeners.forEach(l -> l.onWeaponReloaded(e));
                break;
            }
            case WEAPON_UNLOADED_EVENT: {
                WeaponEvent e = WeaponEvent.fromJson(message.getPayload(), this, WeaponEvent.class);
                playerListeners.forEach(l -> l.onWeaponUnloaded(e));
                break;
            }
            case WEAPON_PICKED_EVENT: {
                WeaponExchanged e = WeaponExchanged.fromJson(message.getPayload(), this, WeaponExchanged.class);
                playerListeners.forEach(l -> l.onWeaponPicked(e));
                break;
            }
            case WEAPON_DROPPED_EVENT: {
                WeaponExchanged e = WeaponExchanged.fromJson(message.getPayload(), this, WeaponExchanged.class);
                playerListeners.forEach(l -> l.onWeaponDropped(e));
                break;
            }
            case PLAYER_DISCONNECTED_EVENT: {
                PlayerEvent e = PlayerEvent.fromJson(message.getPayload(), this, PlayerEvent.class);
                playerListeners.forEach(l -> l.onPlayerDisconnected(e));
                break;
            }
            case PLAYER_RECONNECTED_EVENT: {
                PlayerEvent e = PlayerEvent.fromJson(message.getPayload(), this, PlayerEvent.class);
                playerListeners.forEach(l -> l.onPlayerReconnected(e));
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

    public void addPlayerListener(PlayerListener l) {
        playerListeners.add(l);
    }

    public void removePlayerListener(PlayerListener l) {
        playerListeners.remove(l);
    }

    public void addBoardListener(BoardListener l) {
        boardListeners.add(l);
    }

    public void removeBoardListener(BoardListener l) {
        boardListeners.remove(l);
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
