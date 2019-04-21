package it.polimi.ingsw.server.view;

import it.polimi.ingsw.server.controller.exceptions.ViewDisconnectedException;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;

import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.BasicAction;
import it.polimi.ingsw.server.model.player.PlayerInfo;
import it.polimi.ingsw.server.model.weapons.Weapon;
import it.polimi.ingsw.shared.CommandQueue;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.shared.commands.*;
import it.polimi.ingsw.shared.events.CommandReceived;
import it.polimi.ingsw.shared.events.listeners.CommandReceivedListener;
import it.polimi.ingsw.utils.EnumValueByString;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * This class is an abstract server-side SocketView. It contains all the methods needed for the interaction with the controller
 * and implements all the listeners needed to receive information from the model
 */
public abstract class View implements Interviewer {

    private boolean connected = true;

    private CommandQueue inputCommandQueue = new CommandQueue();
    private CommandQueue outputCommandQueue = new CommandQueue();

    private List<CommandReceivedListener> listeners = new LinkedList<>();
    private List<CommandReceivedListener> singleTriggerListeners = new LinkedList<>();

    public void addCommandReceivedListener(CommandReceivedListener l) {
        listeners.add(l);
    }

    public void addSingleTriggerCommandReceivedListener(CommandReceivedListener l) {
        singleTriggerListeners.add(l);
    }

    public void removeCommandReceivedListener(CommandReceivedListener l) {
        listeners.remove(l);
    }

    public void removeSingleTriggerCommandReceivedListener(CommandReceivedListener l) {
        singleTriggerListeners.remove(l);
    }

    protected void notifyCommandReceived(Command command) {
        CommandReceived e = new CommandReceived(this, command);
        singleTriggerListeners.forEach(l -> l.onCommandReceived(e));
        singleTriggerListeners.clear();
        listeners.forEach(l -> l.onCommandReceived(e));
    }

    public abstract PowerupTile chooseSpawnpoint(List<PowerupTile> powerups);

    public boolean isConnected() {
        return connected;
    }

    public abstract PlayerInfo getPlayerInfo();

    public abstract BoardFactory.Preset getChosenPreset();

    public abstract int getChosenSkulls();

    public abstract Match.Mode getChosenMode();

    protected Command dequeInputCommand(String command) {
        try {
            return inputCommandQueue.dequeue(command);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            connected = false;
            throw new ViewDisconnectedException("Unable to retrieve input command");
        }
    }

    protected void enqueueInputCommand(Command command) {
        inputCommandQueue.enqueue(command);
    }

    protected void enqueueOutputCommand(Command command) {
        inputCommandQueue.enqueue(command);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected <T> T awaitResponse(ClientApi commandName) {
        Command response;
        switch (commandName) {
            case DIRECTION_QUESTION:
                response = dequeInputCommand(ServerApi.DIRECTION_ANSWER.toString());
                return (T)EnumValueByString.findByString(response.getPayload().getAsJsonObject().get("direction").getAsString(), Direction.class);
            default:
                throw new EnumConstantNotPresentException(ClientApi.class, "Unmanaged client response");
        }
    }

    @Override
    public <T> T select(String questionText, Collection<T> options, ClientApi commandName) {
        if (!options.isEmpty()) {
            outputCommandQueue.enqueue(new Command(commandName.toString(), new Question<>(questionText, options)));

            T response = awaitResponse(commandName);
            if (response == null || !options.contains(response)) {
                throw new IllegalStateException("Received an invalid answer from the client");
            }

            return response;
        } else {
            throw new IllegalArgumentException("No option provided");
        }
    }

    @Override
    public <T> Optional<T> selectOptional(String questionText, Collection<T> options, ClientApi commandName) {
        if (!options.isEmpty()) {
            outputCommandQueue.enqueue(new Command(commandName.toString(), new Question<>(questionText, options, true)));

            T response = awaitResponse(commandName);
            if (response != null && !options.contains(response)) {
                throw new IllegalStateException("Received an invalid answer from the client");
            }

            return Optional.ofNullable(response);
        } else {
            throw new IllegalArgumentException("No option provided");
        }
    }

    @Override
    public <T> Optional<T> selectOptional(Collection<T> options) {
        return selectOptional("", options, ClientApi.BLOCK_QUESTION);
    }

    @Override
    public <T> T select(Collection<T> options) {
        return select("", options, ClientApi.BLOCK_QUESTION);
    }
}
