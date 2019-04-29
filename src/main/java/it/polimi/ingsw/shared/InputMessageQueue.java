package it.polimi.ingsw.shared;

import it.polimi.ingsw.shared.messages.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The input message queue used to listen for incoming messages
 *
 * @author Carlo Dell'Acqua
 */
public class InputMessageQueue {

    /**
     * This queue contains question messages
     */
    private BlockingQueue<Message> questionMessageQueues = new LinkedBlockingQueue<>();

    /**
     * This queue contains event messages
     */
    private BlockingQueue<Message> eventMessageQueues = new LinkedBlockingQueue<>();

    /**
     * This map contains the Question-Answer flows identified by a unique id, represented by a string. Each flow
     * has an associated queue where answer are stored waiting to be consumed
     */
    private Map<String, BlockingQueue<Message>> answerMessageQueues = new HashMap<>();

    /**
     * Adds a message to appropriate queue
     *
     * @param message the message to enqueue
     */
    public void enqueue(Message message) {

        switch (message.getType()) {
            case QUESTION:
                questionMessageQueues.add(message);
                break;
            case EVENT:
                eventMessageQueues.add(message);
                break;
            case ANSWER:
                if (!answerMessageQueues.containsKey(message.getStreamId())) {
                    answerMessageQueues.put(message.getStreamId(), new LinkedBlockingQueue<>());
                }
                answerMessageQueues.get(message.getStreamId()).add(message);
                break;
        }
    }

    /**
     * Given a queue, this method returns the first pending message, waiting if necessary for the specified timeout
     *
     * @param messages the queue to listen for new messages
     * @param timeout the maximum timeout
     * @param unit the measurement unit of the timeout
     * @return the first message removed from the queue
     * @throws InterruptedException if the thread is forced to stop
     * @throws TimeoutException if the timeout has been reached without receiving any message
     */
    private Message dequeue(BlockingQueue<Message> messages, int timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        Message message = messages.poll(timeout, unit);
        if (message != null) {
            return message;
        } else throw new TimeoutException("Nothing received");
    }

    /**
     * Dequeue an event message
     *
     * @param timeout the maximum timeout
     * @param unit the measurement unit of the timeout
     * @return the first message removed from the event queue
     * @throws InterruptedException if the thread is forced to stop
     * @throws TimeoutException if the timeout has been reached without receiving any message
     */
    public Message dequeueEvent(int timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        return dequeue(eventMessageQueues, timeout, unit);
    }

    /**
     * Dequeue a question message
     *
     * @param timeout the maximum timeout
     * @param unit the measurement unit of the timeout
     * @return the first message removed from the question queue
     * @throws InterruptedException if the thread is forced to stop
     * @throws TimeoutException if the timeout has been reached without receiving any message
     */
    public Message dequeueQuestion(int timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        return dequeue(questionMessageQueues, timeout, unit);
    }

    /**
     * Dequeue an answer message
     *
     * @param sessionId the identifier of the Question-Answer flow
     * @param timeout the maximum timeout
     * @param unit the measurement unit of the timeout
     * @return the first message removed from the appropriate answer queue
     * @throws InterruptedException if the thread is forced to stop
     * @throws TimeoutException if the timeout has been reached without receiving any message
     */
    public Message dequeueAnswer(String sessionId, int timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        try {
            if (!answerMessageQueues.containsKey(sessionId)) {
                answerMessageQueues.put(sessionId, new LinkedBlockingQueue<>());
            }

            return dequeue(answerMessageQueues.get(sessionId), timeout, unit);
        } finally {
            if (answerMessageQueues.get(sessionId).isEmpty()) {
                answerMessageQueues.remove(sessionId);
            }
        }
    }
}
