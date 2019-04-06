package it.polimi.ingsw.server.model.weapons.attackeffects;

import it.polimi.ingsw.server.model.DamageToken;
import it.polimi.ingsw.server.model.Damageable;
import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.weapons.TargetCalculator;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AttackEffect {

    private Board board;
    private int targetDamages;
    private int targetMarks;
    private Block playerTargetNewLocation = null;

    public AttackEffect(
            Board board,
            TargetCalculator targetCalculator,
            int targetDamages,
            int targetMarks
    ) {
        this.board = board;
        this.targetDamages = targetDamages;
        this.targetMarks = targetMarks;
    }

    public void setPlayerTargetNewLocation(Block playerTargetNewLocation) {
        this.playerTargetNewLocation = playerTargetNewLocation;
    }

    public void execute(Player attacker, List<Damageable> targets) {
        if (playerTargetNewLocation != null) {
            targets.stream()
                .filter(target -> target instanceof Player)
                .forEach(target ->
                        board.teleportPlayer(((Player) target), playerTargetNewLocation)
                );
        }

        targets.stream()
            .forEach(target ->
                target.addDamageTokens(
                    IntStream.range(0, targetDamages)
                        .mapToObj(i -> new DamageToken(attacker)).collect(Collectors.toList())
                    )
            );

        targets.stream()
            .filter(target -> target instanceof Player)
            .forEach(target ->
                ((Player) target)
                    .addMarks(
                            IntStream.range(0, targetMarks)
                                    .mapToObj(i -> new DamageToken(attacker)).collect(Collectors.toList())
                    )
            );
    }
}
