package it.polimi.ingsw.client.io;

import it.polimi.ingsw.shared.commands.Command;
import it.polimi.ingsw.shared.events.CommandReceived;
import it.polimi.ingsw.shared.events.listeners.CommandReceivedListener;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public abstract class Connector implements AutoCloseable {

    protected Logger logger = Logger.getLogger(this.getClass().getName());

    private final ExecutorService threadPool = Executors.newSingleThreadExecutor();

    private final List<CommandReceivedListener> listeners = new LinkedList<>();

    public void startReceivingCommands() {
        threadPool.execute(this::receiveCommandAsync);
    }

    private void receiveCommandAsync() {

        notifyCommandReceived(receiveCommand());

        if (!threadPool.isShutdown()) {
            threadPool.execute(this::receiveCommandAsync);
        }
    }

    protected abstract Command receiveCommand();
    public abstract void sendCommand(Command command);

    public void addCommandReceivedListener(CommandReceivedListener l) {
        listeners.add(l);
    }
    public void removeCommandReceivedListener(CommandReceivedListener l) {
        listeners.remove(l);
    }

    private void notifyCommandReceived(Command command) {
        CommandReceived e = new CommandReceived(this, command);
        listeners.forEach(l -> l.onCommandReceived(e));
    }

    @Override
    public void close() throws Exception {
        threadPool.shutdown();
        while (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
            logger.warning("Thread pool did not shutdown yet, waiting...");
        }
    }
}
