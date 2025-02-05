package it.polimi.ingsw.client.ui.cli;

import com.google.gson.*;
import it.polimi.ingsw.client.ClientConfig;
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
import it.polimi.ingsw.shared.datatransferobjects.Powerup;
import it.polimi.ingsw.shared.events.networkevents.*;
import it.polimi.ingsw.shared.messages.templates.Question;
import it.polimi.ingsw.utils.ConfigFileMaker;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
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

    /**
     *  This variable stores the nickname sent to the server
     */
    private String nickname;

    /**
     * This variable stores the address of the server
     */
    private String serverAddress;

    /**
     * This variable stores the connection type as a string. May be RMI or Socket
     */
    private String connectionType;

    /**
     * This variable stores the preferred preset sent to the server during login
     */
    private BoardFactory.Preset preset;

    /**
     * This variable stores the preferred number of skulls sent to the server during login
     */
    private Integer skulls;

    /**
     * This variable stores the preferred match mode sent to the server during login
     */
    private Match.Mode mode;

    /**
     * This variable tells the CLI if there is an active play or not. It is useful for different
     * behaviour during execution depending on different situations
     */
    private boolean matchOnGoing = false;

    /**
     * Client configuration information
     */
    private ClientConfig config;


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
     * @param config client configuration information
     * @param inputStream a stream used to retrieve user input data
     * @param outputStream a stream used to write output data
     */
    public CLI(ClientConfig config, InputStream inputStream, OutputStream outputStream) {

        this.config = config;

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
        //If the player didn't insert anything we consider the preference as localhost
        if (serverAddress.equals("")){
            serverAddress = "localhost";
        }

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
        //Final Frienzy is not a valid preferred match mode for the start
        modes.remove(Match.Mode.FINAL_FRENZY);
        mode = askForSelection(
                "Choose a match mode",
                modes,
                false
        );
        if (connectionType != null) {
            setConnector();
        } else throw new IllegalStateException("The user had to choose between Socket or RMI, null returned");

        connector.addQuestionMessageReceivedListener(this);
    }

    /**
     * This method sets up the connector, after having received the preferences
     */
    private void setConnector(){
        try {
            switch (connectionType) {
                case "RMI":
                    connector = new RMIConnector();
                    addAllListeners();
                    ((RMIConnector) connector).initialize(new ClientInitializationInfo(nickname, preset, skulls, mode), new InetSocketAddress(serverAddress, config.getRMIPort()));
                    break;
                case "Socket":
                    connector = new SocketConnector();
                    addAllListeners();
                    ((SocketConnector) connector).initialize(new ClientInitializationInfo(nickname, preset, skulls, mode), new InetSocketAddress(serverAddress, config.getSocketPort()));
                    break;
                default:
                    throw new IllegalStateException("The user had to choose between Socket or RMI, unrecognized option " + connectionType);
            }
        } catch (Exception ex) {
            printStream.println("Unhandled exception in connector initializing...");
        }
    }

    /**
     * This method adds the listeners needed for the game to be played
     */
    private void addAllListeners(){
        connector.addMatchListener(this);
        connector.addDuplicatedNicknameListener(this);
        connector.addClientListener(this);
        connector.addQuestionMessageReceivedListener(this);
        connector.addBoardListener(this);
        connector.addPlayerListener(this);
        connector.startListeningToQuestions();
    }

    /**
     * This method removes all the listeners previously added
     */
    private void removeAllListeners(){
        connector.removeMatchListener(this);
        connector.removeDuplicatedNicknameListener(this);
        connector.removeClientListener(this);
        connector.removeQuestionMessageReceivedListener(this);
        connector.removeBoardListener(this);
        connector.removePlayerListener(this);
    }

    /**
     * This setting method sets the matchOnGoing variable
     * @param value value to be assigned to matchOnGoing
     */
    private void setMatchOnGoing(boolean value) {
        this.matchOnGoing = value;
    }

    /**
     * This methods tells if there is an active play
     * @return true if the play is ongoing
     */
    private boolean isMatchOnGoing() {
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
        int chosenIndex = -1;
        String answer = "";
        do {
            printStream.println(questionText);
            if (skippable) {
                printStream.println("0) Skip");
            }
            for (int i = 0; i < options.size(); i++) {
                String option = ANSIColor.parseString(options.get(i).toString());
                printStream.println(String.format("%d) %s", (i + 1), option));
            }
            try {
                answer = scanner.nextLine();
                chosenIndex = Integer.parseInt(answer);
            } catch (Exception ex) {
                if (matchOnGoing) {
                    manageRepresentationAsks(answer);
                } else printStream.println(w + "Insert a valid number!");
            }
        } while (!((skippable ? 0 : 1) <= chosenIndex && chosenIndex <= options.size()));

        if (chosenIndex == 0) {
            return null;
        } else {
            return options.get(chosenIndex - 1);
        }
    }

    private void manageRepresentationAsks(String answer) {
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

    /**
     * This method is called when the player should choose the direction in which to moove
     * @param question The question sent to the print stream
     * @param answerCallback The possible answers
     */
    @Override
    public void onDirectionQuestion(Question<Direction> question, Consumer<Direction> answerCallback) {
        printStream.println(q + "Direction question");
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    /**
     * This method is called when the player should choose the attack to perform
     * @param question the question sent to the print stream
     * @param answerCallback the possible answers
     */
    @Override
    public void onAttackQuestion(Question<String> question, Consumer<String> answerCallback) {
        printStream.println(q + "Attack question");
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    /**
     * This method is called when the player should choose the action to perform
     * @param question the question sent to the print stream
     * @param answerCallback the possible actions
     */
    @Override
    public void onBasicActionQuestion(Question<BasicAction> question, Consumer<BasicAction> answerCallback) {
        printStream.println(q + "Basic action question");
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    /**
     * This method is called when the player should choose a block to which move
     * @param question the question sent to the print stream
     * @param answerCallback the possible answers
     */
    @Override
    public void onBlockQuestion(Question<Point> question, Consumer<Point> answerCallback) {
        printStream.println(q + "Block question");
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    /**
     * This method is called when the player should choose the payment method
     * @param question the question sent to the print stream
     * @param answerCallback the possible answers
     */
    @Override
    public void onPaymentMethodQuestion(Question<String> question, Consumer<String> answerCallback) {
        printStream.println(q + "Payment method question");
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    /**
     * This method is called when the player should choose the powerup to use
     * @param question the question sent to the print stream
     * @param answerCallback the possible answers
     */
    @Override
    public void onPowerupQuestion(Question<Powerup> question, Consumer<Powerup> answerCallback) {
        printStream.println(q + "Powerup question");
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    /**
     * This method is called when the player should choose a weapon to use
     * @param question the question sent to the print stream
     * @param answerCallback the possible answers
     */
    @Override
    public void onWeaponQuestion(Question<String> question, Consumer<String> answerCallback) {
        printStream.println(q + "Weapon question");
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    /**
     * This method is called when the player should choose which weapon to reload
     * @param question the question sent to the print stream
     * @param answerCallback the possible answers
     */
    @Override
    public void onReloadQuestion(Question<String> question, Consumer<String> answerCallback) {
        printStream.println(q + "Reload question");
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    /**
     * This method is called when the player should choose the powerup to discard to choose the spawnpoint
     * @param question the question sent to the print stream
     * @param answerCallback the possible answers
     */
    @Override
    public void onSpawnpointQuestion(Question<Powerup> question, Consumer<Powerup> answerCallback) {
        printStream.println(q + "Spawnpoint question");
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    /**
     * This method is called when the player should choose the targets to select
     * @param question the question sent to the print stream
     * @param answerCallback the possible answers
     */
    @Override
    public void onTargetQuestion(Question<String> question, Consumer<String> answerCallback) {
        printStream.println(q + "Target question");
        answerCallback.accept(
            askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    /**
     * This method is called when the player should choose a set of targets to select
     * @param question the question sent to the print stream
     * @param answerCallback the possible answers
     */
    @Override
    public void onTargetSetQuestion(Question<Set<String>> question, Consumer<Set<String>> answerCallback) {
        printStream.println(q + "Target set question");
        answerCallback.accept(
                askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );
    }

    /**
     * This method is called when the player should choose the payment method to be used
     * @param question the question sent to the print stream
     * @param answerCallback the possible answers
     */
    @Override
    public void onPaymentColorQuestion(Question<CurrencyColor> question, Consumer<CurrencyColor> answerCallback) {
        printStream.println(q + "Payment color question");
        answerCallback.accept(
                askForSelection(question.getText(), question.getAvailableOptions(), question.isSkippable())
        );

    }

    /**
     * This event is called when the match starts
     * @param e The matchStarted event
     */
    @Override
    public void onMatchStarted(MatchStarted e) {
        gameRepresentation = GameRepresentationFactory.create(e);
        setMatchOnGoing(true);
        printStream.println(m + "Match started!");
    }

    /**
     * This event is called when the match mode changes
     * @param e the matchModeChanged event
     */
    @Override
    public void onMatchModeChanged(MatchModeChanged e) {
        printStream.print(w + "Match mode changed! It now is: " + e.getMode().toString());
    }

    /**
     * This event is called when the kill-shot track has some changes.
     * @param e The killshotTrackChanged event containing the new kill shots track
     */
    @Override
    public void onKillshotTrackChanged(KillshotTrackChanged e) {
        gameRepresentation.setKillshots(e.getKillshots());

    }

    /**
     * This event is called when the match ends. It shows the final rankings
     * @param e the MatchEnded event
     */
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

    /**
     * If this event is called, it handles the resuming of the match
     * @param e Match Resumed event, containing info about the match
     */
    @Override
    public void onMatchResumed(MatchResumed e) {
        gameRepresentation = GameRepresentationFactory.create(e);
        setMatchOnGoing(true);
        gameRepresentation.showUpdatedSituation(printStream);
        printStream.println(m + "Match resumed!");
    }

    /**
     * This event is called when a player on the board moves
     * @param e the PlayerMoved event
     */
    @Override
    public void onPlayerMoved(PlayerMoved e) {
        gameRepresentation.movePlayer(e.getPlayer(), e.getRow(), e.getColumn());
        if (!e.getPlayer().getNickname().equals(nickname)){
            printStream.println(m + e.getPlayer().getNickname() + " moved to block in row " + e.getRow() + " and column " + e.getColumn());
        }
    }

    /**
     * This event is called when a player on the board teleports
     * @param e the PlayerMoved event containing informations about the player who teleported
     */
    @Override
    public void onPlayerTeleported(PlayerMoved e) {
        gameRepresentation.movePlayer(e.getPlayer(), e.getRow(), e.getColumn());
        if (!e.getPlayer().getNickname().equals(nickname)) {
            printStream.println(m + e.getPlayer().getNickname() + " teleported to block in row " + e.getRow() + " and column " + e.getColumn());
        }
    }

    /**
     * This event is called when a new weapon is available on the field
     * @param e the WeaponEvent containing informations about the weapon
     */
    @Override
    public void onNewWeaponAvailable(WeaponEvent e) {
        if (gameRepresentation != null){
            printStream.println(m + "Weapon " + e.getWeaponName() + " dropped on spawnpoint " + gameRepresentation.addWeaponToSpawnpoint(e.getWeaponName(), e.getRow(), e.getColumn()));
        }
    }

    /**
     * This event is called when an ammo or a powerup is grabbed from the field
     * @param e the bonusTileEvent containing info about the grabbed bonus
     */
    @Override
    public void onBonusTileGrabbed(BonusTileEvent e) {
        gameRepresentation.removeBonusFromMap(e.getBonusTile());
    }

    /**
     * This event is called when an ammo or a powerup is dropped on the field
     * @param e the bonusTileEvent containing info about the dropped bonus
     */
    @Override
    public void onBonusTileDropped(BonusTileEvent e) {
        gameRepresentation.addBonusToMap(e.getBonusTile());
    }

    /**
     * This event is called when a player dies
     * @param e the PlayerEvent containing info about the player who died
     */
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

    /**
     * This event is called when a player reborn
     * @param e the PlayerEvent containing info about the player who reborn
     */
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

    /**
     * This event is called when a player's wallet changes
     * @param e the event containing info about the new wallet of the player
     */
    @Override
    public void onPlayerWalletChanged(PlayerWalletChanged e) {
        gameRepresentation.updatePlayer(e.getPlayer());
        if (!e.getPlayer().getNickname().equals(nickname)) {
            printStream.println(w + e.getPlayer().getNickname() + "'s wallet changed!");
            printStream.println(m + e.getMessage());
        }
    }

    /**
     * This event is called when a player's board filps
     * @param e the event containing info about the player who flipped his board
     */
    @Override
    public void onPlayerBoardFlipped(PlayerEvent e) {
        if (e.getPlayer().getNickname().equals(nickname)){
            printStream.println(w + "Your board just flipped!");
        }
        printStream.println(w + e.getPlayer().getNickname() + "'s board flipped");
    }

    /**
     * This event is called when a player tile's flips
     * @param e the event containing info about the player who flipped his tile
     */
    @Override
    public void onPlayerTileFlipped(PlayerEvent e) {
        gameRepresentation.updatePlayer(e.getPlayer());
        if (e.getPlayer().getNickname().equals(nickname)){
            printStream.println(w + "Your tile just flipped!");
        }
        printStream.println(w + e.getPlayer().getNickname() + "'s tile flipped");
    }

    /**
     * This event is called when a player's health changes
     * @param e the event containing info about the player's health
     */
    @Override
    public void onPlayerHealthChanged(PlayerHealthChanged e) {
        gameRepresentation.updatePlayer(e.getPlayer());
        if (!e.getPlayer().getNickname().equals(nickname)) {
            printStream.println(m + e.getPlayer().getNickname() + "'s health changed");
        } else{
            printStream.println(m + "Your health changed!");
        }
    }

    /**
     * This event is called when a weapon is reloaded
     * @param e the event containing info about the player and the reloaded weapon
     */
    @Override
    public void onWeaponReloaded(PlayerWeaponEvent e) {
        gameRepresentation.updatePlayer(e.getPlayer());
        if (!e.getPlayer().getNickname().equals(nickname)) {
            printStream.println(m + e.getPlayer().getNickname() + " reloaded his " + e.getWeaponName());
        }
    }

    /**
     * This event is called when a weapon becomes unloaded
     * @param e the event containing info about the player and the unloaded weapon
     */
    @Override
    public void onWeaponUnloaded(PlayerWeaponEvent e) {
        gameRepresentation.updatePlayer(e.getPlayer());
        if (!e.getPlayer().getNickname().equals(nickname)) {
            printStream.println(m + e.getPlayer().getNickname() + " now has his " + e.getWeaponName() + " unloaded");
        }
    }

    /**
     * This event is called when a weapon is picked up
     * @param e the event containing info about the player and the picked up weapon
     */
    @Override
    public void onWeaponPicked(PlayerWeaponExchanged e) {
        gameRepresentation.grabPlayerWeapon(e.getPlayer(), e.getWeaponName(), e.getRow(), e.getColumn());
        if (!e.getPlayer().getNickname().equals(nickname)) {
            printStream.println(m + e.getPlayer().getNickname() + " picked up " + e.getWeaponName() + " on block Row" + e.getRow() + " Column" + e.getColumn());
        }
    }

    /**
     * This event is called when a weapon is dropped
     * @param e the event containing info about the player and the dropped weapon
     */
    @Override
    public void onWeaponDropped(PlayerWeaponExchanged e) {
        gameRepresentation.dropPlayerWeapon(e.getPlayer(), e.getWeaponName(), e.getRow(), e.getColumn());
        if (!e.getPlayer().getNickname().equals(nickname)) {
            printStream.println(m + e.getPlayer().getNickname() + " dropped his " + e.getWeaponName() + " on Row" + e.getRow() + " Column" + e.getColumn());
        }
    }

    /**
     * This event is called when a player completes successfully the login
     * @param e the ClientEvent containing info about the new client connected
     */
    @Override
    public void onLoginSuccess(ClientEvent e) {
        if (e.getNickname().equals(this.nickname)){
            printStream.println("LOGIN SUCCESSFUL! Your nickname is: " + e.getNickname());
        } else {
            if (isMatchOnGoing()) {
                gameRepresentation.setPlayerAlive(e.getNickname());
            }
            printStream.println(e.getNickname() + " connected to the game");
        }
    }

    /**
     * This event is called when a clients disconnected
     * @param e the ClientEvent containing info about the new client connected
     */
    @Override
    public void onClientDisconnected(ClientEvent e) {
        printStream.println(w + e.getNickname() + " disconnected");
        gameRepresentation.setPlayerDied(e.getNickname());
    }

    /**
     * This event is called when a clients reconnected
     * @param e the ClientEvent containing info about the client reconnected
     */
    @Override
    public void onPlayerReconnected(PlayerEvent e) {
        gameRepresentation.updatePlayer(e.getPlayer());
        gameRepresentation.setPlayerAlive(e.getPlayer());
        printStream.println(w + e.getPlayer().getNickname() + " reconnected");
    }

    /**
     * This event is called when a player spawns on a spawnpoint
     * @param e The event containing info about the player who spawns and his location
     */
    @Override
    public void onPlayerSpawned(PlayerSpawned e) {
        gameRepresentation.setPlayerAlive(e.getPlayer());
        gameRepresentation.movePlayer(e.getPlayer(), e.getRow(), e.getColumn());
        if (!e.getPlayer().getNickname().equals(nickname)) {
            printStream.println(m + e.getPlayer().getNickname() + " spawned on Row: " + e.getRow() + " and Column: " + e.getColumn());
        }
    }

    /**
     * This event is called when a player gets overkilled
     * @param e the Player event containing info about the player who has been overkilled
     */
    @Override
    public void onPlayerOverkilled(PlayerEvent e) {
        if (e.getPlayer().getNickname().equals(nickname)){
            printStream.println(w + "You have been overkilled!");
        } else printStream.println(w + e.getPlayer().getNickname() + " has been overkilled");
    }

    /**
     * This event is called when the active player changes
     * @param e The PlayerEvent containing info about the new active player
     */
    @Override
    public void onActivePlayerChanged(PlayerEvent e) {
        if (isMatchOnGoing()) {
            printStream.println(m + "Now it's " + e.getPlayer().getNickname() + " turn!");
            gameRepresentation.showUpdatedMap(printStream);
        } else {
            removeAllListeners();
            printStream.println(m + "Game over!");
        }
    }

    /**
     * This event is called during login if the print stream choose a nickname already selected by a connected player
     * to the server. This event lets the print stream choose another nickname
     */
    @Override
    public void onDuplicatedNickname() {
        printStream.println("Nickname " + nickname + " not available. Choose another nick!");
        removeAllListeners();

        new Thread(() -> {
            try {
                connector.close();

                printStream.println("Enter another nickname");
                this.nickname = scanner.nextLine();

                setConnector();
            } catch (Exception ex) {
                printStream.println("Could not close the connector");
            }
        }).start();
    }
}
