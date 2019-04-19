package it.polimi.ingsw.server.view;

import it.polimi.ingsw.server.controller.exceptions.ViewDisconnectedException;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.PowerupTile;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.PlayerInfo;
import it.polimi.ingsw.shared.commands.Command;
import it.polimi.ingsw.shared.commands.answers.SelectMultipleAnswer;
import it.polimi.ingsw.shared.commands.answers.SelectOptionAnswer;
import it.polimi.ingsw.shared.commands.answers.SelectOptionOrSkipAnswer;
import it.polimi.ingsw.shared.commands.questions.SelectMultipleQuestion;
import it.polimi.ingsw.shared.commands.questions.SelectOptionOrSkipQuestion;
import it.polimi.ingsw.shared.commands.questions.SelectOptionQuestion;
import it.polimi.ingsw.shared.events.CommandReceived;
import it.polimi.ingsw.shared.events.listeners.CommandReceivedListener;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class is an abstract server-side SocketView. It contains all the methods needed for the interaction with the controller
 * and implements all the listeners needed to receive information from the model
 */
public abstract class View implements Interviewer {

    private boolean connected = true;

    private Map<String, BlockingQueue<Command>> commandQueues = new HashMap<>();

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

    protected void enqueCommand(Command command) {
        if (!commandQueues.containsKey(command.getName())) {
            commandQueues.put(command.getName(), new LinkedBlockingQueue<>());
        }
        commandQueues.get(command.getName()).add(command);
        notifyCommandReceived(command);
    }

    protected Command dequeueCommand(String commandName) {
        if (!commandQueues.containsKey(commandName)) {
            commandQueues.put(commandName, new LinkedBlockingQueue<>());
        }
        try {
            return commandQueues.get(commandName).take();
        } catch (InterruptedException ex) {
            connected = false;
            throw new ViewDisconnectedException("Player disconnected, unable to select");
        }
    }

    public abstract PowerupTile chooseSpawnpoint(List<PowerupTile> powerups);

    public boolean isConnected() {
        return connected;
    }

    public abstract PlayerInfo getPlayerInfo();

    public abstract BoardFactory.Preset getChosenPreset();

    public abstract int getChosenSkulls();

    public abstract Match.Mode getChosenMode();

    public abstract void sendCommand(Command command);

    @Override
    public <T> T select(Collection<T> options) {
        sendCommand(new SelectOptionQuestion<>(options));
        return dequeueCommand(SelectOptionAnswer.class.getName()).getPayload();
    }

    @Override
    public <T> Optional<T> selectOptional(Collection<T> options) {
        sendCommand(new SelectOptionOrSkipQuestion<>(options));
        return dequeueCommand(SelectOptionOrSkipAnswer.class.getName()).getPayload();
    }

    @Override
    public <T> List<T> selectMultiple(Collection<T> options) {
        sendCommand(new SelectMultipleQuestion<>(options));
        return dequeueCommand(SelectMultipleAnswer.class.getName()).getPayload();
    }
}
