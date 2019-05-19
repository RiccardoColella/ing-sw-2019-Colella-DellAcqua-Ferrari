package it.polimi.ingsw.client.ui.cli;

import com.google.gson.*;
import it.polimi.ingsw.client.io.Connector;
import it.polimi.ingsw.client.io.RMIConnector;
import it.polimi.ingsw.client.io.SocketConnector;
import it.polimi.ingsw.client.io.listeners.BoardListener;
import it.polimi.ingsw.client.io.listeners.MatchListener;
import it.polimi.ingsw.client.io.listeners.PlayerListener;
import it.polimi.ingsw.client.io.listeners.QuestionMessageReceivedListener;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.exceptions.MissingConfigurationFileException;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.BasicAction;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.shared.bootstrap.ClientInitializationInfo;
import it.polimi.ingsw.shared.events.networkevents.*;
import it.polimi.ingsw.shared.messages.templates.Question;
import it.polimi.ingsw.shared.datatransferobjects.Powerup;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.*;
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
public class CLI implements QuestionMessageReceivedListener, AutoCloseable, MatchListener, BoardListener, PlayerListener {
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
     * This variable stores and knows how to represent the game situation
     */
    private GameRepresentation gameRepresentation;

    private static final String  TEXTS_JSON_FILE = "./resources/gameTextsForCLI.json";
    private final String w;
    private final String m;
    private final String title;

    /**
     * Constructs a UI based on the command line
     *
     * @param inputStream a stream used to retrieve user input data
     * @param outputStream a stream used to write output data
     */
    public CLI(InputStream inputStream, OutputStream outputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        JsonElement jsonElement;
        try {
            jsonElement = new JsonParser().parse(new FileReader(new File(TEXTS_JSON_FILE)));
        } catch (IOException e) {
            throw new MissingConfigurationFileException("Unable to read texts configuration file");
        }
        JsonObject jsonObject =jsonElement.getAsJsonObject();
        this.m = jsonObject.get("message").toString();
        this.w = jsonObject.get("warning").toString();
        JsonArray title = jsonObject.get("gameTitle").getAsJsonArray();
        for (JsonElement line : title){
            stringBuilder.append(line.getAsString());
        }
        this.title = stringBuilder.toString();

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

        printStream.print(title);
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

    @Override
    public void onTargetSetQuestion(Question<Set<String>> question, Consumer<Set<String>> answerCallback) {
        // TODO: Implement the selection
    }

    @Override
    public void onPaymentColorQuestion(Question<CurrencyColor> question, Consumer<CurrencyColor> answerCallback) {

    }

    @Override
    public void onMatchStarted(MatchStarted e) {
        gameRepresentation = new GameRepresentation(e);
    }

    @Override
    public void onMatchModeChanged(MatchModeChanged e) {
        printStream.print(w + "Match mode changed! It now is: " + e.getMode().toString());
    }

    @Override
    public void onKillshotTrackChanged(KillshotTrackChanged e) {
        // TODO: implement

    }

    @Override
    public void onMatchEnded(MatchEnded e) {
        // TODO: implement

    }

    @Override
    public void onPlayerMoved(PlayerMoved e) {
        gameRepresentation.movePlayer(e.getPlayer(), e.getRow(), e.getColumn());
    }

    @Override
    public void onPlayerTeleported(PlayerMoved e) {
        gameRepresentation.movePlayer(e.getPlayer(), e.getRow(), e.getColumn());
    }

    @Override
    public void onNewWeaponAvailable(WeaponEvent e) {

    }

    @Override
    public void onPlayerDied(PlayerEvent e) {
        // TODO: implement

    }

    @Override
    public void onPlayerReborn(PlayerEvent e) {
        // TODO: implement

    }

    @Override
    public void onPlayerWalletChanged(PlayerWalletChanged e) {
        gameRepresentation.updatePlayer(e.getPlayer());
        printStream.println(w + e.getPlayer().getNickname() + "'s wallet changed!");
        printStream.println(m + e.getMessage());
    }

    @Override
    public void onPlayerBoardFlipped(PlayerEvent e) {
        // TODO: implement

    }

    @Override
    public void onPlayerTileFlipped(PlayerEvent e) {
        // TODO: implement

    }

    @Override
    public void onPlayerHealthChanged(PlayerHealthChanged e) {
        gameRepresentation.updatePlayer(e.getPlayer());
        printStream.println(m + e.getPlayer().getNickname() + "'s health changed");
    }

    @Override
    public void onWeaponReloaded(PlayerWeaponEvent e) {
        gameRepresentation.updatePlayer(e.getPlayer());
        printStream.println(m + e.getPlayer().getNickname() + " reloaded his " + e.getWeaponName());
    }

    @Override
    public void onWeaponUnloaded(PlayerWeaponEvent e) {
        gameRepresentation.updatePlayer(e.getPlayer());
        printStream.println(m + e.getPlayer().getNickname() + " unloaded his " + e.getWeaponName());
    }

    @Override
    public void onWeaponPicked(PlayerWeaponExchanged e) {
        gameRepresentation.grabPlayerWeapon(e.getPlayer(), e.getWeaponName(), e.getRow(), e.getColumn());
        printStream.println(m + e.getPlayer().getNickname() + " picked up " + e.getWeaponName() + " on block Row" + e.getRow() + " Column" + e.getColumn());
    }

    @Override
    public void onWeaponDropped(PlayerWeaponExchanged e) {
        gameRepresentation.dropPlayerWeapon(e.getPlayer(), e.getWeaponName(), e.getRow(), e.getColumn());
        printStream.println(m + e.getPlayer().getNickname() + " dropped his " + e.getWeaponName() + " on Row" + e.getRow() + " Column" + e.getColumn());
    }

    @Override
    public void onPlayerDisconnected(PlayerEvent e) {
        printStream.println(w + e.getPlayer().getNickname() + "disconnected");
    }

    @Override
    public void onPlayerReconnected(PlayerEvent e) {
        printStream.println(w + e.getPlayer().getNickname() + " reconnected");
    }

    @Override
    public void onPlayerSpawned(PlayerSpawned e) {
        gameRepresentation.movePlayer(e.getPlayer(), e.getRow(), e.getColumn());
        printStream.println(w + e.getPlayer().getNickname() + " respowned on Row" + e.getRow() + " Column" + e.getColumn());
    }

    @Override
    public void onPlayerOverkilled(PlayerEvent e) {
        // TODO: implement

    }

    @Override
    public void onActivePlayerChanged(PlayerEvent e) {
        gameRepresentation.showUpdatedSituation(printStream);
        printStream.println(m + "Now it's " + e.getPlayer().getNickname() + " turn!");

    }
}
