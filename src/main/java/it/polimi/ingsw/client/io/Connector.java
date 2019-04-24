package it.polimi.ingsw.client.io;

import it.polimi.ingsw.shared.InputMessageQueue;
import it.polimi.ingsw.shared.messages.Message;
import it.polimi.ingsw.shared.events.MessageReceived;
import it.polimi.ingsw.shared.events.listeners.EventMessageReceivedListener;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public abstract class Connector implements AutoCloseable {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    protected InputMessageQueue inputMessageQueue = new InputMessageQueue();
    protected LinkedBlockingQueue<Message> outputMessageQueue = new LinkedBlockingQueue<>();
    private ExecutorService threadPool = Executors.newFixedThreadPool(2);
    private List<EventMessageReceivedListener> eventListeners = new LinkedList<>();
    private List<EventMessageReceivedListener> questionListeners = new LinkedList<>();

    public Connector() {
        threadPool.execute(() -> receiveAsync(Message.Type.EVENT));
        threadPool.execute(() -> receiveAsync(Message.Type.QUESTION));
    }

    private void receiveAsync(Message.Type type) {

        try {
            switch (type) {
                case EVENT:
                    notifyEventMessageReceivedListeners(inputMessageQueue.dequeueEvent());
                    break;
                case QUESTION:
                    notifyQuestionMessageReceivedListeners(inputMessageQueue.dequeueQuestion());
                    break;
            }

            if (!threadPool.isShutdown()) {
                threadPool.execute(() -> receiveAsync(type));
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.warning("Thread interrupted " + ex.toString());
        }
    }

    public void sendMessage(Message message) {
        outputMessageQueue.add(message);
    }

    public void addQuestionMessageReceivedListener(EventMessageReceivedListener l) {
        questionListeners.add(l);
    }
    public void removeQuestionMessageReceivedListener(EventMessageReceivedListener l) {
        questionListeners.remove(l);
    }
    public void notifyQuestionMessageReceivedListeners(Message message) {
        MessageReceived e = new MessageReceived(this, message);
        questionListeners.forEach(l -> l.onEventMessageReceived(e));
    }

    public void addEventMessageReceivedListener(EventMessageReceivedListener l) {
        eventListeners.add(l);
    }
    public void removeEventMessageReceivedListener(EventMessageReceivedListener l) {
        eventListeners.remove(l);
    }
    public void notifyEventMessageReceivedListeners(Message message) {
        MessageReceived e = new MessageReceived(this, message);
        eventListeners.forEach(l -> l.onEventMessageReceived(e));
    }

    @Override
    public void close() throws Exception {
        threadPool.shutdown();
        while (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
            logger.warning("Thread pool did not shutdown yet, waiting...");
        }
    }
}
