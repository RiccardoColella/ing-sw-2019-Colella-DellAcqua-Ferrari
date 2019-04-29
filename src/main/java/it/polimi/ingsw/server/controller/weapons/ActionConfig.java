package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.utils.TriConsumer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ActionConfig {
    private final TargetCalculator calculator;
    private final TriConsumer<Set<Player>, Interviewer, Weapon> executor;
    private final boolean skippable;
    private final BiFunction<List<Player>, Player, Set<Player>> bonusTargets;
    private final Function<Weapon, Set<Player>> targetsToChooseFrom;
    private final Function<Set<Player>, Set<Set<Player>>> adaptToScope;
    private final BiFunction<Set<Set<Player>>, Weapon, Set<Set<Player>>> addToEach;
    private final BiFunction<Set<Set<Player>>, Weapon, Set<Set<Player>>> veto;
    private final Function<Weapon, Set<Block>> startingPointUpdater;


    public ActionConfig(
            @Nullable TargetCalculator calculator,
            BiFunction<List<Player>, Player, Set<Player>> bonusTargets,
            Function<Weapon, Set<Player>> targetsToChooseFrom,
            Function<Set<Player>, Set<Set<Player>>> adaptToScope,
            BiFunction<Set<Set<Player>>, Weapon, Set<Set<Player>>> addToEach,
            BiFunction<Set<Set<Player>>, Weapon, Set<Set<Player>>> veto,
            boolean skippable,
            Function<Weapon, Set<Block>> startingPointUpdater,
            TriConsumer<Set<Player>, Interviewer, Weapon> executor) {
        this.calculator = calculator;
        this.executor = executor;
        this.skippable = skippable;
        this.bonusTargets = bonusTargets;
        this.targetsToChooseFrom = targetsToChooseFrom;
        this.adaptToScope = adaptToScope;
        this.addToEach = addToEach;
        this.veto = veto;
        this.startingPointUpdater = startingPointUpdater;
    }

    public Optional<TargetCalculator> getCalculator() {
        return Optional.ofNullable(calculator);
    }

    public void execute(Set<Player> targets, Interviewer interviewer, Weapon weapon) {
        executor.apply(targets, interviewer, weapon);
    }

    public boolean isSkippable() {
        return skippable;
    }

    public Set<Player> getBonusTargets(List<Player> previouslyHit, Player activePlayer) {
        return bonusTargets.apply(previouslyHit, activePlayer);
    }

    public Set<Player> getTargetsToChooseFrom(Weapon currentWeapon) {
        return targetsToChooseFrom.apply(currentWeapon);
    }

    public Set<Set<Player>> adaptToScope(Set<Player> potentialTargets) {
        return adaptToScope.apply(potentialTargets);
    }

    public Set<Set<Player>> addToEach(Set<Set<Player>> potentialTargets, Weapon weapon) {
        return addToEach.apply(potentialTargets, weapon);
    }

    public Set<Set<Player>> applyVeto(Set<Set<Player>> potentialTargets, Weapon weapon) {
        return veto.apply(potentialTargets, weapon);
    }

    public Set<Block> updateStartingPoint(Weapon weapon) {
        return startingPointUpdater.apply(weapon);
    }
}
