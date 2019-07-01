package it.polimi.ingsw.server.view;

import com.google.gson.Gson;
import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.battlefield.SpawnpointBlock;
import it.polimi.ingsw.server.model.battlefield.TurretBlock;
import it.polimi.ingsw.server.model.currency.AmmoCube;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.events.*;
import it.polimi.ingsw.server.model.events.listeners.BoardListener;
import it.polimi.ingsw.server.model.events.listeners.MatchListener;
import it.polimi.ingsw.server.model.events.listeners.PlayerListener;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.server.model.weapons.WeaponTile;
import it.polimi.ingsw.server.view.events.ViewEvent;
import it.polimi.ingsw.server.view.events.listeners.ViewListener;
import it.polimi.ingsw.server.view.exceptions.ViewDisconnectedException;
import it.polimi.ingsw.shared.InputMessageQueue;
import it.polimi.ingsw.shared.bootstrap.ClientInitializationInfo;
import it.polimi.ingsw.shared.datatransferobjects.BonusTile;
import it.polimi.ingsw.shared.datatransferobjects.PlayerHealth;
import it.polimi.ingsw.shared.events.networkevents.ClientEvent;
import it.polimi.ingsw.shared.events.networkevents.PlayerHealthChanged;
import it.polimi.ingsw.shared.events.networkevents.PlayerWeaponExchanged;
import it.polimi.ingsw.shared.events.networkevents.WeaponEvent;
import it.polimi.ingsw.shared.messages.ClientApi;
import it.polimi.ingsw.shared.messages.Message;
import it.polimi.ingsw.shared.messages.ServerApi;
import it.polimi.ingsw.shared.messages.templates.Answer;
import it.polimi.ingsw.shared.messages.templates.Question;
import it.polimi.ingsw.shared.messages.templates.gsonadapters.AnswerOf;
import it.polimi.ingsw.shared.datatransferobjects.Powerup;
import it.polimi.ingsw.shared.datatransferobjects.Wallet;
import it.polimi.ingsw.utils.Tuple;
import javafx.util.Pair;

import javax.annotation.Nullable;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class is an abstract server-side View. It contains all the methods needed for the interaction with the controller
 * and implements all the listeners needed to receive information from the model
 *
 * @author Carlo Dell'Acqua
 */
public abstract class View implements Interviewer, AutoCloseable, MatchListener, PlayerListener, BoardListener, ViewListener {

    /**
     * The view waits for this amount of milliseconds, with the purpose of emptying the queue, after a shutdown requests
     */
    private static final int CLOSE_TIMEOUT_MILLISECONDS = 10000;

    /**
     * Timeout used in the waiting loop (when the view is waiting for the queue to get empty)
     */
    private static final int CLOSE_CHECK_DELAY = CLOSE_TIMEOUT_MILLISECONDS / 10;

    /**
     * Timeout used when dequeueing an event
     */
    private static final int DEQUEUE_TIMEOUT_MILLISECONDS = 1000;

    /**
     * Timeout used to check the heartbeat
     */
    private static final int HEARTBEAT_TIMEOUT = 10000;


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
     * Listeners of View events
     */
    private final Set<ViewListener> listeners = Collections.synchronizedSet(new HashSet<>());

    /**
     * Thread pool used to check the heartbeat of the client
     */
    private final ExecutorService heartbeatThreadPool = Executors.newSingleThreadExecutor();

    /**
     * Thread pool used to transmit events
     */
    private final ExecutorService eventThreadPool = Executors.newSingleThreadExecutor();

    /**
     * The instant of the last heartbeat that was received
     */
    private Instant lastHeartbeat;

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

