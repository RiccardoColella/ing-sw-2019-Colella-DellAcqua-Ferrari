package it.polimi.ingsw.client.ui.cli;

import com.google.gson.*;
import it.polimi.ingsw.client.io.Connector;
import it.polimi.ingsw.client.io.RMIConnector;
import it.polimi.ingsw.client.io.SocketConnector;
import it.polimi.ingsw.client.io.listeners.*;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.BasicAction;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.shared.bootstrap.ClientInitializationInfo;
import it.polimi.ingsw.shared.events.networkevents.*;
import it.polimi.ingsw.shared.messages.templates.Question;
import it.polimi.ingsw.shared.datatransferobjects.Powerup;
import it.polimi.ingsw.utils.ConfigFileMaker;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This class represents a command line implementation of the user interface of the game
 *
 * @author Carlo Dell'Acqua
 */
public class CLI implements AutoCloseable, QuestionMessageReceivedListener, BoardListener, MatchListener, PlayerListener, ClientListener, DuplicatedNicknameListener {
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

    private String nickname;
    private String serverAddress;
    private String connectionType;
    private BoardFactory.Preset preset;
    private Integer skulls;
    private Match.Mode mode;

    private boolean matchOnGoing = false;


    /**
     * This variable stores and knows how to represent the game situation
     */
    private GameRepresentation gameRepresentation;

    private static final String TEXTS_JSON_PATH = "./config/gameTextsForCLI.json";
    private static final String TEXTS_JSON_PATH_RES = "/config/gameTextsForCLI.json";
    private final String w;
    private final String m;
    private final String q;
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

        jsonElement = new JsonParser().parse(ConfigFileMaker.load(TEXTS_JSON_PATH, TEXTS_JSON_PATH_RES));

        JsonObject jsonObject =jsonElement.getAsJsonObject();
        this.m = jsonObject.get("message").toString().replace("\"", "");
        this.w = jsonObject.get("warning").toString().replace("\"", "");
        this.q = jsonObject.get("question").toString().replace("\"", "");

        JsonArray gameTitle = jsonObject.get("gameTitle").getAsJsonArray();
        for (JsonElement line : gameTitle){
            stringBuilder.append(line.getAsString());
        }
        this.title = stringBuilder.toString();

