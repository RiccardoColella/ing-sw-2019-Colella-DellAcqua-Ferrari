package it.polimi.ingsw.client.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.client.io.Connector;
import it.polimi.ingsw.client.io.RMIConnector;
import it.polimi.ingsw.client.io.SocketConnector;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.shared.bootstrap.ClientInitializationInfo;
import it.polimi.ingsw.shared.events.MessageReceived;
import it.polimi.ingsw.client.io.listeners.QuestionMessageReceivedListener;
import it.polimi.ingsw.shared.messages.Message;
import it.polimi.ingsw.shared.messages.templates.Question;
import it.polimi.ingsw.shared.messages.ServerApi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;
import java.util.Scanner;

/**
 * This class represents a command line implementation of the user interface of the game
 *
 * @author Carlo Dell'Acqua
 */
public class CLI implements QuestionMessageReceivedListener, AutoCloseable {
    /**
     * JSON conversion utility
     */
    private static final Gson gson = new Gson();

    /**
     * The mediator between the client-side View and the server-side View
     */
    private Connector connector;

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
     * @param inputStream a stream used to retrieve user input data
     * @param outputStream a stream used to write output data
     */
    public CLI(InputStream inputStream, OutputStream outputStream) {
        scanner = new Scanner(inputStream);
        printStream = new PrintStream(outputStream);
    }

    /**
     * Initializes the CLI with the needed settings asking the user for his preferences
     *
     * @throws IOException if a network error occur
     * @throws NotBoundException if the message proxy was not found in the remote RMI registry
     * @throws InterruptedException if the thread was forced to stop before construction completion
     */
    public void initialize() throws InterruptedException, IOException, NotBoundException {

        String[] availableConnectionOptions = new String[] { "RMI", "Socket" };
        Integer[] availableSkulls = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8 };

        printStream.println("Enter the server address");
        String serverAddress = scanner.nextLine();

        int chosenIndex;
        chosenIndex = askForSelection(
                "Choose the connection type you'd like to use",
                availableConnectionOptions,
                false
        ) - 1;
        String connectionType = availableConnectionOptions[chosenIndex];

        printStream.println("Enter a nickname");
        String nickname = scanner.nextLine();

        chosenIndex = askForSelection(
                "Choose a board preset",
                BoardFactory.Preset.values(),
                false
        ) - 1;
        BoardFactory.Preset preset = BoardFactory.Preset.values()[chosenIndex];
        chosenIndex = askForSelection(
                "Choose a number of skulls",
                availableSkulls,
                false
        );

        int skulls = availableSkulls[chosenIndex];

        chosenIndex = askForSelection(
                "Choose a board preset",
                Match.Mode.values(),
                false
        ) - 1;

        Match.Mode mode = Match.Mode.values()[chosenIndex];

        switch (connectionType) {
            case "RMI":
                connector = new RMIConnector();
                ((RMIConnector) connector).initialize(new ClientInitializationInfo(nickname, preset, skulls, mode), new InetSocketAddress(serverAddress, 9090));
                break;
            case "Socket":
                connector = new SocketConnector();
                ((SocketConnector) connector).initialize(new ClientInitializationInfo(nickname, preset, skulls, mode), new InetSocketAddress(serverAddress, 9000));
                break;
            default:
                throw new IllegalStateException("The user had to choose between Socket or RMI, unrecognized option " + connectionType);
        }

        connector.addQuestionMessageReceivedListener(this);
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

        int chosenIndex = askForSelection(question.getText(), options, question.isSkippable());

        connector.sendMessage(Message.createAnswer(ServerApi.ANSWER, chosenIndex, message.getFlowId()));
    }

    /**
     * Method used for interacting with the user. It asks a question and wait for a valid user selection between the available options
     *
     * @param questionText the text of the question
     * @param options the available options to choose from
     * @param skippable indicates whether or not the answer can be none of the available options
     * @return 0 if the user didn't choose any of the presented options
     *         1..{@code options.length}, corresponding to the option index in the array + 1
     */
    private int askForSelection(String questionText, Object[] options, boolean skippable) {
        int chosenIndex;
        do {
            printStream.println(questionText);
            if (skippable) {
                printStream.println("0) Skip");
            }
            for (int i = 0; i < options.length; i++) {
                printStream.println(String.format("%d) %s", (i + 1), options[i].toString()));
            }
            chosenIndex = Integer.parseInt(scanner.nextLine());
        } while (!((skippable ? 0 : 1) <= chosenIndex && chosenIndex <= options.length));

        return chosenIndex;
    }

    /**
     * Closes this object and the associated connector
     *
     * @throws Exception if an error occurs during the closing operation
     */
    @Override
    public void close() throws Exception {
        if (connector != null) {
            connector.close();
        }
    }
}
