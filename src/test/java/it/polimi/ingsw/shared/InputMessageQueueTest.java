package it.polimi.ingsw.shared;

import it.polimi.ingsw.shared.messages.Message;
import it.polimi.ingsw.shared.messages.templates.Question;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;


class InputMessageQueueTest {

    @Test
    void test() {
        InputMessageQueue queue = new InputMessageQueue();
        try {
            Message event = Message.createEvent("test", new Object());
            queue.enqueue(event);
            assertEquals(event, queue.dequeueEvent(1, TimeUnit.SECONDS), "Enqueued event does not match the expected instance");
            assertThrows(TimeoutException.class, () -> queue.dequeueEvent(100, TimeUnit.MILLISECONDS), "Exception should have been thrown, the queue should be empty");
            Message question = Message.createQuestion("test", new Question<>("test", Arrays.asList(1, 2, 3)));
            queue.enqueue(question);
            assertEquals(question, queue.dequeueQuestion(1, TimeUnit.SECONDS), "Enqueued question does not match the expected instance");
            Message answer = Message.createAnswer("test", 1, question.getFlowId());
            queue.enqueue(answer);
            assertEquals(answer, queue.dequeueAnswer(question.getFlowId(), 1, TimeUnit.SECONDS), "Expected the previously created answer");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
            fail("Unable to dequeue");
        } catch (TimeoutException e) {
            e.printStackTrace();
            fail("Unable to dequeue");
        }
    }
}