package it.polimi.ingsw.server.view;

import it.polimi.ingsw.server.controller.exceptions.ViewDisconnectedException;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.PlayerInfo;
import it.polimi.ingsw.shared.InputMessageQueue;
import it.polimi.ingsw.shared.messages.ClientApi;
import it.polimi.ingsw.shared.messages.Message;
import it.polimi.ingsw.shared.messages.Question;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This class is an abstract server-side View. It contains all the methods needed for the interaction with the controller
 * and implements all the listeners needed to receive information from the model
 */
public abstract class View implements Interviewer {

    private boolean connected = true;

    protected InputMessageQueue inputMessageQueue = new InputMessageQueue();
    protected LinkedBlockingQueue<Message> outputMessageQueue = new LinkedBlockingQueue<>();

    protected int answerTimeout;
    protected TimeUnit answerTimeoutUnit;

    public View(int answerTimeout, TimeUnit answerTimeoutUnit) {
        this.answerTimeout = answerTimeout;
        this.answerTimeoutUnit = answerTimeoutUnit;
    }


    public abstract PowerupTile chooseSpawnpoint(List<PowerupTile> powerups);

    public boolean isConnected() {
        return connected;
    }

    public abstract PlayerInfo getPlayerInfo();

    public abstract BoardFactory.Preset getChosenPreset();

    public abstract int getChosenSkulls();

    public abstract Match.Mode getChosenMode();

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
            throw new ViewDisconnectedException("Unable to retrieve input message");
        }
    }

    @Override
    public <T> T select(String questionText, Collection<T> options, ClientApi messageName) {
        if (!options.isEmpty()) {

            Message message = Message.createQuestion(messageName.toString(), new Question<>(questionText, options));

            outputMessageQueue.add(message);

            T response = awaitResponse(message.getStreamId(), options);
            if (response == null || !options.contains(response)) {
                throw new IllegalStateException("Received an invalid answer from the client");
            }

            return response;
        } else {
            throw new IllegalArgumentException("No option provided");
        }
    }

    @Override
    public <T> Optional<T> selectOptional(String questionText, Collection<T> options, ClientApi messageName) {
        if (!options.isEmpty()) {
            Message message = Message.createQuestion(messageName.toString(), new Question<>(questionText, options, true));
            outputMessageQueue.add(message);

            T response = awaitResponse(message.getStreamId(), options);
            if (response != null && !options.contains(response)) {
                throw new IllegalStateException("Received an invalid answer from the client");
            }

            return Optional.ofNullable(response);
        } else {
            throw new IllegalArgumentException("No option provided");
        }
    }
}
