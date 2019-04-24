package it.polimi.ingsw.shared.view.remote;

import com.google.gson.Gson;
import it.polimi.ingsw.shared.InputMessageQueue;
import it.polimi.ingsw.shared.messages.Message;
import it.polimi.ingsw.utils.io.UncheckedInputStream;
import it.polimi.ingsw.utils.io.UncheckedOutputStream;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SocketMessageManager implements AutoCloseable {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private Gson gson = new Gson();
    private UncheckedOutputStream outputStream;
    private UncheckedInputStream inputStream;
    private InputMessageQueue inputMessageQueue;
    private BlockingQueue<Message> outputMessageQueue;
    private ExecutorService threadPool = Executors.newFixedThreadPool(2);

    public SocketMessageManager(Socket socket, InputMessageQueue inputMessageQueue, BlockingQueue<Message> outputMessageQueue) throws IOException {
        this.inputMessageQueue = inputMessageQueue;
        this.outputMessageQueue = outputMessageQueue;

        outputStream = new UncheckedOutputStream(socket.getOutputStream());
        inputStream = new UncheckedInputStream(socket.getInputStream());

        threadPool.execute(this::receiveMessageAsync);
        threadPool.execute(this::sendMessageAsync);
    }



    private void receiveMessageAsync() {

        inputMessageQueue.enqueue(
                Message.fromJson(
                        new String(
                                inputStream.readBytes(inputStream.readInt()),
                                StandardCharsets.UTF_8
                        )
                )
        );

        if (!threadPool.isShutdown()) {
            threadPool.execute(this::receiveMessageAsync);
        }
    }

    private void sendMessageAsync() {
        try {
            // Fixed charset shared with the client configuration to prevent incompatibility that
            // can be caused by different defaults
            byte[] content = gson.toJson(outputMessageQueue.take()).getBytes(StandardCharsets.UTF_8);
            outputStream.writeInt(content.length);
            outputStream.writeBytes(content);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.warning("Thread interrupted " + ex.toString());
        }

        if (!threadPool.isShutdown()) {
            threadPool.execute(this::sendMessageAsync);
        }
    }

    @Override
    public void close() throws Exception {
        threadPool.shutdown();
        while (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
            logger.warning("Thread pool did not shutdown yet, waiting...");
        }
    }
}
