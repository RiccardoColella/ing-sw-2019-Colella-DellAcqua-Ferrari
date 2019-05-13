package it.polimi.ingsw.server.view;

import com.google.gson.Gson;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.currency.AmmoCube;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.events.*;
import it.polimi.ingsw.server.model.events.listeners.BoardListener;
import it.polimi.ingsw.server.model.events.listeners.MatchListener;
import it.polimi.ingsw.server.model.events.listeners.PlayerListener;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.weapons.WeaponTile;
import it.polimi.ingsw.server.view.exceptions.ViewDisconnectedException;
import it.polimi.ingsw.shared.InputMessageQueue;
import it.polimi.ingsw.shared.bootstrap.ClientInitializationInfo;
import it.polimi.ingsw.shared.messages.ClientApi;
import it.polimi.ingsw.shared.messages.Message;
import it.polimi.ingsw.shared.messages.ServerApi;
import it.polimi.ingsw.shared.messages.templates.Answer;
import it.polimi.ingsw.shared.messages.templates.Question;
import it.polimi.ingsw.shared.messages.templates.gsonadapters.AnswerOf;
import it.polimi.ingsw.shared.viewmodels.Powerup;
import it.polimi.ingsw.shared.viewmodels.Wallet;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class is an abstract server-side View. It contains all the methods needed for the interaction with the controller
 * and implements all the listeners needed to receive information from the model
 *
 * @author Carlo Dell'Acqua
 */
public abstract class View implements Interviewer, AutoCloseable, MatchListener, PlayerListener, BoardListener {

    private static final int CLOSE_TIMEOUT_MILLISECONDS = 10000;
    private static final int CLOSE_CHECK_DELAY = CLOSE_TIMEOUT_MILLISECONDS / 10;


    /**
     * JSON conversion utility
     */
    private static Gson gson = new Gson();

    /**
     * Logging utility
     */
    protected Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Boolean representing the virtual connection status
     */
    private boolean connected = true;

    /**
     * The message queue which accumulates input messages
     */
    protected InputMessageQueue inputMessageQueue = new InputMessageQueue();

    /**
     * The message queue which stores output messages ready to be sent
     */
    protected LinkedBlockingQueue<Message> outputMessageQueue = new LinkedBlockingQueue<>();

    /**
     * Maximum timeout before considering the view disconnected
     */
    protected int answerTimeout;
    /**
     * Measurement unit of the timeout
     */
    protected TimeUnit answerTimeoutUnit;

    /**
     * Player initialization information
     */
    private ClientInitializationInfo setup;

    /**
     * The player instance associated with this View
     */
    private Player player;

    /**
     * Constructs a server-side view
     *
     * @param answerTimeout maximum timeout before considering the view disconnected
     * @param answerTimeoutUnit measurement unit of the timeout
     */
    public View(int answerTimeout, TimeUnit answerTimeoutUnit) {
        this.answerTimeout = answerTimeout;
        this.answerTimeoutUnit = answerTimeoutUnit;
    }

    /**
     * Initializes the view. This method should be called after the view construction to collect the user preferences from the client view
     *
     * @throws InterruptedException if the thread was forced to stop
     * @throws ViewDisconnectedException if the expected initialization message has not been not received
     */
    public void initialize() throws InterruptedException {

        try {
            Message initEvent = inputMessageQueue.dequeueEvent(5, TimeUnit.SECONDS);
            if (initEvent.getNameAsEnum(ServerApi.class).equals(ServerApi.VIEW_INIT_EVENT)) {
                this.setup = gson.fromJson(
                        initEvent.getPayload(),
                        ClientInitializationInfo.class
                );
            } else {
                throw new ViewDisconnectedException("Initialization event message is malformed");
            }
        } catch (TimeoutException ex) {
            throw new ViewDisconnectedException("Initialization event message not received", ex);
        }
    }

    /**
     * Sets the player associated with this view (this should be done before starting the match)
     *
     * @param player the player associated with this view
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * @return true if the view is still considered virtually connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * @return the nickname chosen by the player
     */
    public String getNickname() {
        return setup.getNickname();
    }

    /**
     * @return the preset chosen by the player
     */
    public BoardFactory.Preset getChosenPreset() {
        return setup.getPreset();
    }

