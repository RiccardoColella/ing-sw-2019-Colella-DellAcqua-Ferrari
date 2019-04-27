package it.polimi.ingsw.client.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.client.io.Connector;
import it.polimi.ingsw.shared.events.MessageReceived;
import it.polimi.ingsw.shared.events.listeners.EventMessageReceivedListener;
import it.polimi.ingsw.shared.events.listeners.QuestionMessageReceivedListener;
import it.polimi.ingsw.shared.messages.ClientApi;
import it.polimi.ingsw.shared.messages.Message;
import it.polimi.ingsw.shared.messages.Question;
import it.polimi.ingsw.shared.messages.ServerApi;
import it.polimi.ingsw.utils.EnumValueByString;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * This class represents a command line implementation of the user interface of the game
 *
 * @author Carlo Dell'Acqua
 */
public class CLI implements EventMessageReceivedListener, QuestionMessageReceivedListener {
    /**
     * JSON conversion utility
     */
    private static final Gson gson = new Gson();

    /**
     * The mediator between the client-side View and the server-side View
     */
    private final Connector connector;

    /**
     * Scanner of the System.in user input
     */
    private final Scanner scanner;

    /**
     * Printer for writing to System.out
     */
    private final PrintStream printStream;

    /**
     * Constructs a UI based on the command line
     *
     * @param connector a concrete connector implementation
     * @param inputStream a stream used to retrieve user input data
     * @param outputStream a stream used to write output data
     */
    public CLI(Connector connector, InputStream inputStream, OutputStream outputStream) {
        this.connector = connector;
        scanner = new Scanner(inputStream);
        printStream = new PrintStream(outputStream);
    }

    /**
     * Manages events
     *
     * @param e the MessageReceived event
     */
    @Override
    public void onEventMessageReceived(MessageReceived e) {

        ClientApi eventType = EnumValueByString.findByString(e.getMessage().getName(), ClientApi.class);

        // TODO: Manage events
        switch (eventType) {
            
        }

    }

    /**
     * Manages questions
     *
     * @param e the MessageReceived event
     */
    @Override
    public void onQuestionMessageReceived(MessageReceived e) {
        manageQuestion(e.getMessage());
    }

    /**
     * Given a question message it shows it to the user and ask for a selection. Once
     * a valid selection has been made by the user, the answer is forwarded through the connector
     *
     * @param message a question message
     */
    private void manageQuestion(Message message) {
        Question question = gson.fromJson(message.getPayload(), new TypeToken<Question>(){}.getType());
        Object[] options = question.getAvailableOptions().toArray();

        int chosenIndex;
        do {
            printStream.println(question.getText());
            if (question.isSkippable()) {
                printStream.println("0) Skip");
            }
            for (int i = 0; i < options.length; i++) {
                printStream.println(String.format("%d) %s", (i + 1), options[i].toString()));
            }
            chosenIndex = Integer.parseInt(scanner.nextLine());
        } while (!((question.isSkippable() ? 0 : 1) <= chosenIndex && chosenIndex <= options.length));
        connector.sendMessage(Message.createAnswer(ServerApi.ANSWER.toString(), chosenIndex, message.getStreamId()));
    }
}
