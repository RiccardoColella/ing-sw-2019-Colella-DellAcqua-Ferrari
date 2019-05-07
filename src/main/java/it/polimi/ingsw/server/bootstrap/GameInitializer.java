package it.polimi.ingsw.server.bootstrap;

import it.polimi.ingsw.server.controller.Controller;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.MatchFactory;
import it.polimi.ingsw.server.view.View;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class initializes a Game waiting for the correct amount of views from the waiting room
 * to create a controller which will handle the match
 *
 * @author Carlo Dell'Acqua
 */
public class GameInitializer {
    /**
     * Delay between waiting room queries
     */
    private static final int WAITING_ROOM_QUERY_DELAY = 1000;

    /**
     * Time to wait before automatically start a match when the minimum number of participants is available
     */
    private int matchStartTimeoutMilliseconds;

    /**
     * Maximum number of participant. When this number is reached a new match starts without waiting the timeout
     */
    private int maxParticipants;

    /**
     * The minimum number of participants needed to start a match
     */
    private int minParticipants;

    /**
     * This list accumulates the participants waiting for a match
     */
    private final List<View> participants = new LinkedList<>();

    /**
     * The waiting room that will be queried periodically to get new participants
     */
    private WaitingRoom participantSource;

    /**
     * Constructs a game initializer
     *
     * @param participantSource a waiting room to take participants from
     * @param matchStartTimeoutMilliseconds the time to wait before starting a match with at least the minimum number of participants
     * @param minParticipants the minimum number of participants
     * @param maxParticipants the maximum number of participants
     */
    public GameInitializer(WaitingRoom participantSource, int matchStartTimeoutMilliseconds, int minParticipants, int maxParticipants) {
        this.matchStartTimeoutMilliseconds = matchStartTimeoutMilliseconds;
        this.minParticipants = minParticipants;
        this.maxParticipants = maxParticipants;
        this.participantSource = participantSource;
    }

    /**
     * Starts a blocking loop that will wait until the conditions needed to create a match are fulfilled
     *
     * @return a Controller instance initialized with a valid Match and the Views
     * @throws InterruptedException if the loop is forced to stop
     */
    public Controller initialize() throws InterruptedException {

        java.time.Instant deadline = Instant.now().plus(Duration.ofMillis(matchStartTimeoutMilliseconds));

        while (
            (deadline.isAfter(Instant.now()) || participants.size() < minParticipants) &&
            participants.size() != maxParticipants
        ) {
            Thread.sleep(WAITING_ROOM_QUERY_DELAY);

            participantSource.pop().ifPresent(participants::add);
            participants.removeIf(view -> !view.isConnected());

            if (participants.size() < minParticipants) {
                deadline = Instant.now().plus(Duration.ofMillis(matchStartTimeoutMilliseconds));
            }
        }
        Match match = MatchFactory.create(
                participants.stream()
                        .map(View::getNickname)
                        .collect(Collectors.toList()),
                playersChoice(participants.stream().map(View::getChosenPreset)),
                playersChoice(participants.stream().map(View::getChosenSkulls)),
                playersChoice(participants.stream().map(View::getChosenMode))
        );
        participants.forEach(match::addMatchListener);
        return new Controller(
                match,
                participants
        );
    }

    /**
     * Determines the result of the player votes about some choices regarding the match initial setup
     *
     * @param choices a stream of players' choices
     * @param <T> the type of those choices
     * @return the winning choice
     */
    private <T> T playersChoice(Stream<T> choices) {
        return choices
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted((o1, o2) -> -1 * Long.compare(o1.getValue(), o2.getValue()))
                .collect(Collectors.toList())
                .get(0)
                .getKey();
    }
}