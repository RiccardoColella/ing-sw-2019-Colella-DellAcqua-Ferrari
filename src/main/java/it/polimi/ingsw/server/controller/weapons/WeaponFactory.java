package it.polimi.ingsw.server.controller.weapons;

import com.google.gson.*;
import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.server.model.currency.AmmoCubeFactory;
import it.polimi.ingsw.server.model.currency.Coin;
import it.polimi.ingsw.server.model.currency.CurrencyColor;
import it.polimi.ingsw.server.model.exceptions.IncoherentConfigurationException;
import it.polimi.ingsw.server.model.exceptions.MissingConfigurationFileException;
import it.polimi.ingsw.server.model.player.DamageToken;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.model.weapons.Weapon;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.utils.EnumValueByString;
import it.polimi.ingsw.utils.TriConsumer;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WeaponFactory {
    private static final String WEAPON_JSON_PATH = "./resources/attacks.json";

    private static Map<Weapon.Name, BasicWeapon> weaponMap;

    /**
     * Private empty constructor because this class should not have instances
     */
    private WeaponFactory() { }

    /**
     * This method is used to create any weapon
     * @param name the enum corresponding to the desired weapon
     * @return the weapon, ready to be bought
     */
    public static BasicWeapon create(Weapon.Name name, Board board) {
        if (weaponMap == null) {
            readFromFile(board);
        }
        return weaponMap.get(name);
    }

    private static void readFromFile(Board board) {
        weaponMap = new EnumMap<>(Weapon.Name.class);
        JsonElement jsonElement;
        try {
            jsonElement = new JsonParser().parse(new FileReader(new File(WEAPON_JSON_PATH)));
        } catch (FileNotFoundException e) {
            throw new MissingConfigurationFileException("Weapon configuration file not found");
        }

        jsonElement.getAsJsonArray().forEach(weapon -> readWeapon(weapon, board));
    }

    private static void readWeapon(JsonElement jsonElement, Board board) {
        JsonObject weaponObject = jsonElement.getAsJsonObject();
        Weapon.Name weaponId = EnumValueByString.findByString(weaponObject.get("weaponId").getAsString(), Weapon.Name.class);
        Attack basicAttack = readAttack(weaponObject.get("basicAttack").getAsJsonObject(), board);
        if (weaponObject.has("alternativeAttack")) {
            Attack alternativeAttack = readAttack(weaponObject.get("alternativeAttack").getAsJsonObject(), board);
            weaponMap.put(weaponId, new WeaponWithAlternative(weaponId, basicAttack, alternativeAttack));
        } else if (weaponObject.has("advancedAttacks")) {
            List<Attack> advancedAttacks = new LinkedList<>();
            weaponObject.get("advancedAttacks").getAsJsonArray().forEach(attack -> {
                Attack read = readAttack(attack, board);
                boolean basicFirst = attack.getAsJsonObject().has("basicMustBeDoneFirst") && attack.getAsJsonObject().get("basicMustBeDoneFirst").getAsBoolean();
                Attack advanced = new Attack(read, basicFirst);
                advancedAttacks.add(advanced);
            });
            boolean mustExecuteInOrder = weaponObject.has("mustExecuteInOrder") && weaponObject.get("mustExecuteInOrder").getAsBoolean();
            weaponMap.put(weaponId, new WeaponWithMultipleEffects(weaponId, basicAttack, advancedAttacks, mustExecuteInOrder));
        } else {
            weaponMap.put(weaponId, new BasicWeapon(weaponId, basicAttack));
        }
    }

    private static Attack readAttack(JsonElement jsonElement, Board board) {
        JsonObject attackObject = jsonElement.getAsJsonObject();
        String name = attackObject.get("name").getAsString();
        List<Coin> cost = new ArrayList<>();
        if (attackObject.has("cost")) {
            attackObject.get("cost").getAsJsonArray().forEach(color -> cost.add(AmmoCubeFactory.create(EnumValueByString.findByString(color.getAsString(), CurrencyColor.class))));
        }
        JsonArray actions = attackObject.get("actions").getAsJsonArray();
        TargetCalculator lastTargetCalculator = null;
        List<ActionConfig> actionConfigs = new ArrayList<>();
        for (JsonElement action : actions) {
            ActionConfig config = readAction(action, lastTargetCalculator, board);
            lastTargetCalculator = config.getCalculator().orElse(lastTargetCalculator);
            actionConfigs.add(config);
        }
        return new Attack(name, actionConfigs, board, cost);
    }

    private static ActionConfig readAction(JsonElement action, @Nullable TargetCalculator lastTargetCalculator, Board board) {
        JsonObject actionObject = action.getAsJsonObject();
        TargetCalculator targetCalculator = readTargetCalculator(actionObject, lastTargetCalculator, board);
        BiFunction<List<Player>, Player, Set<Player>> bonusTargets = readBonusTarget(actionObject);
        Function<BasicWeapon, Set<Player>> targetsToChooseFrom = readAvailableTargets(actionObject);
        Function<Set<Player>, Set<Set<Player>>> adaptToScope = readScope(actionObject);
        BiFunction<Set<Set<Player>>, BasicWeapon, Set<Set<Player>>> addToEach = readMandatoryExtras(actionObject);
        BiFunction<Set<Set<Player>>, List<Player>, Set<Set<Player>>> veto = readVeto(actionObject);
        boolean skippable = actionObject.get("skippable").getAsBoolean();
        Function<BasicWeapon, Set<Block>> startingPointUpdater = readStartingPoint(actionObject);
        TriConsumer<Set<Player>, Interviewer, BasicWeapon> executor = readActionType(actionObject, board.getBlocks().size());
        return new ActionConfig(
            targetCalculator,
            bonusTargets,
            targetsToChooseFrom,
            adaptToScope,
            addToEach,
            veto,
            skippable,
            startingPointUpdater,
            executor
        );
    }

    private static @Nullable TargetCalculator readTargetCalculator(JsonObject actionObject, @Nullable TargetCalculator lastTargetCalculator, Board board) {
        if ("FIND".equals(actionObject.get("targets").getAsString())) {
            return elaborateFindingTarget(actionObject, board, lastTargetCalculator);
        }
        return null;
    }

    private static BiFunction<List<Player>, Player, Set<Player>> readBonusTarget(JsonObject actionObject) {
        BiFunction<List<Player>, Player, Set<Player>> bonusTarget;
        switch (actionObject.get("targets").getAsString()) {
            case "INHERIT_LAST":
                bonusTarget = (previouslyHit, activePlayer) -> {
                    Set<Player> last = new HashSet<>();
                    int index = previouslyHit.size() - 1;
                    while (index > 0) {
                        if (previouslyHit.get(index) != activePlayer) {
                            last.add(previouslyHit.get(index));
                            return last;
                        }
                        index--;
                    }
                    return last;
                };
                break;
            case "SELF":
                bonusTarget = (previouslyHit, activePlayer) -> {
                    Set<Player> self = new HashSet<>();
                    self.add(activePlayer);
                    return self;
                };
                break;
            default:
                bonusTarget = (previouslyHit, activePlayer) -> new HashSet<>();
                break;
        }
        return bonusTarget;
    }

    private static Function<BasicWeapon, Set<Player>> readAvailableTargets(JsonObject actionObject) {
        Function<BasicWeapon, Set<Player>> availableTargets;
        if ("INHERIT_ALL".equals(actionObject.get("targets").getAsString())) {
            availableTargets = weapon -> {
                Set<Player> available = new HashSet<>(weapon.getAllTargets());
                available.remove(weapon.getCurrentShooter());
                return available;
            };
        } else {
            availableTargets = weapon -> {
                Set<Player> nonActive = new HashSet<>(weapon.getCurrentShooter().getMatch().getPlayers());
                nonActive.remove(weapon.getCurrentShooter());
                return nonActive;
            };
        }
        return availableTargets;
    }

    private static @Nullable TargetCalculator elaborateFindingTarget(JsonObject actionObject, Board board, @Nullable TargetCalculator lastTargetCalculator) {
        String field = "targetDistance";
        switch (actionObject.get("targetPosition").getAsString()) {
            case "FIXED":
                JsonObject distance = actionObject.get(field).getAsJsonObject();
                return new FixedDistanceTargetCalculator(board, computeRange(distance, board.getBlocks().size()));
            case "VISIBLE":
                boolean isVisible = true;
                if (actionObject.has("visibility")) {
                    isVisible = actionObject.get("visibility").getAsBoolean();
                }
                FixedVisibilityTargetCalculator visibilityTargetCalculator = new FixedVisibilityTargetCalculator(board, isVisible);
                if (actionObject.has(field)) {
                    distance = actionObject.get(field).getAsJsonObject();
                    FixedDistanceTargetCalculator distanceTargetCalculator = new FixedDistanceTargetCalculator(board, computeRange(distance, board.getBlocks().size()));
                    List<TargetCalculator> calculators = new ArrayList<>();
                    calculators.add(visibilityTargetCalculator);
                    calculators.add(distanceTargetCalculator);
                    return new CompoundTargetCalculator(calculators);
                }
                return visibilityTargetCalculator;
            case "STRAIGHT":
                boolean goesThroughWalls = false;
                if (actionObject.has("goesThroughWalls")) {
                    goesThroughWalls = actionObject.get("goesThroughWalls").getAsBoolean();
                }
                String direction = actionObject.get("targetDirection").getAsString();
                if (direction.equals("FIXED")) {
                    return new FixedDirectionTargetCalculator(board, goesThroughWalls);
                } else if (direction.equals("INHERIT")) {
                    return lastTargetCalculator;
                } else throw new IncoherentConfigurationException("Unknown value for targetDirection: " + direction);
            default:
                throw new IncoherentConfigurationException("Unknown value for targetPosition: " + actionObject.get("targetPosition").getAsString());
        }
    }

    private static Range computeRange(JsonObject distance, int maxAvailable) {
        int min = 0;
        int max = maxAvailable;
        if (distance.has("min")) {
            min = distance.get("min").getAsInt();
        }
        if (distance.has("max")) {
            max = distance.get("max").getAsInt();
        }
        return new Range(min, max);
    }

    private static Function<Set<Player>, Set<Set<Player>>> readScope(JsonObject actionObject) {
        Function<Set<Player>, Set<Set<Player>>> adaptToScope;
        switch (actionObject.get("targetAmount").getAsString()) {
            case "ONE":
                adaptToScope = targets -> {
                    Set<Set<Player>> adaptedTargets = new HashSet<>();
                    for (Player target : targets) {
                        adaptedTargets.add(new HashSet<>(Collections.singletonList(target)));
                    }
                    return adaptedTargets;
                };
                break;
            case "BLOCK":
                adaptToScope = targets -> {
                    Set<Set<Player>> adaptedTargets = new HashSet<>();
                    for (Player target: targets) {
                        adaptedTargets.add(new HashSet<>(target.getBlock().getPlayers()));
                    }
                    return adaptedTargets;
                };
                break;
            case "ROOM":
                adaptToScope = targets -> {
                    Set<Set<Player>> adaptedTargets = new HashSet<>();
                    for (Player target : targets) {
                        adaptedTargets.add(target
                              .getMatch()
                              .getBoard()
                              .getRoom(target.getBlock())
                              .stream()
                              .flatMap(block -> block.getPlayers().stream())
                              .collect(Collectors.toSet())
                        );
                    }
                    return adaptedTargets;
                };
                break;
            default:
                throw new IncoherentConfigurationException("Unrecognized targetAmount: " + actionObject.get("targetAmount").getAsString());
        }
        return adaptToScope;
    }

    private static BiFunction<Set<Set<Player>>, BasicWeapon, Set<Set<Player>>> readMandatoryExtras(JsonObject actionObject) {
        BiFunction<Set<Set<Player>>, BasicWeapon, Set<Set<Player>>> addToEach;
        String field = "andTargets";
        if (actionObject.has(field)) {
            if ("INCLUDE_LAST".equals(actionObject.get(field).toString())) {
                addToEach = (potentialTargets, weapon) -> {
                    Set<Set<Player>> result = new HashSet<>(potentialTargets);
                    Player toAdd = null;
                    for (int i = weapon.getAllTargets().size() - 1; i >= 0 && toAdd == null; i--) {
                        Player t = weapon.getAllTargets().get(i);
                        if (t != weapon.getCurrentShooter()) {
                            toAdd = t;
                        }
                    }
                    if (toAdd != null) {
                        if (result.isEmpty()) {
                            Set<Player> set = new HashSet<>();
                            set.add(toAdd);
                            result.add(set);
                        } else {
                            Player finalToAdd = toAdd;
                            result.forEach(set -> set.add(finalToAdd));
                        }
                    }
                    return result;
                };
            } else throw new IncoherentConfigurationException("Unknown andTargets specification: " + actionObject.get(field).toString());
        } else {
            addToEach = (potentialTargets, toAdd) -> potentialTargets;
        }
        return addToEach;
    }

    private static BiFunction<Set<Set<Player>>, List<Player>, Set<Set<Player>>> readVeto(JsonObject actionObject) {
        BiFunction<Set<Set<Player>>, List<Player>, Set<Set<Player>>> veto;
        String field = "veto";
        if (actionObject.has(field)) {
            switch (actionObject.get(field).getAsString()) {
                case "LAST_HIT":
                    veto = (potentialTargets, previouslyHit) -> {
                        List<Player> vetoList = new LinkedList<>();
                        if (!previouslyHit.isEmpty()) {
                            vetoList.add(previouslyHit.get(previouslyHit.size() - 1));
                        }
                        return WeaponFactory.removeFromSet(potentialTargets, vetoList);
                    };
                    break;
                case "ALL_PREVIOUS":
                    veto = WeaponFactory::removeFromSet;
                    break;
                default:
                    throw new IncoherentConfigurationException("Unknown veto: " + actionObject.get(field).getAsString());
            }
        } else {
            veto = (potentialTargets, previouslyHit) -> potentialTargets;
        }
        return veto;
    }

    private static <T> Set<Set<T>> removeFromSet(Set<Set<T>> original, List<T> toRemove) {
        Set<Set<T>> updatedTargets = new HashSet<>();
        for (Set<T> set : original) {
            Set<T> updatedSet = new HashSet<>(set);
            updatedSet.removeAll(toRemove);
            updatedTargets.add(updatedSet);
        }
        return updatedTargets;
    }

    private static Function<BasicWeapon, Set<Block>> readStartingPoint(JsonObject actionObject) {
        Function<BasicWeapon, Set<Block>> startingPointCalculator;
        String field = "startingPoint";
        if (actionObject.has(field)) {
            switch (actionObject.get(field).getAsString()) {
                case "INHERIT":
                    startingPointCalculator = weapon -> new HashSet<>(Collections.singletonList(
                            weapon.getStartingPoint().orElseThrow(() -> new IncoherentConfigurationException("No starting point to inherit"))
                    ));
                    break;
                case "ACTIVE_PLAYER":
                    startingPointCalculator = weapon -> new HashSet<>(Collections.singletonList(
                            weapon.getCurrentShooter().getBlock()
                    ));
                    break;
                case "VISIBLE":
                    startingPointCalculator = weapon -> weapon.getCurrentShooter().getMatch().getBoard().getVisibleBlocks(weapon.getCurrentShooter().getBlock());
                    if (actionObject.has("andNotStartingPoint") && actionObject.get("andNotStartingPoint").getAsString().equals("ACTIVE_PLAYER")) {
                        startingPointCalculator = weapon -> {
                            Set<Block> blocks = weapon.getCurrentShooter().getMatch().getBoard().getVisibleBlocks(weapon.getCurrentShooter().getBlock());
                            blocks.remove(weapon.getCurrentShooter().getBlock());
                            return blocks;
                        };
                    }
                    break;
                case "PREVIOUS_TARGET":
                    startingPointCalculator = weapon -> new HashSet<>(Collections.singletonList(
                            weapon.getExecutedAttacks().get(weapon.getExecutedAttacks().size() - 1).getLastHit().iterator().next().getBlock()
                    ));
                    break;
                default:
                    throw new IncoherentConfigurationException("Unknown startingPoint: " + actionObject.get(field).getAsString());
            }
        } else {
            startingPointCalculator = weapon -> new HashSet<>(Collections.singletonList(
                    weapon.getStartingPoint().orElse(null)
            ));
        }
        return startingPointCalculator;
    }

    private static TriConsumer<Set<Player>, Interviewer, BasicWeapon> readActionType(final JsonObject actionObject, int boardSize) {
        TriConsumer<Set<Player>, Interviewer, BasicWeapon> executor;
        if (actionObject.has("damage")) {
            executor = readDamageExecutor(actionObject);
        } else if (actionObject.has("mark")) {
            executor = readMarkExecutor(actionObject);
        } else if (actionObject.has("move")) {
            executor = readMoveExecutor(actionObject, boardSize);
        } else throw new IncoherentConfigurationException("No action specified");
        return executor;
    }

    private static TriConsumer<Set<Player>, Interviewer, BasicWeapon> readDamageExecutor(final JsonObject actionObject) {
        return  (targets, interviewer, weapon) -> {
            int damageAmount = actionObject.get("damage").getAsInt();
            List<DamageToken> tokens = new ArrayList<>();
            for (int i = 0; i < damageAmount; i++) {
                tokens.add(new DamageToken(weapon.getCurrentShooter()));
            }
            targets.forEach(target -> target.addDamageTokens(tokens));
        };
    }

    private static TriConsumer<Set<Player>, Interviewer, BasicWeapon> readMarkExecutor(final JsonObject actionObject) {
        return (targets, interviewer, weapon) -> {
            int markAmount = actionObject.get("mark").getAsInt();
            List<DamageToken> marks = new ArrayList<>();
            for (int i = 0; i < markAmount; i++) {
                marks.add(new DamageToken(weapon.getCurrentShooter()));
            }
            targets.forEach(target -> target.addDamageTokens(marks));
        };
    }

    private static TriConsumer<Set<Player>, Interviewer, BasicWeapon> readMoveExecutor(final JsonObject actionObject, int boardSize) {
        final Range range = computeRange(actionObject.get("move").getAsJsonObject(), boardSize);
        if (actionObject.has("targetFinalPosition")) {
            String position = actionObject.get("targetFinalPosition").getAsString();
            switch (position) {
                case "FIXED":
                    return readFixedTargetMoveExecutor(actionObject, range, boardSize);
                case "STRAIGHT":
                    return readStraightTargetMoveExecutor(range, boardSize);
                default:
                    throw new IncoherentConfigurationException("Unrecognized targetFinalPosition: " + position);
            }
        } else {
            return readStandardMoveExecutor(range);
        }
    }

    private static TriConsumer<Set<Player>, Interviewer, BasicWeapon> readFixedTargetMoveExecutor(final JsonObject actionObject, final Range range, int boardSize) {
        final Range rangeFromStartingBlock = computeRange(actionObject.get("targetFinalDistance").getAsJsonObject(), boardSize);
        return (targets, interviewer, weapon) -> {
            for (Player target : targets) {
                Block start = weapon.getStartingPoint().orElseThrow(() -> new IllegalStateException("Missing starting point, can't calculate fixed distance"));
                Board board = target.getMatch().getBoard();
                Set<Block> arrivalOptionsFromTarget = board.getReachableBlocks(target.getBlock(), range);
                Set<Block> arrivalOptionsFromStart = board.getReachableBlocks(start, rangeFromStartingBlock);
                arrivalOptionsFromTarget.removeIf(block -> !arrivalOptionsFromStart.contains(block));
                Set<Point> coordinates = arrivalOptionsFromTarget.stream().map(block -> new Point(block.getColumn(), block.getRow())).collect(Collectors.toSet());
                Point chosenPoint = interviewer.select(coordinates);
                board.teleportPlayer(target, board.getBlock(chosenPoint.y, chosenPoint.x).orElseThrow(() -> new IllegalArgumentException("Destination block does not exist")));
            }
        };
    }

    private static TriConsumer<Set<Player>, Interviewer, BasicWeapon> readStraightTargetMoveExecutor(final Range range, int boardSize) {
        return (targets, interviewer, weapon) -> {
            for (Player target : targets) {
                Block start = target.getBlock();
                Board board = target.getMatch().getBoard();
                Set<Direction> options = Arrays
                        .stream(Direction.values())
                        .filter(dir -> start.getBorderType(dir) != Block.BorderType.WALL)
                        .collect(Collectors.toSet());

                Optional<Direction> chosenDirection =
                        range.getMin() > 0 ?
                                Optional.of(interviewer.select(options)) :
                                interviewer.selectOptional(options);

                for (int i = 0; (i < range.getMax()) && chosenDirection.isPresent(); i++) {
                    board.movePlayer(target, chosenDirection.get());
                    chosenDirection = range.getMin() > (i + 1) ?
                            chosenDirection :
                            interviewer.selectOptional(Collections.singleton(chosenDirection.get()));
                }

            }
        };
    }

    private static TriConsumer<Set<Player>, Interviewer, BasicWeapon> readStandardMoveExecutor(final Range range) {
        return (targets, interviewer, weapon) -> {
            for (Player target : targets) {
                Board board = target.getMatch().getBoard();
                int i = 0;
                Optional<Direction> chosenDirection;
                do {
                    Block start = target.getBlock();
                    Set<Direction> options = Arrays
                            .stream(Direction.values())
                            .filter(dir -> start.getBorderType(dir) != Block.BorderType.WALL)
                            .collect(Collectors.toSet());
                    chosenDirection =
                            range.getMin() > i ?
                                    Optional.of(interviewer.select(options)) :
                                    interviewer.selectOptional(options);
                    chosenDirection.ifPresent(dir -> board.movePlayer(target, dir));
                    i++;
                } while (i < range.getMax() && chosenDirection.isPresent());
            }
        };
    }
}
