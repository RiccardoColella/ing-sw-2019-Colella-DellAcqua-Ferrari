package it.polimi.ingsw.shared;

import it.polimi.ingsw.shared.messages.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

    public Message dequeueEvent() throws InterruptedException {
        return eventMessageQueues.take();
    }

    public Message dequeueQuestion() throws InterruptedException {
        return questionMessageQueues.take();
    }

    public Message dequeueAnswer(String sessionId) throws InterruptedException {
        if (!answerMessageQueues.containsKey(sessionId)) {
            answerMessageQueues.put(sessionId, new LinkedBlockingQueue<>());
        }
        Message response = answerMessageQueues.get(sessionId).take();
        if (answerMessageQueues.get(sessionId).isEmpty()) {
            answerMessageQueues.remove(sessionId);
        }

        return response;
    }

}
