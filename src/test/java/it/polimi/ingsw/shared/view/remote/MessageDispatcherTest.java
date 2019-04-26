package it.polimi.ingsw.shared.view.remote;

import it.polimi.ingsw.shared.InputMessageQueue;
import it.polimi.ingsw.shared.messages.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * @author Carlo Dell'Acqua
 */
class MessageDispatcherTest {

    private InputMessageQueue inputQueue;
    private BlockingQueue<Message> outputQueue;
    private MessageDispatcher dispatcher;
    private Message testMessage;


    @BeforeEach
    void init() {
        inputQueue = new InputMessageQueue();
        outputQueue = new LinkedBlockingQueue<>();
        testMessage = Message.createEvent("Test", new Object());
        dispatcher = new MessageDispatcher(inputQueue, outputQueue, (timeout, unit) -> testMessage, (message) -> {});
    }

    @Test
    void dispatch() {
        // TODO: Test
    }

    @Test
    void close() {
        assertDoesNotThrow(() -> dispatcher.close(), "Unable to correctly close the dispatcher");
    }
}