    /**
     * @return the number of skulls chosen by the player
     */
    public int getChosenSkulls() {
        return setup.getSkulls();
    }

    /**
     * @return the match mode chosen by the player
     */
    public Match.Mode getChosenMode() {
        return setup.getMode();
    }

    /**
     * Called after sending a question, this method waits on the input message queue until a message associated with
     * the question and answer flow is available
     *
     * @param flowId the identifier of the question-and-answer flow
     * @param options the options the client can choose from
     * @param <T> the type of the item in the option collection
     * @return the chosen option or null if no choice was made
     * @throws ViewDisconnectedException if the client didn't give an answer within the available answering timeout
     */
    @Nullable
    private  <T> T awaitResponse(String flowId, Collection<T> options) {

        try {
            Message response = inputMessageQueue.dequeueAnswer(flowId, answerTimeout, answerTimeoutUnit);
            Answer<T> answer = gson.fromJson(response.getPayload(), new AnswerOf<>(options.iterator().next().getClass()));

            if (answer.isPresent() && !options.contains(answer.getChoice())) {
                throw new ViewDisconnectedException("Received an invalid answer from the client");
            } else {
                return answer.getChoice();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            connected = false;
            throw new ViewDisconnectedException("Unable to retrieve input message", e);
        } catch (TimeoutException e) {
            connected = false;
            throw new ViewDisconnectedException("Unable to retrieve input message", e);
        }
    }

    /**
     * Sends a question message to the client view and wait for the response
     *
     * @param questionText the question to show to the user
     * @param options a collection of options to choose from
     * @param messageName the name which identifies the type of message that is been sent
     * @param <T> the type of the item in the option collection
     * @return the chosen answer
     * @throws ViewDisconnectedException if the client wasn't able to give a correct answer within the timeout
     * @throws IllegalArgumentException if the an empty collection was provided for the "options" parameter
     */
    @Override
    public <T> T select(String questionText, Collection<T> options, ClientApi messageName) {
        if (!options.isEmpty()) {

            Message message = Message.createQuestion(messageName, new Question<>(questionText, options));

            outputMessageQueue.add(message);

            return awaitResponse(message.getFlowId(), options);
        } else {
            throw new IllegalArgumentException("No option provided");
        }
    }

    /**
     * Sends a question message to the client view and wait for the response that can be empty
     *
     * @param questionText the question to show to the user
     * @param options a collection of options to choose from
     * @param messageName the name which identifies the type of message that is been sent
     * @param <T> the type of the item in the option collection
     * @return the chosen answer or an empty optional
     * @throws ViewDisconnectedException if the client wasn't able to give a correct answer within the timeout
     * @throws IllegalArgumentException if the an empty collection was provided for the "options" parameter
     */
    @Override
    public <T> Optional<T> selectOptional(String questionText, Collection<T> options, ClientApi messageName) {
        if (!options.isEmpty()) {
            Message message = Message.createQuestion(messageName, new Question<>(questionText, options, true));
            outputMessageQueue.add(message);

            return Optional.ofNullable(awaitResponse(message.getFlowId(), options));
        } else {
            throw new IllegalArgumentException("No option provided");
        }
    }

    private void mapWallets(Player player, it.polimi.ingsw.shared.viewmodels.Player playerVM) {
        List<CurrencyColor> ammoCubes = player.getAmmoCubes().stream().map(AmmoCube::getColor).collect(Collectors.toList());
        playerVM.getWallet().setAmmoCubes(ammoCubes);
        List<Powerup> powerups = player.getPowerups().stream().map(p -> new Powerup(p.getName(), p.getColor())).collect(Collectors.toList());
        playerVM.getWallet().setPowerups(powerups);
        List<String> unloadedWeapons = player.getWeapons()
                .stream()
                .map(WeaponTile::getName)
                .collect(Collectors.toList());
        List<String> loadedWeapons = player.getWeapons()
                .stream()
                .filter(WeaponTile::isLoaded)
                .map(WeaponTile::getName)
                .collect(Collectors.toList());
        unloadedWeapons.removeAll(loadedWeapons);
        playerVM.getWallet().setLoadedWeapons(loadedWeapons);
        playerVM.getWallet().setUnloadedWeapons(unloadedWeapons);
    }


    @Override
    public void close() throws Exception {
        java.time.Instant deadline = Instant.now().plus(Duration.ofMillis(CLOSE_TIMEOUT_MILLISECONDS));

        while (
                deadline.isAfter(Instant.now())
                && !outputMessageQueue.isEmpty()
        ) {
            Thread.sleep(CLOSE_CHECK_DELAY);
        }
    }

    public void close(Message lastMessage) throws Exception {
        outputMessageQueue.add(lastMessage);
        this.close();
    }

    @Override
    public void onMatchStarted(MatchEvent event) {
        Match match = event.getMatch();
        List<Player> allPlayers = new LinkedList<>(match.getPlayers());
        List<Player> opponents = allPlayers
                .stream()
                .filter(o -> !o.getPlayerInfo().getNickname().equals(setup.getNickname()))
                .collect(Collectors.toList());
        List<it.polimi.ingsw.shared.viewmodels.Player> opponentsVM = opponents
                .stream()
                .map(p ->
                        new it.polimi.ingsw.shared.viewmodels.Player(
                                p.getPlayerInfo().getNickname(),
                                p.getPlayerInfo().getColor(),
                                new Wallet()
                        )
                )
                .collect(Collectors.toList());
        it.polimi.ingsw.shared.viewmodels.Player selfVM = new it.polimi.ingsw.shared.viewmodels.Player(
                player.getPlayerInfo().getNickname(),
                player.getPlayerInfo().getColor(),
                new Wallet()
        );
        mapWallets(player, selfVM);
        it.polimi.ingsw.shared.events.networkevents.MatchStarted e = new it.polimi.ingsw.shared.events.networkevents.MatchStarted(
                match.getBoardPreset(),
                selfVM,
                opponentsVM
        );
        outputMessageQueue.add(Message.createEvent(ClientApi.MATCH_STARTED_EVENT, e));
    }

    @Override
    public void onMatchEnded(MatchEnded event) {
        // TODO: Bind Model to ViewModel and enqueue the event
    }

    @Override
    public void onMatchModeChanged(MatchModeChanged event) {
        // TODO: Bind Model to ViewModel and enqueue the event
    }


    @Override
    public void onKillshotTrackChanged(KillshotTrackChanged e) {
        // TODO: Bind Model to ViewModel and enqueue the event
    }

    @Override
    public void onPlayerDied(PlayerDied e) {
        // TODO: Bind Model to ViewModel and enqueue the event
    }

    @Override
    public void onPlayerDamaged(PlayerDamaged e) {
        // TODO: Bind Model to ViewModel and enqueue the event
    }

    @Override
    public void onPlayerOverkilled(PlayerOverkilled e) {
        // TODO: Bind Model to ViewModel and enqueue the event
    }

    @Override
    public void onPlayerReborn(PlayerEvent e) {
        // TODO: Bind Model to ViewModel and enqueue the event
    }

    @Override
    public void onPlayerBoardFlipped(PlayerEvent e) {
        // TODO: Bind Model to ViewModel and enqueue the event
    }

    @Override
    public void onWeaponReloaded(WeaponEvent e) {
        // TODO: Bind Model to ViewModel and enqueue the event
    }

    @Override
    public void onWeaponUnloaded(WeaponEvent e) {
        // TODO: Bind Model to ViewModel and enqueue the event
    }

    @Override
    public void onWeaponPicked(WeaponExchanged e) {
        // TODO: Bind Model to ViewModel and enqueue the event

    }

    @Override
    public void onWeaponDropped(WeaponExchanged e) {
        // TODO: Bind Model to ViewModel and enqueue the event

    }

    @Override
    public void onWalletChanged(PlayerWalletChanged e) {
        // TODO: Bind Model to ViewModel and enqueue the event
    }

    @Override
    public void onHealthChanged(PlayerEvent e) {
        // TODO: Bind Model to ViewModel and enqueue the event
    }

    @Override
    public void onPlayerTeleported(PlayerMoved e) {
        // TODO: Bind Model to ViewModel and enqueue the event

    }

    @Override
    public void onPlayerMoved(PlayerMoved e) {
        // TODO: Bind Model to ViewModel and enqueue the event

    }
}
