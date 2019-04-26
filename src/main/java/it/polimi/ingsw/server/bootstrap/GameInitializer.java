package it.polimi.ingsw.server.bootstrap;

import it.polimi.ingsw.server.controller.Controller;
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

    private int acceptTimeout;
    private int matchStartTimeout;
    private int maxParticipants;
    private int minParticipants;
    private final List<View> participants = new LinkedList<>();
    private WaitingRoom participantSource;


    public GameInitializer(WaitingRoom participantSource, int matchStartTimeout, int acceptTimeout, int minParticipants, int maxParticipants) {
        this.matchStartTimeout = matchStartTimeout;
        this.acceptTimeout = acceptTimeout;
        this.minParticipants = minParticipants;
        this.maxParticipants = maxParticipants;
        this.participantSource = participantSource;
    }

    public Controller initialize() throws InterruptedException {

        java.time.Instant deadline = Instant.now().plus(Duration.ofMillis(matchStartTimeout));

        while (
            (deadline.isAfter(Instant.now()) || participants.size() < minParticipants) &&
            participants.size() != maxParticipants
        ) {
            Thread.sleep(acceptTimeout);

            participantSource.pop().ifPresent(participants::add);
            participants.removeIf(view -> !view.isConnected());

            if (participants.size() < minParticipants) {
                deadline = Instant.now().plus(Duration.ofMillis(matchStartTimeout));
            }
        }

        return new Controller(
                MatchFactory.create(
                        participants.stream()
                                .map(View::getPlayerInfo)
                                .collect(Collectors.toList()),
                        playersChoice(participants.stream().map(View::getChosenPreset)),
                        playersChoice(participants.stream().map(View::getChosenSkulls)),
                        playersChoice(participants.stream().map(View::getChosenMode))
                ),
                participants
        );
    }

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