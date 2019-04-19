package it.polimi.ingsw.server.controller.weapons;

import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.battlefield.Direction;
import it.polimi.ingsw.server.model.currency.AmmoCube;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.player.DamageToken;
import it.polimi.ingsw.server.model.player.Damageable;
import it.polimi.ingsw.server.model.player.Player;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This class schematizes an attack, which is the effect that a weapon has on one or more targets
 */
public class Attack {

    enum ActionType {
        MOVE,
        MARK,
        DAMAGE
    }

    protected final String name;
    protected final List<ActionConfig> actionConfigs;
    protected final Board board;
    protected boolean basicFirst;
    protected final List<Coin> cost;

    public Attack(String name, List<ActionConfig> actionConfigs, Board board, List<Coin> cost) {
        this.name = name;
        this.actionConfigs = Collections.unmodifiableList(actionConfigs);
        this.board = board;
        this.basicFirst = false;
        this.cost = cost;
    }

    public Attack(Attack toCopy, boolean basicFirst) {
        this(toCopy.name, toCopy.actionConfigs, toCopy.board, toCopy.cost);
        this.basicFirst = basicFirst;
    }

    /**
     * This method returns a String representing the name of the Attack
     * The name is a Unique Identifier for the attack and must be used to implement hashCode and equals in classes which implement this interface
     *
     * @return the name of the attack
     */
    public String getName() {
        return this.name;
    }

    public final void execute(Communicator communicator, BasicWeapon weapon) {

        for (ActionConfig actionConfig : actionConfigs) {
            Set<Block> potentialStartingPoints = getPotentialStartingPoints(actionConfig, weapon);
            Block startingPoint;
            if (potentialStartingPoints.size() > 1) {
                startingPoint = communicator.select(potentialStartingPoints);
            } else {
                startingPoint = potentialStartingPoints.iterator().next();
            }
            Set<Set<Player>> potentialTargets = getPotentialTargets(startingPoint, actionConfig, weapon);
            Optional<Set<Player>> chosenSet;
            if (potentialTargets.isEmpty() && actionConfig.isSkippable()) {
                chosenSet = Optional.empty();
            } else if (potentialTargets.size() == 1 && !actionConfig.isSkippable()) {
                chosenSet = Optional.of(potentialTargets.iterator().next());
            } else if (potentialTargets.isEmpty()) {
                throw new IllegalStateException("No players to hit");
            } else if (actionConfig.isSkippable()) {
                chosenSet = communicator.selectOptional(potentialTargets);
            } else {
                chosenSet = Optional.of(communicator.select(potentialTargets));
            }
            if (!chosenSet.isPresent()) {
                //TODO: attack is over
            } else {
                handleAction(communicator, actionConfig, weapon.getCurrentShooter(), chosenSet.get());
                weapon.addHitTargets(chosenSet.get());
            }
        }
    }

    protected Set<Block> getPotentialStartingPoints(ActionConfig actionConfig, BasicWeapon weapon) {
        return actionConfig.updateStartingPoint(weapon);
    }

    protected Set<Set<Player>> getPotentialTargets(Block startingPoint, ActionConfig actionConfig, BasicWeapon weapon) {
        Set<Player> computedTargets = null;
        Optional<TargetCalculator> calculator = actionConfig.getCalculator();
        if (calculator.isPresent()) {
            computedTargets = calculator.get().computeTargets(startingPoint);
        }
        if (computedTargets != null) {
            Set<Player> availableTargets = actionConfig.getTargetsToChooseFrom(weapon);
            computedTargets.removeIf(player -> !availableTargets.contains(player));
        } else {
            computedTargets = actionConfig.getBonusTargets(weapon.getAllTargets(), weapon.getCurrentShooter());
        }
        Set<Set<Player>> potentialTargets = actionConfig.adaptToScope(computedTargets);
        potentialTargets = actionConfig.addToEach(potentialTargets, weapon);
        return actionConfig.applyVeto(potentialTargets, weapon.getAllTargets());
    }

    protected void handleMovement(Communicator communicator, Range range, Set<Player> targets) {
        for (Player target : targets) {
            boolean hasDecidedToStop = false;
            for (int i = 0; i <= range.getMax() && !hasDecidedToStop; i++) {
                //move is mandatory
                Block start = board.findPlayer(target).orElseThrow(() -> new IllegalStateException("Player is not on this board"));
                List<Block> neighbors = new ArrayList<>();
                Arrays.stream(Direction.values()).filter(dir -> start.getBoarderType(dir) != Block.BorderType.WALL).forEach(dir -> board.getBlockNeighbor(start, dir).ifPresent(neighbors::add));
                Optional<Block> chosen;
                if (i < range.getMin()) {
                    chosen = Optional.of(communicator.select(neighbors));
                } else {
                    chosen = communicator.selectOptional(neighbors);
                }
                if (chosen.isPresent()) {
                    board.teleportPlayer(target, chosen.get());
                } else {
                    hasDecidedToStop = true;
                }
            }
        }
    }

    private void handleAction(Communicator communicator, ActionConfig currentConfig, Player activePlayer, Set<Player> chosenTargets) {
        //the range is wider than 1 only in moves, so for the others we can assume actual = max
        int actualAmount = currentConfig.getRange().getMax();

        switch (currentConfig.getActionType()) {
            case MARK:
                List<DamageToken> marks = new LinkedList<>();
                for (int j = 0; j < actualAmount; j++) {
                    marks.add(new DamageToken(activePlayer));
                }
                chosenTargets.forEach(target -> target.addMarks(marks));
                break;
            case DAMAGE:
                List<DamageToken> damageTokens = new LinkedList<>();
                for (int j = 0; j < actualAmount; j++) {
                    damageTokens.add(new DamageToken(activePlayer));
                }
                chosenTargets.forEach(target -> target.addDamageTokens(damageTokens));
                break;
            case MOVE:
                handleMovement(communicator, currentConfig.getRange(), chosenTargets);
                break;
            default:
                throw new EnumConstantNotPresentException(ActionType.class, "Action Type " + currentConfig.getActionType() + " is unknown");
        }
    }

    public Set<Player> getLastHit() {
        return new HashSet<>();
    }

    public List<Coin> getCost() {
        return cost;
    }
}

