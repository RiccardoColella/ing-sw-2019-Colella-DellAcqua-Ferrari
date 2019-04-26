package it.polimi.ingsw.shared.view.remote;

import it.polimi.ingsw.shared.InputMessageQueue;
import it.polimi.ingsw.shared.messages.Message;
import it.polimi.ingsw.shared.view.remote.events.MessageDispatcherStopped;
import it.polimi.ingsw.shared.view.remote.events.listeners.MessageDispatcherStoppedListener;
import it.polimi.ingsw.utils.function.TimeoutConsumer;
import it.polimi.ingsw.utils.function.TimeoutSupplier;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;


/**
 * This class dispatches the messages from the input and output queues provided a supplier and a consumer
 *
 * @author Carlo Dell'Acqua
 */
public class MessageDispatcher implements AutoCloseable {

    private static final int TAKE_TIMEOUT_MILLISECONDS = 1000;

    private final TimeoutSupplier<Message> inputMessageSupplier;
    private final TimeoutConsumer<Message> outputMessageConsumer;
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private InputMessageQueue inputMessageQueue;
    private BlockingQueue<Message> outputMessageQueue;
    private ExecutorService threadPool = Executors.newFixedThreadPool(2);

    private List<MessageDispatcherStoppedListener> stoppedListeners = new LinkedList<>();

    public MessageDispatcher(InputMessageQueue inputMessageQueue, BlockingQueue<Message> outputMessageQueue, TimeoutSupplier<Message> inputMessageSupplier, TimeoutConsumer<Message> outputMessageConsumer) {
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
                    inputMessageSupplier.get(TAKE_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)
            );
        } catch (IOException e) {
            logger.warning("Unable to receive data " + e);
            stop();
        } catch (TimeoutException ignored) { }

        synchronized (threadPool) {
            if (!threadPool.isShutdown()) {
                threadPool.execute(this::receiveMessageAsync);
            }
        }
    }

    private void sendMessageAsync() {
        try {
            Message message = outputMessageQueue.poll(TAKE_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
            if (message != null) {
                outputMessageConsumer.accept(message);
            }
            synchronized (threadPool) {
                if (!threadPool.isShutdown()) {
                    threadPool.execute(this::sendMessageAsync);
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
        synchronized (threadPool) {
            threadPool.shutdown();
        }
    }

    @Override
    public void close() throws Exception {
        stop();
        try {
            while (!threadPool.awaitTermination(1, TimeUnit.SECONDS)) {
                logger.warning("Thread pool hasn't shut down yet, waiting...");
            }
            notifyMessageDispatcherStopped();
        } catch (InterruptedException ex) {
            logger.warning("Unexpected thread interruption, unable to correctly shutdown the threadPool and notify this to listeners");
            Thread.currentThread().interrupt();
        }
    }
}
