package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.player.Player;

import java.util.Optional;
import java.util.function.BiFunction;

public class ActionConfig {
    private final TargetCalculator calculator;
    private final Range range;
    private final Attack.ActionScope fieldOfAction;
    private final Attack.ActionType actionType;
    private final boolean skippable;
    private final BiFunction<Communicator, Player, Block> startingBlockProvider;

    public ActionConfig(Optional<TargetCalculator> calculator, Range range, Attack.ActionScope fieldOfAction, Attack.ActionType actionType, boolean skippable, BiFunction<Communicator, Player, Block> startingBlockProvider) {
        this.calculator = calculator.orElse(null);
        this.range = range;
        this.fieldOfAction = fieldOfAction;
        this.actionType = actionType;
        this.skippable = skippable;
        this.startingBlockProvider = startingBlockProvider;
    }

    public ActionConfig(Optional<TargetCalculator> calculator, Range range, Attack.ActionScope fieldOfAction, Attack.ActionType actionType, boolean skippable) {
        this(calculator, range, fieldOfAction, actionType, skippable, ((communicator, player) -> player.getBlock()));
    }

    public Optional<TargetCalculator> getCalculator() {
        return Optional.ofNullable(calculator);
    }

    public Range getRange() {
        return range;
    }

    public Attack.ActionScope getFieldOfAction() {
        return fieldOfAction;
    }

    public Attack.ActionType getActionType() {
        return actionType;
    }

    public boolean isSkippable() {
        return skippable;
    }

    public Block getStartingBlock(Communicator communicator, Player player) {
        return startingBlockProvider.apply(communicator, player);
    }
}
