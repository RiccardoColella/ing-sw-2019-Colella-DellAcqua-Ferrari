package it.polimi.ingsw.shared;

import it.polimi.ingsw.shared.messages.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class InputMessageQueue {

    private BlockingQueue<Message> questionMessageQueues = new LinkedBlockingQueue<>();
    private BlockingQueue<Message> eventMessageQueues = new LinkedBlockingQueue<>();
    private Map<String, BlockingQueue<Message>> answerMessageQueues = new HashMap<>();


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

    private Message dequeue(BlockingQueue<Message> messages, int timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        Message message = messages.poll(timeout, unit);
        if (message != null) {
            return message;
        } else throw new TimeoutException("Nothing received");
    }

    public Message dequeueEvent(int timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        return dequeue(eventMessageQueues, timeout, unit);
    }

    public Message dequeueQuestion(int timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        return dequeue(questionMessageQueues, timeout, unit);
    }

    public Message dequeueAnswer(String sessionId, int timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        try {
            return dequeue(getAnswerQueue(sessionId), timeout, unit);
        } finally {
            if (answerMessageQueues.get(sessionId).isEmpty()) {
                answerMessageQueues.remove(sessionId);
            }
        }
    }

    private BlockingQueue<Message> getAnswerQueue(String sessionId) {
        if (!answerMessageQueues.containsKey(sessionId)) {
            answerMessageQueues.put(sessionId, new LinkedBlockingQueue<>());
        }

        return answerMessageQueues.get(sessionId);
    }

}