                lastHeartbeat = Instant.now();
                startReceivingEvents();
                startHeartbeat();
            } else {
                throw new ViewDisconnectedException("Initialization event message is malformed");
            }
        } catch (TimeoutException ex) {
            throw new ViewDisconnectedException("Initialization event message not received", ex);
        }
    }

    /**
     * This method makes the view start receiving events
     */
    private void startReceivingEvents() {
        eventThreadPool.execute(() -> {
            while (!eventThreadPool.isShutdown() && isConnected()) {
                try {
                    Message message = inputMessageQueue.dequeueEvent(DEQUEUE_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
                    ServerApi eventType = message.getNameAsEnum(ServerApi.class);
                    switch (eventType) {
                        case HEARTBEAT: {
                            lastHeartbeat = Instant.now();
                            break;
                        }
                        default: {
                            throw new UnsupportedOperationException("Event \"" + eventType + "\" not supported");
                        }
                    }
                } catch (TimeoutException ignored) {
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    logger.warning("Thread interrupted " + ex);
                }

            }
        });
    }

    /**
     * This method makes the view start listening for heartbeats
     */
    private void startHeartbeat() {
        heartbeatThreadPool.execute(() -> {
            while (!heartbeatThreadPool.isShutdown() && isConnected()) {
                if (lastHeartbeat.isBefore(Instant.now().minusMillis(HEARTBEAT_TIMEOUT))) {
                    logger.info("No heartbeat received within the timeout, disconnecting " + getNickname() + "...");
                    disconnect();
                } else {
                    try {
                        Thread.sleep(HEARTBEAT_TIMEOUT);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.warning("Thread stopped " + e);
                    }
                }
            }
        });
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
     * Gets the player associated with this view
     *
     * @return the player associated with this view
     */
    public Player getPlayer() {
        return player;
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

        if (!connected) {
            throw new ViewDisconnectedException("Unable to retrieve input message, view is not connected");
        }

        try {
            Message response = inputMessageQueue.dequeueAnswer(flowId, answerTimeout, answerTimeoutUnit);
            Answer<T> answer = gson.fromJson(response.getPayload(), new AnswerOf<>(options.iterator().next().getClass()));

            return options.stream().filter(option -> option.equals(answer.getChoice())).findAny().orElse(null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            disconnect();
            throw new ViewDisconnectedException("Unable to retrieve input message", e);
        } catch (TimeoutException e) {
            disconnect();
            throw new ViewDisconnectedException("Unable to retrieve input message", e);
        }
    }

    /**
     * Set the connection status to false and notify all the listeners
     */
    private void disconnect() {
        if (connected) {
            logger.warning("Player " + getNickname() + " disconnected");
            this.connected = false;
            notifyViewDisconnected();
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
     * @throws IllegalArgumentException if the an empty collection was provided for the "options" parameter
     */
    @Override
    public <T> T select(String questionText, Collection<T> options, ClientApi messageName) {
        if (!options.isEmpty()) {

            Message message = Message.createQuestion(messageName, new Question<>(questionText, options));

            outputMessageQueue.add(message);

            try {
                return awaitResponse(message.getFlowId(), options);
            } catch (ViewDisconnectedException e) {
                return options.iterator().next(); // Fake response
            }
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
     * @throws IllegalArgumentException if the an empty collection was provided for the "options" parameter
     */
    @Override
    public <T> Optional<T> selectOptional(String questionText, Collection<T> options, ClientApi messageName) {
        if (!options.isEmpty()) {
            Message message = Message.createQuestion(messageName, new Question<>(questionText, options, true));
            outputMessageQueue.add(message);

            try {
                return Optional.ofNullable(awaitResponse(message.getFlowId(), options));
            } catch (ViewDisconnectedException e) {
                return Optional.empty(); // Fake response
            }
        } else {
            throw new IllegalArgumentException("No option provided");
        }
    }

    /**
     * Maps the wallet of a Player of the Model to the appropriate data transfer object
     *
     * @param player the Player Model object
     * @return the Wallet data transfer object
     */
    private Wallet mapWallets(Player player) {
        List<CurrencyColor> ammoCubes = player.getAmmoCubes().stream().map(AmmoCube::getColor).collect(Collectors.toList());

        List<Powerup> powerups = player.getPowerups().stream().map(p -> new Powerup(p.getName(), p.getColor())).collect(Collectors.toList());

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

        return new Wallet(loadedWeapons, unloadedWeapons, ammoCubes, powerups);
    }

    /**
     * Maps a Player into the relative data transfer object
     *
     * @param player the Player Model object
     * @return the Player data transfer object
     */
    private it.polimi.ingsw.shared.datatransferobjects.Player mapPlayer(Player player) {
        return new it.polimi.ingsw.shared.datatransferobjects.Player(
                player.getPlayerInfo().getNickname(),
                player.getPlayerInfo().getColor(),
                mapWallets(player),
                mapPlayerHealth(player),
                player.isTileFlipped(),
                player.isBoardFlipped()
        );
    }

    /**
     * Maps the health of a Player of the Model to the appropriate data transfer object
     *
     * @param player the Player Model object
     * @return the PlayerHealth data transfer object
     */
    private PlayerHealth mapPlayerHealth(Player player) {

        return new PlayerHealth(
                player.getSkulls(),
                player.getDamageTokens().stream().map(t -> t.getAttacker().getPlayerInfo().getColor()).collect(Collectors.toList()),
                player.getMarks().stream().map(m -> m.getAttacker().getPlayerInfo().getColor()).collect(Collectors.toList())
        );
    }

    /**
     * Closes this object and stops the background threads execution
     *
     * @throws Exception if the closing process is forced to stop or the remote resources are unable to correctly close or the socket cannot be closed
     */
    @Override
    public void close() throws Exception {

        disconnect();

        java.time.Instant deadline = Instant.now().plus(Duration.ofMillis(CLOSE_TIMEOUT_MILLISECONDS));

        while (
                deadline.isAfter(Instant.now())
                && !outputMessageQueue.isEmpty()
        ) {
            Thread.sleep(CLOSE_CHECK_DELAY);
        }

        synchronized (heartbeatThreadPool) {
            heartbeatThreadPool.shutdown();
        }
        while (!heartbeatThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
            logger.warning(getNickname() + ": heartbeatThreadPool hasn't shut down yet, waiting...");
        }
        synchronized (eventThreadPool) {
            eventThreadPool.shutdown();
        }
        while (!eventThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
            logger.warning(getNickname() + ": eventThreadPool hasn't shut down yet, waiting...");
        }
    }

    /**
     * Enqueues a last message, then closes this object and stops the background threads execution
     *
     * @param lastMessage the last message to send
     * @throws Exception if the closing process is forced to stop or the remote resources are unable to correctly close or the socket cannot be closed
     */
    public void close(Message lastMessage) throws Exception {
        outputMessageQueue.add(lastMessage);
        this.close();
    }

    /**
     * Notifies the client that the match has started
     * @param event the event corresponding to the beginning of the match
     */
    @Override
    public void onMatchStarted(MatchEvent event) {
        enqueueMatchInitializationEvent(event.getMatch(), false);
    }

    /**
     * Enqueues a generic match initialization event
     *
     * @param match the match that was initialized
     * @param resumed whether the match was resumed
     */
    private void enqueueMatchInitializationEvent(Match match, boolean resumed) {
        List<Player> allPlayers = new LinkedList<>(match.getPlayers());
        List<it.polimi.ingsw.shared.datatransferobjects.Player> opponentsVM = allPlayers
                .stream()
                .filter(o -> !o.getPlayerInfo().getNickname().equals(setup.getNickname()))
                .map(this::mapPlayer)
                .collect(Collectors.toList());
        List<String> weaponTop = new LinkedList<>();
        List<String> weaponLeft = new LinkedList<>();
        List<String> weaponRight = new LinkedList<>();
        for (CurrencyColor color : CurrencyColor.values()) {
            SpawnpointBlock block = match.getBoard().getSpawnpoint(color);
            List<String> weapons = block.getWeapons().stream().map(WeaponTile::getName).collect(Collectors.toList());
            if (block.getRow() == 0) {
                weaponTop = weapons;
            } else if (block.getColumn() == 0) {
                weaponLeft = weapons;
            } else {
                weaponRight = weapons;
            }
        }
        if (resumed) {
            int totalSkulls = match.getRemainingSkulls();
            List<Integer> givenSkulls = match.getPlayers()
                    .stream()
                    .map(Player::getSkulls)
                    .collect(Collectors.toList());
            for (Integer s : givenSkulls) {
                totalSkulls += s;
            }
            Map<PlayerColor, Point> playerLocations = match
                    .getPlayers()
                    .stream()
                    .filter(p -> {
                        try {
                            p.getBlock();
                            return true;
                        }
                        catch (IllegalStateException e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toMap((
                            p -> p.getPlayerInfo().getColor()),
                            p -> new Point(p.getBlock().getColumn(), p.getBlock().getRow())
                    ));
            outputMessageQueue.add(Message.createEvent(
                    ClientApi.MATCH_RESUMED_EVENT,
                    new it.polimi.ingsw.shared.events.networkevents.MatchResumed(
                            totalSkulls,
                            match.getBoardPreset(),
                            mapPlayer(player),
                            opponentsVM,
                            weaponTop,
                            weaponRight,
                            weaponLeft,
                            mapPlayer(match.getActivePlayer()),
                            mapTurretBonusTiles(match.getBoard()),
                            match.getMode(),
                            match.getKillshots().stream().map(k -> new Tuple<>(k.getDamageToken().getAttacker().getPlayerInfo().getColor(), k.isOverkill())).collect(Collectors.toList()),
                            playerLocations
                    )
            ));
        } else {
            outputMessageQueue.add(Message.createEvent(
                    ClientApi.MATCH_STARTED_EVENT,
                    new it.polimi.ingsw.shared.events.networkevents.MatchStarted(
                            match.getRemainingSkulls(),
                            match.getBoardPreset(),
                            mapPlayer(player),
                            opponentsVM,
                            weaponTop,
                            weaponRight,
                            weaponLeft,
                            mapPlayer(match.getActivePlayer()),
                            mapTurretBonusTiles(match.getBoard())
                    )
            ));
        }
    }

    /**
     * Maps the content of the turrets of the model into the appropriate data transfer objects
     *
     * @param board the board containing the turrets
     * @return a set with the data transfer object bonus tiles
     */
    private Set<BonusTile> mapTurretBonusTiles(Board board) {
        return board.getTurretBlocks()
                .stream()
                .map(block ->
                        new BonusTile(
                                block.getBonusTile()
                                        .orElseThrow(() -> new IllegalStateException("No bonus tile present on turret"))
                                        .getRewards()
                                        .stream()
                                        .map(AmmoCube::getColor)
                                        .collect(Collectors.toList()),
                                new Point(block.getColumn(), block.getRow())
                        )
                )
                .collect(Collectors.toSet());
    }

    /**
     * Notifies the client that the match has ended
     * @param event the event corresponding to the end of the match
     */
    @Override
    public void onMatchEnded(MatchEnded event) {
        it.polimi.ingsw.shared.events.networkevents.MatchEnded convertedEvent;
        Map<Integer, List<it.polimi.ingsw.shared.datatransferobjects.Player>> mappedRankings = new HashMap<>();
        event.getRankings().forEach((key, value) -> mappedRankings.put(key, value.stream().map(this::mapPlayer).collect(Collectors.toList())));
        Map<String, Integer> scores = new HashMap<>();
        event.getRankings()
                .forEach((key, value) -> value.forEach(p -> scores.put(p.getPlayerInfo().getNickname(), p.getPoints())));
        convertedEvent = new it.polimi.ingsw.shared.events.networkevents.MatchEnded(mappedRankings, scores);
        outputMessageQueue.add(Message.createEvent(ClientApi.MATCH_ENDED_EVENT, convertedEvent));
    }

    /**
     * Notifies the client that the match mode has changed
     *
     * @param event the event corresponding to the match mode change
     */
    @Override
    public void onMatchModeChanged(MatchModeChanged event) {
        // TODO: Bind Model to ViewModel and enqueue the event
    }

    /**
     * Notifies the client that the killshot track changed
     *
     * @param e the event corresponding to the killshot track change
     */
    @Override
    public void onKillshotTrackChanged(KillshotTrackChanged e) {
        List<Tuple<PlayerColor, Boolean>> killshots = e.getKillshots().stream().map(k -> new Tuple<>(k.getDamageToken().getAttacker().getPlayerInfo().getColor(), k.isOverkill())).collect(Collectors.toList());
        it.polimi.ingsw.shared.events.networkevents.KillshotTrackChanged convertedEvent = new it.polimi.ingsw.shared.events.networkevents.KillshotTrackChanged(killshots);
        outputMessageQueue.add(Message.createEvent(ClientApi.MATCH_KILLSHOT_TRACK_CHANGED_EVENT, convertedEvent));
    }

    /**
     * Notifies the client that a player died
     *
     * @param e the event corresponding to the player's death
     */
    @Override
    public void onPlayerDied(PlayerDied e) {
        it.polimi.ingsw.shared.datatransferobjects.Player playerVM = mapPlayer(e.getVictim());
        it.polimi.ingsw.shared.events.networkevents.PlayerEvent convertedEvent = new it.polimi.ingsw.shared.events.networkevents.PlayerEvent(playerVM);
        outputMessageQueue.add(Message.createEvent(ClientApi.PLAYER_DIED_EVENT, convertedEvent));
    }

    /**
     * Notifies the client that a player was damaged
     *
     * @param e this parameter contains info about the attacker and the damaged player
     */
    @Override
    public void onPlayerDamaged(PlayerDamaged e) {
        //View only cares about onPlayerHealthChanged
    }

    /**
     * Notifies the client that a player was overkilled
     *
     * @param e the event corresponding to the player's death
     */
    @Override
    public void onPlayerOverkilled(PlayerOverkilled e) {
        it.polimi.ingsw.shared.datatransferobjects.Player playerVM = mapPlayer(e.getVictim());
        it.polimi.ingsw.shared.events.networkevents.PlayerEvent convertedEvent = new it.polimi.ingsw.shared.events.networkevents.PlayerEvent(playerVM);
        outputMessageQueue.add(Message.createEvent(ClientApi.PLAYER_OVERKILLED_EVENT, convertedEvent));
    }

    /**
     * Notifies the client that a player was brought back to life
     *
     * @param e the event corresponding to the player's rebirth
     */
    @Override
    public void onPlayerReborn(PlayerEvent e) {
        onBasicPlayerEvent(e, ClientApi.PLAYER_REBORN_EVENT);
    }

    /**
     * Notifies the client that a player's board flipped
     *
     * @param e the event corresponding to the player's board flipping
     */
    @Override
    public void onPlayerBoardFlipped(PlayerEvent e) {
        onBasicPlayerEvent(e, ClientApi.PLAYER_BOARD_FLIPPED_EVENT);
    }


    /**
     * Notifies the client that a player's tile flipped
     *
     * @param e the event corresponding to the player's tile flipping
     */
    @Override
    public void onPlayerTileFlipped(PlayerEvent e) {
        onBasicPlayerEvent(e, ClientApi.PLAYER_TILE_FLIPPED_EVENT);
    }

    /**
     * Notifies the client that a weapon was reloaded
     *
     * @param e the event corresponding to the player reloading a weapon
     */
    @Override
    public void onWeaponReloaded(PlayerWeaponEvent e) {
        it.polimi.ingsw.shared.events.networkevents.PlayerWeaponEvent convertedEvent;
        it.polimi.ingsw.shared.datatransferobjects.Player playerVM = mapPlayer(e.getPlayer());
        convertedEvent = new it.polimi.ingsw.shared.events.networkevents.PlayerWeaponEvent(playerVM, playerVM.getNickname() + " reloaded their " + e.getWeaponTile().getName());
        outputMessageQueue.add(Message.createEvent(ClientApi.WEAPON_RELOADED_EVENT, convertedEvent));
    }

    /**
     * Notifies the client that a weapon was unloaded
     *
     * @param e the event corresponding to the player unloading a weapon
     */
    @Override
    public void onWeaponUnloaded(PlayerWeaponEvent e) {
        it.polimi.ingsw.shared.events.networkevents.PlayerWeaponEvent convertedEvent;
        it.polimi.ingsw.shared.datatransferobjects.Player playerVM = mapPlayer(e.getPlayer());
        convertedEvent = new it.polimi.ingsw.shared.events.networkevents.PlayerWeaponEvent(playerVM, playerVM.getNickname() + " unloaded their " + e.getWeaponTile().getName());
        outputMessageQueue.add(Message.createEvent(ClientApi.WEAPON_UNLOADED_EVENT, convertedEvent));

    }

    /**
     * Notifies the client that a weapon was picked
     *
     * @param e the event corresponding to the player picking up a weapon
     */
    @Override
    public void onWeaponPicked(WeaponExchanged e) {
        PlayerWeaponExchanged convertedEvent;
        convertedEvent = new PlayerWeaponExchanged(mapPlayer(e.getPlayer()), e.getWeaponTile().getName(), e.getBlock().getRow(), e.getBlock().getColumn());
        outputMessageQueue.add(Message.createEvent(ClientApi.WEAPON_PICKED_EVENT, convertedEvent));

    }

    /**
     * Notifies the client that a weapon was dropped
     *
     * @param e the event corresponding to the player dropping a weapon
     */
    @Override
    public void onWeaponDropped(WeaponExchanged e) {
        PlayerWeaponExchanged convertedEvent;
        convertedEvent = new PlayerWeaponExchanged(mapPlayer(e.getPlayer()), e.getWeaponTile().getName(), e.getBlock().getRow(), e.getBlock().getColumn());
        outputMessageQueue.add(Message.createEvent(ClientApi.WEAPON_DROPPED_EVENT, convertedEvent));
    }

    /**
     * Notifies the client that a player's wallet has changed
     *
     * @param e the event corresponding to the player's wallet changing
     */
    @Override
    public void onWalletChanged(PlayerWalletChanged e) {
        it.polimi.ingsw.shared.events.networkevents.PlayerWalletChanged convertedEvent;
        it.polimi.ingsw.shared.datatransferobjects.Player playerVM = mapPlayer(e.getPlayer());
        convertedEvent = new it.polimi.ingsw.shared.events.networkevents.PlayerWalletChanged(playerVM, playerVM.getNickname() + "'s wallet changed");
        outputMessageQueue.add(Message.createEvent(ClientApi.PLAYER_WALLET_CHANGED_EVENT, convertedEvent));

    }

    /**
     * Notifies the client that a player's health has changed
     *
     * @param e the event corresponding to the player's health changing
     */
    @Override
    public void onHealthChanged(PlayerEvent e) {
        it.polimi.ingsw.shared.datatransferobjects.Player playerVM = mapPlayer(e.getPlayer());
        PlayerHealthChanged convertedEvent = new PlayerHealthChanged(playerVM);
        outputMessageQueue.add(Message.createEvent(ClientApi.PLAYER_HEALTH_CHANGED_EVENT, convertedEvent));
    }

    /**
     * Notifies the client that a player teleported
     *
     * @param e the event corresponding to the player teleporting
     */
    @Override
    public void onPlayerTeleported(PlayerMoved e) {
        it.polimi.ingsw.shared.events.networkevents.PlayerMoved convertedEvent;
        convertedEvent = new it.polimi.ingsw.shared.events.networkevents.PlayerMoved(mapPlayer(e.getPlayer()), e.getDestination().getRow(), e.getDestination().getColumn());
        outputMessageQueue.add(Message.createEvent(ClientApi.PLAYER_TELEPORTED_EVENT, convertedEvent));
    }

    /**
     * Notifies the client that a player moved
     *
     * @param e the event corresponding to the player moving
     */
    @Override
    public void onPlayerMoved(PlayerMoved e) {
        it.polimi.ingsw.shared.events.networkevents.PlayerMoved convertedEvent;
        convertedEvent = new it.polimi.ingsw.shared.events.networkevents.PlayerMoved(mapPlayer(e.getPlayer()), e.getDestination().getRow(), e.getDestination().getColumn());
        outputMessageQueue.add(Message.createEvent(ClientApi.PLAYER_MOVED_EVENT, convertedEvent));
    }

    /**
     * Notifies the client that a new weapon is available
     *
     * @param e the event corresponding to a new weapon being available
     */
    @Override
    public void onNewWeaponAvailable(NewWeaponAvailable e) {
        WeaponEvent convertedEvent = new WeaponEvent(
                e.getWeapon().getName(),
                e.getBlock().getRow(),
                e.getBlock().getColumn()
        );
        outputMessageQueue.add(Message.createEvent(ClientApi.NEW_WEAPON_AVAILABLE_EVENT, convertedEvent));
    }

    /**
     * Maps a BonusTile Model event to the appropriate network event
     * @param e the model bonus tile event
     * @return the converted event
     */
    private it.polimi.ingsw.shared.events.networkevents.BonusTileEvent mapBonusTileEvent(BonusTileBoardEvent e) {
        return new it.polimi.ingsw.shared.events.networkevents.BonusTileEvent(
                new BonusTile(
                        ((TurretBlock)e.getLocation())
                                .getBonusTile()
                                .orElseThrow(() -> new IllegalStateException("No bonus tile present on turret"))
                                .getRewards()
                                .stream()
                                .map(AmmoCube::getColor)
                                .collect(Collectors.toList()),
                        new Point(e.getLocation().getColumn(), e.getLocation().getRow())
                )
        );
    }

    /**
     * Notifies the client that a bonus tile was grabbed
     *
     * @param e the event corresponding to a bonus tile being grabbed
     */
    @Override
    public void onBonusTileGrabbed(BonusTileBoardEvent e) {
        it.polimi.ingsw.shared.events.networkevents.BonusTileEvent convertedEvent = mapBonusTileEvent(e);
        outputMessageQueue.add(Message.createEvent(ClientApi.BONUS_TILE_GRABBED_EVENT, convertedEvent));
    }

    /**
     * Notifies the client that a bonus tile was dropped
     *
     * @param e the event corresponding to a bonus tile being dropped
     */
    @Override
    public void onBonusTileDropped(BonusTileBoardEvent e) {
        it.polimi.ingsw.shared.events.networkevents.BonusTileEvent convertedEvent = mapBonusTileEvent(e);
        outputMessageQueue.add(Message.createEvent(ClientApi.BONUS_TILE_DROPPED_EVENT, convertedEvent));
    }

    /**
     * Notifies the client that a powerup was discarded
     *
     * @param e the event corresponding to the player discarding a powerup
     */
    @Override
    public void onPowerupDiscarded(PowerupExchange e){
        it.polimi.ingsw.shared.datatransferobjects.Player playerVM = mapPlayer(e.getPlayer());
        it.polimi.ingsw.shared.events.networkevents.PlayerWalletChanged convertedEvent;
        convertedEvent = new it.polimi.ingsw.shared.events.networkevents.PlayerWalletChanged(playerVM, "Powerup " + e.getPowerupTile().getColor().toString().toLowerCase() + " " + e.getPowerupTile().getName() + " was discarded");
        outputMessageQueue.add(Message.createEvent(ClientApi.PLAYER_WALLET_CHANGED_EVENT, convertedEvent));
    }

    /**
     * Notifies the client that a powerup was grabbed
     *
     * @param e the event corresponding to the player grabbing a powerup
     */
    @Override
    public void onPowerupGrabbed(PowerupExchange e){
        it.polimi.ingsw.shared.events.networkevents.PlayerWalletChanged convertedEvent;
        it.polimi.ingsw.shared.datatransferobjects.Player playerVM = mapPlayer(e.getPlayer());
        convertedEvent = new it.polimi.ingsw.shared.events.networkevents.PlayerWalletChanged(playerVM, playerVM.getNickname() + " grabbed a " + e.getPowerupTile().getColor().toString().toLowerCase() + " " + e.getPowerupTile().getName());
        outputMessageQueue.add(Message.createEvent(ClientApi.PLAYER_WALLET_CHANGED_EVENT, convertedEvent));
    }

    /**
     * Notifies the client that a spawnpoint was chosen by a player
     *
     * @param e the event corresponding to the player choosing a spawnpoint
     */
    @Override
    public void onSpawnpointChosen(SpawnpointChoiceEvent e){
        it.polimi.ingsw.shared.events.networkevents.PlayerSpawned convertedEvent;
        convertedEvent = new it.polimi.ingsw.shared.events.networkevents.PlayerSpawned(mapPlayer(e.getPlayer()), e.getDestination().getRow(), e.getDestination().getColumn());
        outputMessageQueue.add(Message.createEvent(ClientApi.PLAYER_SPAWNED_EVENT, convertedEvent));
    }

    /**
     * Notifies the client that the active player changed
     *
     * @param e the event corresponding to the changing of the turn
     */
    @Override
    public void onActivePlayerChanged(PlayerEvent e) {
        onBasicPlayerEvent(e, ClientApi.ACTIVE_PLAYER_CHANGED_EVENT);
    }

    /**
     * Generic method to convert PlayerEvents from the model into the appropriate network events
     *
     * @param e the model event
     * @param type the type of event
     */
    private void onBasicPlayerEvent(PlayerEvent e, ClientApi type) {
        it.polimi.ingsw.shared.datatransferobjects.Player playerVM = mapPlayer(e.getPlayer());
        it.polimi.ingsw.shared.events.networkevents.PlayerEvent convertedEvent = new it.polimi.ingsw.shared.events.networkevents.PlayerEvent(playerVM);
        outputMessageQueue.add(Message.createEvent(type, convertedEvent));
    }

    /**
     * Adds ViewListener
     * @param l the listener to add
     */
    public void addViewListener(ViewListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    /**
     * Removed ViewListener
     * @param l the listener to remove
     */
    public void removeViewListener(ViewListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    /**
     * Notifies all the ViewListener that the view has been disconnected
     */
    private void notifyViewDisconnected() {
        ViewEvent e = new ViewEvent(this);
        synchronized (listeners) {
            // Cloned list to prevent ConcurrentModificationException
            new LinkedList<>(listeners).forEach(l -> l.onViewDisconnected(e));
        }
        onViewDisconnected(e);
    }

    /**
     * Notifies all the ViewListener that the view is ready
     */
    private void notifyViewReady() {
        ViewEvent e = new ViewEvent(this);
        synchronized (listeners) {
            listeners.forEach(l -> l.onViewReady(e));
        }
    }

    /**
     * Sets ready status to null
     */
    public void setReady() {
        setReady(null);
    }

    /**
     * Sets the ready status to the given value
     *
     * @param resumedMatch the new ready status value
     */
    public void setReady(@Nullable Match resumedMatch) {
        notifyViewReady();
        onViewReady(new ViewEvent(this));
        if (resumedMatch != null) {
            enqueueMatchInitializationEvent(resumedMatch, true);
        }
    }

    /**
     * Notifies the client that a view disconnected
     * @param e the event corresponding to the view disconnection
     */
    @Override
    public void onViewDisconnected(ViewEvent e) {
        synchronized (listeners) {
            listeners.remove(e.getView()); // Cleaning up, there's no interest for further communications from disconnected views
        }
        outputMessageQueue.add(
                Message.createEvent(
                        ClientApi.CLIENT_DISCONNECTED_EVENT,
                        new it.polimi.ingsw.shared.events.networkevents.ClientEvent(e.getView().getNickname())
                )
        );
    }

    /**
     * Notifies the client that a new view is ready
     * @param e the event corresponding to the view being ready
     */
    @Override
    public void onViewReady(ViewEvent e) {
        outputMessageQueue.add(Message.createEvent(ClientApi.LOGIN_SUCCESS_EVENT, new ClientEvent(e.getView().getNickname())));
    }
}
