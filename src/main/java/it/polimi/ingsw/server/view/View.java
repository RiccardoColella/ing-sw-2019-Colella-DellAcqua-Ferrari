package it.polimi.ingsw.server.view;

import com.google.gson.Gson;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.view.exceptions.ViewDisconnectedException;
import it.polimi.ingsw.shared.InputMessageQueue;
import it.polimi.ingsw.shared.bootstrap.ClientInitializationInfo;
import it.polimi.ingsw.shared.messages.ClientApi;
import it.polimi.ingsw.shared.messages.Message;
import it.polimi.ingsw.shared.messages.Question;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * This class is an abstract server-side View. It contains all the methods needed for the interaction with the controller
 * and implements all the listeners needed to receive information from the model
 *
 * @author Carlo Dell'Acqua
 */
public abstract class View implements Interviewer, AutoCloseable {

    /**
     * JSON conversion utility
     */
    private static Gson gson = new Gson();

    /**
     * Logging utility
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Boolean representing the virtual connection status
     */
    private boolean connected = true;

    /**
     * The message queue which accumulates input messages
     */
    protected InputMessageQueue inputMessageQueue = new InputMessageQueue();

    /**
     * The message queue which stores output messages ready to be sent
     */
    protected LinkedBlockingQueue<Message> outputMessageQueue = new LinkedBlockingQueue<>();

    /**
     * Maximum timeout before considering the view disconnected
     */
    protected int answerTimeout;
    /**
     * Measurement unit of the timeout
     */
    protected TimeUnit answerTimeoutUnit;

    /**
     * Player initialization information
     */
    private ClientInitializationInfo setup;

    /**
     * Constructs a server-side view
     *
     * @param answerTimeout maximum timeout before considering the view disconnected
     * @param answerTimeoutUnit measurement unit of the timeout
     */
    public View(int answerTimeout, TimeUnit answerTimeoutUnit) {
        this.answerTimeout = answerTimeout;
        this.answerTimeoutUnit = answerTimeoutUnit;
    }

    /**
     * Initializes the view. This method should be called after the view construction to collect the user preferences from the client view
     *
     * @throws InterruptedException if the thread was forced to stop
     */
    public void initialize() throws InterruptedException {
        try {
            this.setup = gson.fromJson(
                    inputMessageQueue
                            .dequeueEvent(5, TimeUnit.SECONDS)
                            .getPayload(),
                    ClientInitializationInfo.class
            );
        } catch (TimeoutException ex) {
            throw new ViewDisconnectedException("Initialization event message not received", ex);
        }
    }

    /**
     * @return true if the view is still considered virtually connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * @return the nickname chosen by the player
     */
    public String getNickname() {
        return setup.getNickname();
    }

    /**
     * @return the preset chosen by the player
     */
    public BoardFactory.Preset getChosenPreset() {
        return setup.getPreset();
    }

    /**
     * @return the number of skulls chosen by the player
     */
    public int getChosenSkulls() {
        return setup.getSkulls();
    }

    /**
     * @return the match mode chosen by the player
     */
    public Match.Mode getChosenMode() {
        return setup.getMode();
    }

    /**
     * Called after sending a question, this method waits on the input message queue until a message associated with
     * the question and answer flow is available
     *
     * @param streamId the identifier of the question-and-answer flow
     * @param options the available answers for the question
     * @param <T> the type of the item in the option collection
     * @return the chosen option or null if no choice was made
     * @throws ViewDisconnectedException if the client didn't give an answer within the available answering timeout
     */
    @Nullable
    private  <T> T awaitResponse(String streamId, Collection<T> options) {

        try {
            Message response = inputMessageQueue.dequeueAnswer(streamId, answerTimeout, answerTimeoutUnit);
            if (response.getPayload().getAsInt() == 0) {
                return null;
            } else {
                return new ArrayList<>(options).get(response.getPayload().getAsInt() - 1);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            connected = false;
            throw new ViewDisconnectedException("Unable to retrieve input message", e);
        } catch (TimeoutException e) {
            connected = false;
            throw new ViewDisconnectedException("Unable to retrieve input message", e);
        }
    }

    /**
     * Sends a question message to the client view and wait for the response
     *
     * @param questionText the question to show to the user
     * @param options a collection of options to choose from
     * @param messageName the name which identifies the type of message that is been sent
     * @param <T> the type of the item in the option collection
     * @return the chosen answer
     * @throws ViewDisconnectedException if the client wasn't able to give a correct answer within the timeout
     * @throws IllegalArgumentException if the an empty collection was provided for the "options" parameter
     */
    @Override
    public <T> T select(String questionText, Collection<T> options, ClientApi messageName) {
        if (!options.isEmpty()) {

            Message message = Message.createQuestion(messageName.toString(), new Question<>(questionText, options));

            outputMessageQueue.add(message);

            T response = awaitResponse(message.getStreamId(), options);
            if (response == null || !options.contains(response)) {
                throw new ViewDisconnectedException("Received an invalid answer from the client");
            }

            return response;
        } else {
            throw new IllegalArgumentException("No option provided");
        }
    }

    /**
     * Sends a question message to the client view and wait for the response that can be empty
     *
     * @param questionText the question to show to the user
     * @param options a collection of options to choose from
     * @param messageName the name which identifies the type of message that is been sent
     * @param <T> the type of the item in the option collection
     * @return the chosen answer or an empty optional
     * @throws ViewDisconnectedException if the client wasn't able to give a correct answer within the timeout
     * @throws IllegalArgumentException if the an empty collection was provided for the "options" parameter
     */
    @Override
    public <T> Optional<T> selectOptional(String questionText, Collection<T> options, ClientApi messageName) {
        if (!options.isEmpty()) {
            Message message = Message.createQuestion(messageName.toString(), new Question<>(questionText, options, true));
            outputMessageQueue.add(message);

            T response = awaitResponse(message.getStreamId(), options);
            if (response != null && !options.contains(response)) {
                throw new ViewDisconnectedException("Received an invalid answer from the client");
            }

            return Optional.ofNullable(response);
        } else {
            throw new IllegalArgumentException("No option provided");
        }
    }
}
