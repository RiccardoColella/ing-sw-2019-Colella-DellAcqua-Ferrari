package it.polimi.ingsw.server.model.match;

import it.polimi.ingsw.server.model.battlefield.BoardFactory;
import it.polimi.ingsw.server.model.match.Match;
import it.polimi.ingsw.server.model.match.MatchFactory;
import it.polimi.ingsw.server.model.player.DamageToken;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.player.PlayerColor;
import it.polimi.ingsw.server.model.player.PlayerInfo;
import it.polimi.ingsw.server.model.rewards.Reward;
import it.polimi.ingsw.server.model.rewards.RewardFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class MatchTest {

    private Match match;

    @BeforeEach
    void setUp() {
        List<PlayerInfo> playerInfos = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            playerInfos.add(new PlayerInfo("Player" + i, PlayerColor.values()[i]));
        }
        this.match = MatchFactory.create(playerInfos, BoardFactory.Preset.BOARD_1, 5, Match.Mode.STANDARD);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void changeTurn() {

        for (int i = 0; i < match.getPlayers().size() + 1; i++) {
            assertSame(match.getActivePlayer(), match.getPlayers().get(i % match.getPlayers().size()), "Active player mismatch");
            match.changeTurn();
        }
    }


    @Test
    void endTurn() {
        match.getPlayers().forEach(player -> player
                .addDamageTokens(IntStream.range(0, 10)
                        .boxed()
                        .map(x -> new DamageToken(match.getPlayers().get(1)))
                        .collect(Collectors.toList()))
        );

        match.getPlayers().forEach(player -> player.addDamageToken(new DamageToken(match.getPlayers().get(1))));
        List<Player> deadPlayers = match.endTurn();
        assertEquals(match.getPlayers().size(), deadPlayers.size(), "endTurn didn't provide the correct amount of dead players");
        deadPlayers.forEach(Player::bringBackToLife);


        match.getPlayers().get(1).addDamageTokens(
                IntStream.range(0, 10)
                        .boxed()
                        .map(x -> new DamageToken(match.getPlayers().get(1)))
                        .collect(Collectors.toList())
        );
        match.getPlayers().get(1).addDamageToken(new DamageToken(match.getPlayers().get(1)));
        deadPlayers = match.endTurn();
        assertEquals(1, deadPlayers.size(), "endTurn didn't provide the correct amount of dead players");
        deadPlayers.forEach(Player::bringBackToLife);
    }

    @Test
    void onPlayerDied() {
        match.getPlayers().forEach(player -> player
                .addDamageTokens(IntStream.range(0, 10)
                .boxed()
                .map(x -> new DamageToken(match.getPlayers().get(1)))
                .collect(Collectors.toList()))
        );

        int matchSkulls = match.getRemainingSkulls();
        int victimSkulls = match.getPlayers().get(1).getSkulls();
        match.getPlayers().get(1).addDamageToken(new DamageToken(match.getActivePlayer()));
        match.endTurn();
        match.getPlayers().get(1).bringBackToLife();
        matchSkulls--;
        victimSkulls++;
        assertEquals(matchSkulls, match.getRemainingSkulls(), "Skull not subtracted from the match");
        assertEquals(victimSkulls, match.getPlayers().get(1).getSkulls(), "Skull not added to the victim");
        match.changeTurn();

        int killerMarks = match.getActivePlayer().getMarks().size();
        victimSkulls = match.getPlayers().get(0).getSkulls();
        match.getPlayers().get(0).addDamageTokens(Arrays.asList(new DamageToken(match.getActivePlayer()), new DamageToken(match.getActivePlayer())));
        match.endTurn();
        match.getPlayers().get(0).bringBackToLife();
        matchSkulls--;
        victimSkulls++;
        killerMarks++;
        assertEquals(matchSkulls, match.getRemainingSkulls(), "Skull not subtracted from the match");
        assertEquals(victimSkulls, match.getPlayers().get(0).getSkulls(), "Skull not added to the victim");
        assertEquals(killerMarks, match.getActivePlayer().getMarks().size(), "No mark assigned to the player who did an overkill");
        match.changeTurn();
    }

    @Test
    void scoring() {
        Player target = match.getPlayers().get(4);
        List<DamageToken> tokens = new LinkedList<>();
        // PLAYER 0 GIVES 3 DAMAGE TO PLAYER 4
        for (int i = 0; i < 3; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }
        target.addDamageTokens(tokens);
        tokens.clear();
        // CHANGE TURN
        match.endTurn();
        match.changeTurn();
        // PLAYER 1 GIVES 4 DAMAGE TO PLAYER 4
        for (int i = 0; i < 4; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }
        target.addDamageTokens(tokens);
        tokens.clear();
        // CHANGE TURN
        match.endTurn();
        match.changeTurn();
        // PLAYER 2 GIVES 3 DAMAGE TO PLAYER 4
        for (int i = 0; i < 3; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }
        target.addDamageTokens(tokens);
        tokens.clear();
        // CHANGE TURN
        match.endTurn();
        match.changeTurn();
        // PLAYER 1 GIVES 2 DAMAGE TO PLAYER 4, OVERKILLING HIM
        for (int i = 0; i < 2; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }
        target.addDamageTokens(tokens);
        tokens.clear();
        // CHANGE TURN
        match.endTurn();
        // BEST SHOOTERS: 1 - 0 - 2 - 3
        int[] points = new int[5];
        assertEquals(
                RewardFactory.create(RewardFactory.Type.STANDARD).getRewardFor(0, false),
                match.getPlayers().get(1).getPoints(),
                "Wrong score for Player 1"
        );
        assertEquals(
                RewardFactory.create(RewardFactory.Type.STANDARD).getRewardFor(1, true),
                match.getPlayers().get(0).getPoints(),
                "Wrong score for Player 0"
        );
        assertEquals(
                RewardFactory.create(RewardFactory.Type.STANDARD).getRewardFor(2, false),
                match.getPlayers().get(2).getPoints(),
                "Wrong score for Player 2"
        );
        assertEquals(
                RewardFactory.create(RewardFactory.Type.STANDARD).getRewardFor(3, false),
                match.getPlayers().get(3).getPoints(),
                "Wrong score for Player 3"
        );
        assertEquals(
                0,
                target.getPoints(),
                "Wrong score for Player 4"
        );
        target.bringBackToLife();
        match.changeTurn();
        for (int i = 0; i < 5; i++) {
            points[i] = match.getPlayers().get(i).getPoints();
        }

        // testing a double kill, Player 4 is active
        for (int i = 0; i < 11; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }
        target = match.getPlayers().get(0); // PLAYER 0 SHOULD BE KILLED
        target.addDamageTokens(tokens);
        target = match.getPlayers().get(3); // PLAYER 3 SHOULD BE OVERKILLED BECAUSE HE HAS A MARK FROM 4
        target.addDamageTokens(tokens);
        tokens.clear();
        List<Player> dead = match.endTurn();
        // BEST AND ONLY SHOOTER - PLAYER 4
        points[4] += RewardFactory.create(RewardFactory.Type.STANDARD).getRewardFor(0, true) +
                RewardFactory.create(RewardFactory.Type.STANDARD).getRewardFor(0, true) +
                RewardFactory.create(RewardFactory.Type.DOUBLE_KILL).getRewardFor(0);

        for (int i = 0; i < 5; i++) {
            assertEquals(
                    points[i],
                    match.getPlayers().get(i).getPoints(),
                    "Wrong score for Player " + i
            );
        }
        dead.forEach(Player::bringBackToLife);
        match.changeTurn();

        for (int i = 0; i < 11; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }
        target = match.getPlayers().get(2);
        target.addDamageTokens(tokens);
        tokens.clear();

        match.endTurn();
        // BEST AND ONLY SHOOTER - PLAYER 0
        points[0] += RewardFactory.create(RewardFactory.Type.STANDARD).getRewardFor(0, true);

        for (int i = 0; i < 5; i++) {
            assertEquals(
                    points[i],
                    match.getPlayers().get(i).getPoints(),
                    "Wrong score for Player " + i
            );
        }
        target.bringBackToLife();
        match.changeTurn();
        for (int i = 0; i < 11; i++) {
            tokens.add(new DamageToken(match.getActivePlayer()));
        }
        target.addDamageTokens(tokens);
        tokens.clear();
        match.endTurn();
        points[1] += RewardFactory.create(RewardFactory.Type.STANDARD).getRewardFor(target.getSkulls() - 1, true); // player 2 has already died before
        for (int i = 0; i < 5; i++) {
            assertEquals(
                    points[i],
                    match.getPlayers().get(i).getPoints(),
                    "Wrong score for Player " + i
            );
        }
        target.bringBackToLife();

        match.changeTurn();

        for (int i = 0; i < 5; i++) {
            match.endTurn();
            match.changeTurn();
        }

        //PLAYER 0 : 1 KILL 3^
        //PLAYER 1 : 1 KILL 4^
        //PLAYER 2 : 0
        //PLAYER 3 : 1 OVERKILL 2^
        //PLAYER 4 : 1 KILL 1 OVERKILL 1^
        Reward reward = RewardFactory.create(RewardFactory.Type.KILLSHOT);
        points[0] += reward.getRewardFor(2);
        points[1] += reward.getRewardFor(3);
        points[3] += reward.getRewardFor(1);
        points[4] += reward.getRewardFor(0);

        for (int i = 0; i < 5; i++) {
            assertEquals(
                    points[i],
                    match.getPlayers().get(i).getPoints(),
                    "Wrong score for Player " + i
            );
        }
    }
}