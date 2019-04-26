package it.polimi.ingsw.shared.view.remote;

import it.polimi.ingsw.shared.InputMessageQueue;
import it.polimi.ingsw.shared.messages.Message;
import it.polimi.ingsw.shared.view.remote.events.MessageDispatcherStopped;
import it.polimi.ingsw.shared.view.remote.events.listeners.MessageDispatcherStoppedListener;
import it.polimi.ingsw.utils.function.UnsafeConsumer;
import it.polimi.ingsw.utils.function.UnsafeSupplier;
import it.polimi.ingsw.utils.function.exceptions.UnsafeSupplierException;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MessageDispatcher implements AutoCloseable {

    private final UnsafeSupplier<Message> inputMessageSupplier;
    private final UnsafeConsumer<Message> outputMessageConsumer;
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private InputMessageQueue inputMessageQueue;
    private BlockingQueue<Message> outputMessageQueue;
    private ExecutorService threadPool = Executors.newFixedThreadPool(2);

    private List<MessageDispatcherStoppedListener> stoppedListeners = new LinkedList<>();

    public MessageDispatcher(InputMessageQueue inputMessageQueue, BlockingQueue<Message> outputMessageQueue, UnsafeSupplier<Message> inputMessageSupplier, UnsafeConsumer<Message> outputMessageConsumer) {
        this.inputMessageQueue = inputMessageQueue;
        this.outputMessageQueue = outputMessageQueue;
        this.inputMessageSupplier = inputMessageSupplier;
        this.outputMessageConsumer = outputMessageConsumer;
        threadPool.execute(this::receiveMessageAsync);
        threadPool.execute(this::sendMessageAsync);
    }

    private void receiveMessageAsync() {
        try {
            inputMessageQueue.enqueue(
                    inputMessageSupplier.get()
            );
            threadPool.execute(this::receiveMessageAsync);
        } catch (UnsafeSupplierException e) {
            stop();
        }
    }

    private void sendMessageAsync() {
        try {
            outputMessageConsumer.accept(outputMessageQueue.take());
            threadPool.execute(this::sendMessageAsync);
        } catch (InterruptedException e) {
            logger.warning("Thread interrupted " + e);
            stop();
            Thread.currentThread().interrupt();
        } catch (UnsafeSupplierException e) {
            logger.warning("Unable to send message " + e);
            stop();
        }
    }

    public void addMessageDispatcherStoppedListener(MessageDispatcherStoppedListener l) {
        this.stoppedListeners.add(l);
    }

    public void removeMessageDispatcherStoppedListener(MessageDispatcherStoppedListener l) {
        this.stoppedListeners.remove(l);
    }

    private void notifyMessageDispatcherStopped() {
        MessageDispatcherStopped e = new MessageDispatcherStopped(this);
        stoppedListeners.forEach(l -> l.onMessageDispatcherStopped(e));
    }

    private void stop() {
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                logger.warning("Thread pool hasn't shut down yet, waiting...");
            }
            notifyMessageDispatcherStopped();
        } catch (InterruptedException ex) {
            logger.warning("Unexpected thread interruption, unable to correctly shutdown the threadPool and notify this to listeners");
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() throws Exception {
        stop();
    }
}
