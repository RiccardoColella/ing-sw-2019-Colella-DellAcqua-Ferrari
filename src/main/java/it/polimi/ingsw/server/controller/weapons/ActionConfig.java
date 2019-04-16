package it.polimi.ingsw.server.controller.weapons;

import java.util.Optional;

public class ActionConfig {
    private final TargetCalculator calculator;
    private final Range range;
    private final Attack.ActionScope fieldOfAction;
    private final Attack.ActionType actionType;
    private final boolean skippable;

    public ActionConfig(Optional<TargetCalculator> calculator, Range range, Attack.ActionScope fieldOfAction, Attack.ActionType actionType, boolean skippable) {
        this.calculator = calculator.orElse(null);
        this.range = range;
        this.fieldOfAction = fieldOfAction;
        this.actionType = actionType;
        this.skippable = skippable;
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
}
