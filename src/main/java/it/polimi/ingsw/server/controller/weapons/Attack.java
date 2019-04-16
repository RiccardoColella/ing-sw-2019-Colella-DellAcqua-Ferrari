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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This class schematizes an attack, which is the effect that a weapon has on one or more targets
 */
public abstract class Attack implements Iterator {

    /**
     * This enum differentiates the possible type of targets of an attack
     */
    enum TargetType {
        TYPE_1,
        TYPE_2
    }

    enum ActionType {
        MOVE,
        MARK,
        DAMAGE
    }

    enum ActionScope {
        ONE_PLAYER,
        BLOCKWIDE,
        ROOMWIDE
    }

    enum BonusTargetCombination {
        ADD_TO_EACH,
        ADD_AS_NEW
    }

    protected final String name;
    protected final ActionConfig[] actionConfigs;
    protected Map<Player, BonusTargetCombination> bonusTargets;
    protected Set<Player> availableTargets;
    protected Board board;
    protected int currentActionIndex;

    public Attack(String name, ActionConfig[] actionConfigs, Board board) {
        this.name = name;
        this.actionConfigs = actionConfigs;
        this.bonusTargets = new HashMap<>();
        this.availableTargets = new HashSet<>();
        this.board = board;
        this.currentActionIndex = 0;
    }

    /**
     * This method returns a list of list of Damageable that the attack can target in one shot
     *
     * @param type the type of target which is to be attacked
     * @return a list of the groups of targets that can be attacked, which will be empty if none are available
     */
    public abstract List<List<Damageable>> getTargets(TargetType type);

    /**
     * This method returns a String representing the name of the Attack
     * The name is a Unique Identifier for the attack and must be used to implement hashCode and equals in classes which implement this interface
     *
     * @return the name of the attack
     */
    public String getName() {
        return this.name;
    }

    /**
     * This method returns the cost of the attack
     * @return the cost that shall be paid to use this attack, the list will be empty if the attack is free
     */
    public abstract List<Coin> getCost();

    /**
     * This method strikes the attack and returns the damageables this attack was dealt on
     *
     * @param attacker the player who is executing the attack
     * @param targets a Map associating target types to a list of damageable needed to determine the effect
     * @return the list of Damageable affected by this attack, the list will be empty if no one was affected
     */
    public abstract List<Damageable> execute(Player attacker, Map<TargetType, List<Damageable>> targets);

    public abstract TargetType getSupportedTargetTypes();

    public void execute(Communicator communicator, Block startingPoint, Player activePlayer) {
        Optional<Set<Player>> chosenTargets;
        List<Set<Player>> targets = getPotentialTargets(startingPoint);
        if (!actionConfigs[currentActionIndex].isSkippable() && targets.isEmpty()) {
            //non skippable actions must have targets
            throw new IllegalStateException("Player should not have been allowed to pick a weapon with an unusable attack");
        }
        else if (actionConfigs[currentActionIndex].isSkippable() && !targets.isEmpty()) {
            //if the action is skippable, feedback from the player is needed for any amount greater than 0 of sets of targets
            chosenTargets = communicator.selectOptional(targets);
        } else if (targets.size() > 1) {
            chosenTargets = Optional.of(communicator.select(targets));
        } else if (targets.size() == 1) {
            chosenTargets = Optional.of(targets.get(0));
        } else {
            //no targets could be chosen
            chosenTargets = Optional.empty();
        }
        chosenTargets.ifPresent(players -> handleAction(communicator, actionConfigs[currentActionIndex], activePlayer, players));
        currentActionIndex++;
    }

    public List<Set<Player>> getPotentialTargets(Block startingPoint) {
        List<Set<Player>> potentialTargets = new LinkedList<>();
        //if there is a calculator, we get all the targets available based on their position and then intersect them with the available targets
        Optional<TargetCalculator> calculator = actionConfigs[currentActionIndex].getCalculator();
        if (calculator.isPresent()) {
            Set<Block> blocksWithPotentialTargets = calculator.get().computeTargets(startingPoint);
            potentialTargets = handleFieldOfAction(blocksWithPotentialTargets, actionConfigs[currentActionIndex].getFieldOfAction());
        }
        if (!bonusTargets.isEmpty()) {
            handleBonusTargets(potentialTargets);
        }
        return potentialTargets;
    }

    private List<Set<Player>> handleFieldOfAction(Set<Block> blocksWithPotentialTargets, ActionScope fieldOfAction) {
        List<Set<Player>> potentialTargets;
        switch (fieldOfAction) {
            case ONE_PLAYER:
                Set<Player> targetSet = blocksWithPotentialTargets.stream()
                        .flatMap(block -> block.getPlayers().stream())
                        .filter(availableTargets::contains)
                        .collect(Collectors.toSet());
                potentialTargets = targetSet.stream().map(target -> {
                    Set<Player> set = new HashSet<>();
                    set.add(target);
                    return set;
                }).collect(Collectors.toList());
                break;
            case BLOCKWIDE:
                potentialTargets = blocksWithPotentialTargets.stream()
                        .map(Block::getPlayers)
                        .collect(Collectors.toList());
                for (Set<Player> set : potentialTargets) {
                    set.removeIf(t -> !availableTargets.contains(t));
                }
                break;
            case ROOMWIDE:
                //roomwide assumes that the target calculator returned only a block per room
                potentialTargets = blocksWithPotentialTargets.stream()
                        .map(block -> board.getRoom(block).stream()
                                .flatMap(b -> b.getPlayers().stream()).filter(availableTargets::contains)
                                .collect(Collectors.toSet()))
                        .collect(Collectors.toList());
                break;
            default:
                throw new EnumConstantNotPresentException(ActionScope.class, "Field of Action " + fieldOfAction.toString() + " is unknown");
        }
        return potentialTargets;
    }

    private void handleBonusTargets(List<Set<Player>> potentialTargets) {
        for (Map.Entry<Player, BonusTargetCombination> entry : bonusTargets.entrySet()) {
            Player target = entry.getKey();
            BonusTargetCombination wayToUse = entry.getValue();
            if (wayToUse == BonusTargetCombination.ADD_TO_EACH && !potentialTargets.isEmpty()) {
                for (Set<Player> set : potentialTargets) {
                    set.add(target);
                }
            } else if (wayToUse == BonusTargetCombination.ADD_AS_NEW || wayToUse == BonusTargetCombination.ADD_TO_EACH && bonusTargets.isEmpty()) {
                Set<Player> singletonSet = new HashSet<>();
                singletonSet.add(target);
                potentialTargets.add(singletonSet);
            }
        }
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

    public void addBonusTarget(Player target, BonusTargetCombination howToAdd) {
        this.bonusTargets.put(target, howToAdd);
    }

    public void removeBonusTarget(Player targetToRemove) {
        this.bonusTargets.remove(targetToRemove);
    }

    public void clearBonusTargets() {
        this.bonusTargets.clear();
    }

    public void addAvailableTargets(Player target) {
        this.availableTargets.add(target);
    }

    public void addAvailableTargets(Set<Player> targets) {
        this.availableTargets.addAll(targets);
    }

    public void clearAvailableTargets() {
        this.availableTargets.clear();
    }

    public void removeAvailableTarget(Player target) {
        this.availableTargets.remove(target);
    }

    @Override
    public boolean hasNext() {
        return currentActionIndex < actionConfigs.length;
    }

    @Override
    public Attack next() {
        if (this.hasNext()) {
            return this;
        } else throw new NoSuchElementException("No next element found");
    }

    public void rewind() {
        currentActionIndex = 0;
    }
}
