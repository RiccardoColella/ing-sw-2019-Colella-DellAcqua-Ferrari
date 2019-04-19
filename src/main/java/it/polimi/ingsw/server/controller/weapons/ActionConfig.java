package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.weapons.Weapon;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ActionConfig {
    private final TargetCalculator calculator;
    private final Range range;
    private final Attack.ActionType actionType;
    private final boolean skippable;
    private final BiFunction<List<Player>, Player, Set<Player>> bonusTargets;
    private final Function<BasicWeapon, Set<Player>> targetsToChooseFrom;
    private final Function<Set<Player>, Set<Set<Player>>> adaptToScope;
    private final BiFunction<Set<Set<Player>>, BasicWeapon, Set<Set<Player>>> addToEach;
    private final BiFunction<Set<Set<Player>>, List<Player>, Set<Set<Player>>> veto;
    private final Function<BasicWeapon, Set<Block>> startingPointUpdater;


    public ActionConfig(
            @Nullable TargetCalculator calculator,
            BiFunction<List<Player>, Player, Set<Player>> bonusTargets,
            Function<BasicWeapon, Set<Player>> targetsToChooseFrom,
            Function<Set<Player>, Set<Set<Player>>> adaptToScope,
            BiFunction<Set<Set<Player>>, BasicWeapon, Set<Set<Player>>> addToEach,
            BiFunction<Set<Set<Player>>, List<Player>, Set<Set<Player>>> veto,
            boolean skippable,
            Function<BasicWeapon, Set<Block>> startingPointUpdater,
            Attack.ActionType actionType,
            Range range) {
        this.calculator = calculator;
        this.range = range;
        this.actionType = actionType;
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

    public Range getRange() {
        return range;
    }

    public Attack.ActionType getActionType() {
        return actionType;
    }

    public boolean isSkippable() {
        return skippable;
    }

    public Set<Player> getBonusTargets(List<Player> previouslyHit, Player activePlayer) {
        return bonusTargets.apply(previouslyHit, activePlayer);
    }

    public Set<Player> getTargetsToChooseFrom(BasicWeapon currentWeapon) {
        return targetsToChooseFrom.apply(currentWeapon);
    }

    public Set<Set<Player>> adaptToScope(Set<Player> potentialTargets) {
        return adaptToScope.apply(potentialTargets);
    }

    public Set<Set<Player>> addToEach(Set<Set<Player>> potentialTargets, BasicWeapon weapon) {
        return addToEach.apply(potentialTargets, weapon);
    }

    public Set<Set<Player>> applyVeto(Set<Set<Player>> potentialTargets, List<Player> previouslyHit) {
        return veto.apply(potentialTargets, previouslyHit);
    }

    public Set<Block> updateStartingPoint(BasicWeapon weapon) {
        return startingPointUpdater.apply(weapon);
    }
}