        scanner = new Scanner(inputStream);
        printStream = new PrintStream(outputStream);

    }

    /**
     * Initializes the CLI with the needed settings asking the user for his preferences
     */
    public void initialize() {

        List<String> availableConnectionOptions = Arrays.asList("RMI", "Socket");
        List<Integer> availableSkulls = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);

        printStream.print(title);
        printStream.println("Enter the server address");
        serverAddress = scanner.nextLine();

        connectionType = askForSelection(
                "Choose the connection type you'd like to use",
                availableConnectionOptions,
                false
        );

        printStream.println("Enter a nickname");
        this.nickname = scanner.nextLine();

        preset = askForSelection(
                "Choose a board preset",
                Arrays.asList(BoardFactory.Preset.values()),
                false
        );
        skulls = askForSelection(
                "Choose a number of skulls",
                availableSkulls,
                false
        );
        if (skulls == null) {
            throw new IllegalStateException("The user had to choose a number of skulls, null returned");
        }

        List<Match.Mode> modes = new LinkedList<>(Arrays.asList(Match.Mode.values()));
        modes.remove(Match.Mode.FINAL_FRENZY);
        mode = askForSelection(
                "Choose a match mode",
                modes,
                false
        );
        if (connectionType != null) {
            setConnector(serverAddress, connectionType, preset, skulls, mode);
        } else throw new IllegalStateException("The user had to choose between Socket or RMI, null returned");


        connector.addQuestionMessageReceivedListener(this);
    }

    private void setConnector(String serverAddress, String connectionType, BoardFactory.Preset preset, int skulls, Match.Mode mode){
        try {
            switch (connectionType) {
                case "RMI":
                    connector = new RMIConnector();
                    addAllListeners();
                    ((RMIConnector) connector).initialize(new ClientInitializationInfo(nickname, preset, skulls, mode), new InetSocketAddress(serverAddress, 9090));
                    break;
                case "Socket":
                    connector = new SocketConnector();
                    addAllListeners();
                    ((SocketConnector) connector).initialize(new ClientInitializationInfo(nickname, preset, skulls, mode), new InetSocketAddress(serverAddress, 9000));
                    break;
                default:
                    throw new IllegalStateException("The user had to choose between Socket or RMI, unrecognized option " + connectionType);
            }
        } catch (Exception ex) {
            printStream.println("Unhandled exception in connector initializing...");
        }
    }

    private void addAllListeners(){
        connector.addMatchListener(this);
        connector.addDuplicatedNicknameListener(this);
        connector.addClientListener(this);
        connector.addQuestionMessageReceivedListener(this);
        connector.addBoardListener(this);
        connector.addPlayerListener(this);
        connector.startListeningToQuestions();
    }

    private void removeAllListeners(){
        connector.removeMatchListener(this);
        connector.removeDuplicatedNicknameListener(this);
        connector.removeClientListener(this);
        connector.removeQuestionMessageReceivedListener(this);
        connector.removeBoardListener(this);
        connector.removePlayerListener(this);
    }

    public void setMatchOnGoing(boolean value) {
        this.matchOnGoing = value;
    }

    public boolean isMatchOnGoing() {
        return matchOnGoing;
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
        String answer = "";
        do {
            printStream.println(questionText);
            if (skippable) {
                printStream.println("0) Skip");
            }
            for (int i = 0; i < options.size(); i++) {
                String option = ANSIColor.parseColor(options.get(i).toString());
                printStream.println(String.format("%d) %s", (i + 1), option));
            }
            try {
                answer = scanner.nextLine();
                chosenIndex = Integer.parseInt(answer);
            } catch (Exception ex) {
                chosenIndex = -1;
                switch (answer) {
                    case "all":
                        gameRepresentation.showUpdatedSituation(printStream);
                        break;
                    case "map":
                        gameRepresentation.showUpdatedMap(printStream);
                        break;
                    case "wallets":
                        gameRepresentation.showPlayersInfo(printStream);
                        break;
                    case "bonus":
                        gameRepresentation.showBonusMap(printStream);
                        break;
                    default:
                        printStream.println(w + "Please, insert a valid number!");
                        printStream.println("Write 'all', 'map', 'bonus' or 'wallets' to get info about the state of the game");
                        break;
                }
            }
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
        printStream.println(q + "Direction question");
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    @Override
    public void onAttackQuestion(Question<String> question, Consumer<String> answerCallback) {
        printStream.println(q + "Attack question");
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    @Override
    public void onBasicActionQuestion(Question<BasicAction> question, Consumer<BasicAction> answerCallback) {
        printStream.println(q + "Basic action question");
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    @Override
    public void onBlockQuestion(Question<Point> question, Consumer<Point> answerCallback) {
        printStream.println(q + "Block question");
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    @Override
    public void onPaymentMethodQuestion(Question<String> question, Consumer<String> answerCallback) {
        printStream.println(q + "Payment method question");
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    @Override
    public void onPowerupQuestion(Question<Powerup> question, Consumer<Powerup> answerCallback) {
        printStream.println(q + "Powerup question");
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    @Override
    public void onWeaponQuestion(Question<String> question, Consumer<String> answerCallback) {
        printStream.println(q + "Weapon question");
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    @Override
    public void onReloadQuestion(Question<String> question, Consumer<String> answerCallback) {
        printStream.println(q + "Reload question");
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    @Override
    public void onSpawnpointQuestion(Question<Powerup> question, Consumer<Powerup> answerCallback) {
        printStream.println(q + "Spawnpoint question");
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    @Override
    public void onTargetQuestion(Question<String> question, Consumer<String> answerCallback) {
        printStream.println(q + "Target question");
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    @Override
    public void onTargetSetQuestion(Question<Set<String>> question, Consumer<Set<String>> answerCallback) {
        printStream.println(q + "Target set question");
        answerCallback.accept(
                askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );
    }

    @Override
    public void onPaymentColorQuestion(Question<CurrencyColor> question, Consumer<CurrencyColor> answerCallback) {
        printStream.println(q + "Payment color question");
        answerCallback.accept(
                askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    @Override
    public void onMatchStarted(MatchStarted e) {
        gameRepresentation = new GameRepresentation(e);
        setMatchOnGoing(true);
        printStream.println(m + "Match started!");
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
        setMatchOnGoing(false);
        printStream.println(m + "Match ended!");
        printStream.println(m + "Your score is: " + e.getScore(nickname));
        String results = e.getRankings()
                .entrySet()
                .stream()
                .map(placement -> placement.getKey() + " - " + placement.getValue()
                        .stream()
                        .map(player -> player.getNickname() + " " + e.getScore(player.getNickname()) + "pt.")
                        .collect(Collectors.joining(",", " ", " "))
                        )
                .collect(Collectors.joining("\n"));
        printStream.println(results);
    }

    @Override
    public void onMatchResumed(MatchResumed e) {
        // TODO: implement

    }

    @Override
    public void onPlayerMoved(PlayerMoved e) {
        gameRepresentation.movePlayer(e.getPlayer(), e.getRow(), e.getColumn());
        if (!e.getPlayer().getNickname().equals(nickname)){
            printStream.println(m + e.getPlayer().getNickname() + " moved to block in row " + e.getRow() + " and column " + e.getColumn());
        }
    }

    @Override
    public void onPlayerTeleported(PlayerMoved e) {
        gameRepresentation.movePlayer(e.getPlayer(), e.getRow(), e.getColumn());
        if (!e.getPlayer().getNickname().equals(nickname)) {
            printStream.println(m + e.getPlayer().getNickname() + " teleported to block in row " + e.getRow() + " and column " + e.getColumn());
        }
    }

    @Override
    public void onNewWeaponAvailable(WeaponEvent e) {
        if (gameRepresentation != null){
            printStream.println(m + "Weapon " + e.getWeaponName() + " dropped on spawnpoint " + gameRepresentation.addWeaponToSpawnpoint(e.getWeaponName(), e.getRow(), e.getColumn()));
        }
    }

    @Override
    public void onBonusTileGrabbed(BonusTileEvent e) {
        gameRepresentation.removeBonusFromMap(e.getBonusTile());
    }

    @Override
    public void onBonusTileDropped(BonusTileEvent e) {
        gameRepresentation.addBonusToMap(e.getBonusTile());
    }

    @Override
    public void onPlayerDied(PlayerEvent e) {
        gameRepresentation.updatePlayer(e.getPlayer());
        gameRepresentation.setPlayerDied(e.getPlayer());
        if (!e.getPlayer().getNickname().equals(nickname)) {
            printStream.println(m + "Player " + e.getPlayer().getNickname() + " died!");
        } else {
            printStream.println(m + "You died!");
        }
    }

    @Override
    public void onPlayerReborn(PlayerEvent e) {
        gameRepresentation.updatePlayer(e.getPlayer());
        gameRepresentation.setPlayerAlive(e.getPlayer());
        if (!e.getPlayer().getNickname().equals(nickname)) {
            printStream.println(m + "Player " + e.getPlayer().getNickname() + " reborn!");
        } else {
            printStream.println(m + "You reborn!");
        }
    }

    @Override
    public void onPlayerWalletChanged(PlayerWalletChanged e) {
        gameRepresentation.updatePlayer(e.getPlayer());
        if (!e.getPlayer().getNickname().equals(nickname)) {
            printStream.println(w + e.getPlayer().getNickname() + "'s wallet changed!");
            printStream.println(m + e.getMessage());
        }
    }

    @Override
    public void onPlayerBoardFlipped(PlayerEvent e) {
        if (e.getPlayer().getNickname().equals(nickname)){
            printStream.println(w + "Your board just flipped!");
        }
        printStream.println(w + e.getPlayer().getNickname() + "'s board flipped");
    }

    @Override
    public void onPlayerTileFlipped(PlayerEvent e) {
        gameRepresentation.updatePlayer(e.getPlayer());
        if (e.getPlayer().getNickname().equals(nickname)){
            printStream.println(w + "Your tile just flipped!");
        }
        printStream.println(w + e.getPlayer().getNickname() + "'s tile flipped");
    }

    @Override
    public void onPlayerHealthChanged(PlayerHealthChanged e) {
        gameRepresentation.updatePlayer(e.getPlayer());
        if (!e.getPlayer().getNickname().equals(nickname)) {
            printStream.println(m + e.getPlayer().getNickname() + "'s health changed");
        } else{
            printStream.println(m + "Your health changed!");
        }
    }

    @Override
    public void onWeaponReloaded(PlayerWeaponEvent e) {
        gameRepresentation.updatePlayer(e.getPlayer());
        if (!e.getPlayer().getNickname().equals(nickname)) {
            printStream.println(m + e.getPlayer().getNickname() + " reloaded his " + e.getWeaponName());
        }
    }

    @Override
    public void onWeaponUnloaded(PlayerWeaponEvent e) {
        gameRepresentation.updatePlayer(e.getPlayer());
        if (!e.getPlayer().getNickname().equals(nickname)) {
            printStream.println(m + e.getPlayer().getNickname() + " now has his " + e.getWeaponName() + " unloaded");
        }
    }

    @Override
    public void onWeaponPicked(PlayerWeaponExchanged e) {
        gameRepresentation.grabPlayerWeapon(e.getPlayer(), e.getWeaponName(), e.getRow(), e.getColumn());
        if (!e.getPlayer().getNickname().equals(nickname)) {
            printStream.println(m + e.getPlayer().getNickname() + " picked up " + e.getWeaponName() + " on block Row" + e.getRow() + " Column" + e.getColumn());
        }
    }

    @Override
    public void onWeaponDropped(PlayerWeaponExchanged e) {
        gameRepresentation.dropPlayerWeapon(e.getPlayer(), e.getWeaponName(), e.getRow(), e.getColumn());
        if (!e.getPlayer().getNickname().equals(nickname)) {
            printStream.println(m + e.getPlayer().getNickname() + " dropped his " + e.getWeaponName() + " on Row" + e.getRow() + " Column" + e.getColumn());
        }
    }

    @Override
    public void onLoginSuccess(ClientEvent e) {
        if (e.getNickname().equals(this.nickname)){
            printStream.println("LOGIN SUCCESSFUL! Your nickname is: " + e.getNickname());
        } else printStream.println(e.getNickname() + " connected to the game");
    }

    @Override
    public void onClientDisconnected(ClientEvent e) {
        printStream.println(w + e.getNickname() + " disconnected");
    }

    @Override
    public void onPlayerReconnected(PlayerEvent e) {
        printStream.println(w + e.getPlayer().getNickname() + " reconnected");
    }

    @Override
    public void onPlayerSpawned(PlayerSpawned e) {
        gameRepresentation.setPlayerAlive(e.getPlayer());
        gameRepresentation.movePlayer(e.getPlayer(), e.getRow(), e.getColumn());
        if (!e.getPlayer().getNickname().equals(nickname)) {
            printStream.println(m + e.getPlayer().getNickname() + " spawned on Row: " + e.getRow() + " and Column: " + e.getColumn());
        }
    }

    @Override
    public void onPlayerOverkilled(PlayerEvent e) {
        if (e.getPlayer().getNickname().equals(nickname)){
            printStream.println(w + "You have been overkilled!");
        } else printStream.println(w + e.getPlayer().getNickname() + " has been overkilled");
    }

    @Override
    public void onActivePlayerChanged(PlayerEvent e) {
        if (isMatchOnGoing()) {
            printStream.println(m + "Now it's " + e.getPlayer().getNickname() + " turn!");
            gameRepresentation.showUpdatedMap(printStream);
        } else printStream.println(m + "Game over!");
    }

    @Override
    public void onDuplicatedNickname() {
        printStream.println("Nickname " + nickname + " not available. Choose another nick!");
        removeAllListeners();

        new Thread(() -> {
            try {
                connector.close();
            } catch (Exception ex) {
                printStream.println("Could not close the connector");
            }
        }).start();

        printStream.println("Enter another nickname");
        this.nickname = scanner.nextLine();

        setConnector(serverAddress, connectionType, preset, skulls, mode);
    }
}
