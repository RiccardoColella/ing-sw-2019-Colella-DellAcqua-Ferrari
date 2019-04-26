package it.polimi.ingsw.shared;

import it.polimi.ingsw.shared.messages.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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

    public Message dequeueEvent(int timeout, TimeUnit unit) throws InterruptedException {
        return eventMessageQueues.poll(timeout, unit);
    }

    public Message dequeueQuestion(int timeout, TimeUnit unit) throws InterruptedException {
        return questionMessageQueues.poll(timeout, unit);
    }

    public Message dequeueAnswer(String sessionId, int timeout, TimeUnit unit) throws InterruptedException {
        Message response = getAnswerQueue(sessionId).poll(timeout, unit);
        if (answerMessageQueues.get(sessionId).isEmpty()) {
            answerMessageQueues.remove(sessionId);
        }

        return response;
    }

    public Message dequeueEvent() throws InterruptedException {
        return eventMessageQueues.take();
    }

    public Message dequeueQuestion() throws InterruptedException {
        return questionMessageQueues.take();
    }

    public Message dequeueAnswer(String sessionId) throws InterruptedException {
        Message response = getAnswerQueue(sessionId).take();
        if (answerMessageQueues.get(sessionId).isEmpty()) {
            answerMessageQueues.remove(sessionId);
        }

        return response;
    }

    private BlockingQueue<Message> getAnswerQueue(String sessionId) {
        if (!answerMessageQueues.containsKey(sessionId)) {
            answerMessageQueues.put(sessionId, new LinkedBlockingQueue<>());
        }

        return answerMessageQueues.get(sessionId);
    }

}
