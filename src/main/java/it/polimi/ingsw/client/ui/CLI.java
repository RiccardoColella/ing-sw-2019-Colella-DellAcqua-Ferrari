package it.polimi.ingsw.client.ui;

import com.google.gson.Gson;
import it.polimi.ingsw.client.io.Connector;
import it.polimi.ingsw.client.io.RMIConnector;
import it.polimi.ingsw.client.io.SocketConnector;
import it.polimi.ingsw.client.io.listeners.QuestionMessageReceivedListener;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.BasicAction;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.shared.bootstrap.ClientInitializationInfo;
import it.polimi.ingsw.shared.messages.templates.Question;
import it.polimi.ingsw.shared.viewmodels.Powerup;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

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

        List<String> availableConnectionOptions = Arrays.asList("RMI", "Socket");
        List<Integer> availableSkulls = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);

        printStream.println("Enter the server address");
        String serverAddress = scanner.nextLine();

        String connectionType = askForSelection(
                "Choose the connection type you'd like to use",
                availableConnectionOptions,
                false
        );

        printStream.println("Enter a nickname");
        String nickname = scanner.nextLine();

        BoardFactory.Preset preset = askForSelection(
                "Choose a board preset",
                Arrays.asList(BoardFactory.Preset.values()),
                false
        );
        Integer skulls = askForSelection(
                "Choose a number of skulls",
                availableSkulls,
                false
        );
        if (skulls == null) {
            throw new IllegalStateException("The user had to choose a number of skulls, null returned");
        }

        Match.Mode mode = askForSelection(
                "Choose a board preset",
                Arrays.asList(Match.Mode.values()),
                false
        );

        if (connectionType != null) {
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
        } else {
            throw new IllegalStateException("The user had to choose between Socket or RMI, null returned");
        }

        connector.addQuestionMessageReceivedListener(this);
    }

    /**
     * Method used for interacting with the user. It asks a question and wait for a valid user selection between the available options
     *
     * @param questionText the text of the question
     * @param optionCollection the available options to choose from
     * @param skippable indicates whether or not the answer can be none of the available options
     * @param <T> the type of the options
     * @return the selected option or null if the user decided to skip
     */
    @Nullable
    private <T> T askForSelection(String questionText, Collection<T> optionCollection, boolean skippable) {
        List<T> options = new LinkedList<>(optionCollection);
        int chosenIndex;
        do {
            printStream.println(questionText);
            if (skippable) {
                printStream.println("0) Skip");
            }
            for (int i = 0; i < options.size(); i++) {
                printStream.println(String.format("%d) %s", (i + 1), options.get(i).toString()));
            }
            chosenIndex = Integer.parseInt(scanner.nextLine());
        } while (!((skippable ? 0 : 1) <= chosenIndex && chosenIndex <= options.size()));

        if (chosenIndex == 0) {
            return null;
        } else {
            return options.get(chosenIndex - 1);
        }
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

    @Override
    public void onDirectionQuestion(Question<Direction> question, Consumer<Direction> answerCallback) {

        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    @Override
    public void onAttackQuestion(Question<String> question, Consumer<String> answerCallback) {
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    @Override
    public void onBasicActionQuestion(Question<BasicAction> question, Consumer<BasicAction> answerCallback) {
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    @Override
    public void onBlockQuestion(Question<Point> question, Consumer<Point> answerCallback) {
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    @Override
    public void onPaymentMethodQuestion(Question<String> question, Consumer<String> answerCallback) {
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    @Override
    public void onPowerupQuestion(Question<Powerup> question, Consumer<Powerup> answerCallback) {
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    @Override
    public void onWeaponQuestion(Question<String> question, Consumer<String> answerCallback) {
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    @Override
    public void onReloadQuestion(Question<String> question, Consumer<String> answerCallback) {
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    @Override
    public void onSpawnpointQuestion(Question<Powerup> question, Consumer<Powerup> answerCallback) {
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    @Override
    public void onTargetQuestion(Question<String> question, Consumer<String> answerCallback) {
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }
}
