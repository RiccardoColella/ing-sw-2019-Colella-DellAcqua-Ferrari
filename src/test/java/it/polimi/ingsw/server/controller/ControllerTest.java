package it.polimi.ingsw.server.controller;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.MatchFactory;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.server.view.View;
import it.polimi.ingsw.shared.messages.ClientApi;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ControllerTest {

    private class Mockview extends View implements Interviewer {

        int index;
        Random random = new Random();

        /**
         * Constructs a server-side view
         *
         * @param answerTimeout     maximum timeout before considering the view disconnected
         * @param answerTimeoutUnit measurement unit of the timeout
         */
        public Mockview(int answerTimeout, TimeUnit answerTimeoutUnit) {
            super(answerTimeout, answerTimeoutUnit);
        }

        public Mockview(int index) {
            super(60, TimeUnit.SECONDS);
            this.index = index;
        }



        @Override
        public <T> T select(String questionText, Collection<T> options, ClientApi messageName) {
            List<T> optionsList = new ArrayList<>(options);
            int i = 0;
            while (i < index && i < optionsList.size() - 1){ i++; }
            return optionsList.get(i);
        }

        @Override
        public <T> Optional<T> selectOptional(String questionText, Collection<T> options, ClientApi messageName) {
            List<T> optionsList = new ArrayList<>(options);
            int i = random.nextInt(options.size() + 1);
            if (i >= options.size()) return Optional.empty();
            return Optional.of(optionsList.get(i));
        }

        public void setIndex(int newIndex){ index = newIndex; }

        @Override
        public void close() throws Exception {

        }
    }

    @Test
    void onPlayerDamaged() {
    }

    @Test
    void run() {
        List<String> playerNames = new ArrayList<>();
        List<View> mockViews = new ArrayList<>();
        for (int i = 0; i < 3; i++){
            playerNames.add("Player" + i);
            mockViews.add(new Mockview(3));
        }
        Match match = MatchFactory.create(playerNames, BoardFactory.Preset.BOARD_1, 8, Match.Mode.STANDARD);
        Controller controller = new Controller(match, mockViews);
        //controller.run();
    }
}