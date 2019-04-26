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
import it.polimi.ingsw.shared.messages.ClientApi;
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

    private static final String FIXED = "FIXED";
    private static final String STRAIGHT = "STRAIGHT";
    private static final String INHERIT = "INHERIT";
    private static final String STARTING_POINT = "startingPoint";
    private static final String VISIBLE = "VISIBLE";
    private static final String ACTIVE_PLAYER = "ACTIVE_PLAYER";
    private static final String SELF = "SELF";
    private static final String INHERIT_ALL = "INHERIT_ALL";
    private static final String INHERIT_LAST = "INHERIT_LAST";
    private static final String INHERIT_IF_PRESENT = "INHERIT_IF_PRESENT";
    private static final String TARGET_DISTANCE = "targetDistance";
    private static final String TARGET_DIRECTION = "targetDirection";
    private static final String TARGET_POSITION = "targetPosition";


    private static Map<Weapon.Name, JsonObject> weaponMap;

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
            readFromFile();
        }
        return readWeapon(weaponMap.get(name), board);
    }

    private static void readFromFile() {
        weaponMap = new EnumMap<>(Weapon.Name.class);
        JsonElement jsonElement;
        try {
            jsonElement = new JsonParser().parse(new FileReader(new File(WEAPON_JSON_PATH)));
        } catch (FileNotFoundException e) {
            throw new MissingConfigurationFileException("Weapon configuration file not found");
        }

        jsonElement.getAsJsonArray().forEach(weaponJson -> {
            JsonObject weaponObject = weaponJson.getAsJsonObject();
            weaponMap.put(
                    EnumValueByString
                            .findByString(weaponObject.get("weaponId").getAsString(), Weapon.Name.class),
                    weaponObject
            );
        });
    }

    private static BasicWeapon readWeapon(JsonObject weaponObject, Board board) {

        Weapon.Name weaponId = EnumValueByString.findByString(weaponObject.get("weaponId").getAsString(), Weapon.Name.class);
        Attack basicAttack = readAttack(weaponObject.get("basicAttack").getAsJsonObject(), board);
        if (weaponObject.has("alternativeAttack")) {
            Attack alternativeAttack = readAttack(weaponObject.get("alternativeAttack").getAsJsonObject(), board);
            return new WeaponWithAlternative(weaponId, basicAttack, alternativeAttack);
        } else if (weaponObject.has("advancedAttacks")) {
            List<Attack> advancedAttacks = new LinkedList<>();
            weaponObject.get("advancedAttacks").getAsJsonArray().forEach(attack -> {
                Attack read = readAttack(attack, board);
                boolean basicFirst = attack.getAsJsonObject().has("basicMustBeFirst") && attack.getAsJsonObject().get("basicMustBeFirst").getAsBoolean();
                Attack advanced = new Attack(read, basicFirst);
                advancedAttacks.add(advanced);
            });
            boolean mustExecuteInOrder = weaponObject.has("mustExecuteInOrder") && weaponObject.get("mustExecuteInOrder").getAsBoolean();
            return new WeaponWithMultipleEffects(weaponId, basicAttack, advancedAttacks, mustExecuteInOrder);
        } else {
            return new BasicWeapon(weaponId, basicAttack);
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
        Function<BasicWeapon, Set<Player>> targetsToChooseFrom = readAvailableTargets();
        Function<Set<Player>, Set<Set<Player>>> adaptToScope = readScope(actionObject);
        BiFunction<Set<Set<Player>>, BasicWeapon, Set<Set<Player>>> addToEach = readMandatoryExtras(actionObject);
        BiFunction<Set<Set<Player>>, BasicWeapon, Set<Set<Player>>> veto = readVeto(actionObject);
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

    /**
     * This method reads the {@code JsonObject} and looks for the property {@code targets}: if it is set to {@code FIND},
     * it calls {@link #elaborateFindingTarget(JsonObject, Board, TargetCalculator) elaborateFindingTarget}, in all
     * other cases it returns {@code null}. It is required for the {@code JsonObject} to have a property named {@code targets},
     * see {@see jsonAttacks.md} for further info
     *
     * @author Adriana Ferrari
     * @param actionObject the {@code JsonObject} corresponding to the analyzed action
     * @param lastTargetCalculator the last {@code TargetCalculator} computed while scanning the current attack, or
     * {@code null} if none has yet been computed
     * @param board the {@code Board} on which the target analyses will be made
     * @return the computed {@code TargetCalculator}, or {@code null} if none was appliable to the action
     */
    private static @Nullable TargetCalculator readTargetCalculator(JsonObject actionObject, @Nullable TargetCalculator lastTargetCalculator, Board board) {

        if ("FIND".equals(actionObject.get("targets").getAsString())) {
            return elaborateFindingTarget(actionObject, board, lastTargetCalculator);
        }
        return null;
    }

    /**
     * This method is used to retrieve the bonus targets from the given {@code JsonObject}. The bonus targets are those
     * players that are going to be targeted by the attack independently from their position on the board (for those,
     * see {@link #readTargetCalculator(JsonObject, TargetCalculator, Board) readTargetCalculator}). It is required for
     * the {@code JsonObject} to have a property named {@code targets}, see {@see jsonAttacks.md} for further info
     *
     * @author Adriana Ferrari
     * @param actionObject the {@code JsonObject} to analyze
     * @return a {@code BiFunction} that takes {@code List<Player>}, the players that were previously hit by the weapon,
     * and a {@code Player}, the active player, as arguments and returns the {@code Set<Player>} representing the bonus
     * targets, which will be empty if none are present
     */
    private static BiFunction<List<Player>, Player, Set<Player>> readBonusTarget(JsonObject actionObject) {
        BiFunction<List<Player>, Player, Set<Player>> bonusTarget;

        //The field targets of actionObject is analyzed
        switch (actionObject.get("targets").getAsString()) {
            case INHERIT_LAST:
                bonusTarget = (previouslyHit, activePlayer) -> {
                    //scanning the targets previously hit by the weapon and retrieving the last that is not the active player
                    Set<Player> last = new HashSet<>();
                    int index = previouslyHit.size() - 1;
                    while (index >= 0) {
                        if (previouslyHit.get(index) != activePlayer) {
                            last.add(previouslyHit.get(index));
                            return last;
                        }
                        index--;
                    }
                    //last will be empty if no previous target was different from the active player
                    return last;
                };
                break;
            case SELF:
                //the active player is added to the set
                bonusTarget = (previouslyHit, activePlayer) -> {
                    Set<Player> self = new HashSet<>();
                    self.add(activePlayer);
                    return self;
                };
                break;
            case INHERIT_ALL:
                //all the previous targets but the active player are added to the set
                bonusTarget = (previouslyHit, activePlayer) -> {
                    Set<Player> available = new HashSet<>(previouslyHit);
                    available.remove(activePlayer);
                    return available;
                };
                break;
            default:
                //no bonus targets
                bonusTarget = (previouslyHit, activePlayer) -> new HashSet<>();
                break;
        }
        return bonusTarget;
    }

    /**
     * This method restricts the hittable targets
     *
     * @author Adriana Ferrari
     * @return a {@code Function} that takes {@code BasicWeapon}, the weapon that is being used, and returns a {@code Set<Player>},
     * the available targets
     */
    private static Function<BasicWeapon, Set<Player>> readAvailableTargets() {
        return weapon -> {
                Set<Player> nonActive = new HashSet<>(weapon.getCurrentShooter().getMatch().getPlayers());
                nonActive.remove(weapon.getCurrentShooter());
                return nonActive;
            };
    }

    /**
     * This method returns the appropriate {@code TargetCalculator} for the occasion. It is required that {@code actionObject}
     * has the property {@code targetPosition}, see {@see jsonAttacks.md} for further info
     *
     * @author Adriana Ferrari
     * @param actionObject the {@code JsonObject} to analyze
     * @param board the {@code Board} on which the targets will be found
     * @param lastTargetCalculator the previous {@code TargetCalculator} for this attack, which can be {@code null}
     * @return the new {@code TargetCalculator}, which might be {@code null} if {@code lastTargetCalculator} was null
     * @throws IncoherentConfigurationException if the value of {@code targetPosition} is not supported
     */
    private static @Nullable TargetCalculator elaborateFindingTarget(JsonObject actionObject, Board board, @Nullable TargetCalculator lastTargetCalculator) {
        switch (actionObject.get(TARGET_POSITION).getAsString()) {
            case FIXED:
                return fixedDistanceTargetCalculator(actionObject, board);
            case VISIBLE:
                return fixedVisibilityTargetCalculator(actionObject, board);
            case STRAIGHT:
                return straightTargetCalculator(actionObject, board, lastTargetCalculator);
            default:
                throw new IncoherentConfigurationException("Unknown value for targetPosition: " + actionObject.get(TARGET_POSITION).getAsString());
        }
    }

    private static TargetCalculator fixedDistanceTargetCalculator(JsonObject actionObject, Board board) {
        JsonObject distance = actionObject.get(TARGET_DISTANCE).getAsJsonObject();
        return new FixedDistanceTargetCalculator(board, computeRange(distance, board.getBlocks().size()));
    }

    private static TargetCalculator fixedVisibilityTargetCalculator(JsonObject actionObject, Board board) {
        boolean isVisible = true;
        if (actionObject.has("visibility")) {
            isVisible = actionObject.get("visibility").getAsBoolean();
        }
        FixedVisibilityTargetCalculator visibilityTargetCalculator = new FixedVisibilityTargetCalculator(board, isVisible);
        if (actionObject.has(TARGET_DISTANCE)) {
            JsonObject distance = actionObject.get(TARGET_DISTANCE).getAsJsonObject();
            FixedDistanceTargetCalculator distanceTargetCalculator = new FixedDistanceTargetCalculator(board, computeRange(distance, board.getBlocks().size()));
            List<TargetCalculator> calculators = new ArrayList<>();
            calculators.add(visibilityTargetCalculator);
            calculators.add(distanceTargetCalculator);
            return new CompoundTargetCalculator(calculators);
        }
        return visibilityTargetCalculator;
    }

    private static @Nullable TargetCalculator straightTargetCalculator(JsonObject actionObject, Board board, @Nullable TargetCalculator lastTargetCalculator) {
        boolean goesThroughWalls = false;
        if (actionObject.has("goesThroughWalls")) {
            goesThroughWalls = actionObject.get("goesThroughWalls").getAsBoolean();
        }
        String direction = actionObject.get(TARGET_DIRECTION).getAsString();
        if (direction.equals(FIXED)) {
            return fixedDirectionTargetCalculator(actionObject, board, goesThroughWalls);
        } else if (direction.equals(INHERIT)) {
            return inheritedTargetCalculator(actionObject, board, lastTargetCalculator);
        } else throw new IncoherentConfigurationException("Unknown value for targetDirection: " + direction);
    }

    private static TargetCalculator fixedDirectionTargetCalculator(JsonObject actionObject, Board board, boolean goesThroughWalls) {
        if (!actionObject.has(TARGET_DISTANCE)) {
            return new FixedDirectionTargetCalculator(board, goesThroughWalls);
        } else {
            JsonObject distance = actionObject.get(TARGET_DISTANCE).getAsJsonObject();
            FixedDistanceTargetCalculator distanceTargetCalculator = new FixedDistanceTargetCalculator(board, computeRange(distance, board.getBlocks().size()));
            List<TargetCalculator> calculators = new ArrayList<>();
            calculators.add(distanceTargetCalculator);
            calculators.add(new FixedDirectionTargetCalculator(board, goesThroughWalls));
            return new CompoundTargetCalculator(calculators);
        }
    }

    private static @Nullable TargetCalculator inheritedTargetCalculator(JsonObject actionObject, Board board, @Nullable TargetCalculator lastTargetCalculator) {
        if (!actionObject.has(TARGET_DISTANCE)) {
            return lastTargetCalculator;
        } else {
            JsonObject distance = actionObject.get(TARGET_DISTANCE).getAsJsonObject();
            FixedDistanceTargetCalculator distanceTargetCalculator = new FixedDistanceTargetCalculator(board, computeRange(distance, board.getBlocks().size()));
            List<TargetCalculator> calculators = new ArrayList<>();
            if (lastTargetCalculator != null) {
                for (TargetCalculator c : lastTargetCalculator.getSubCalculators()) {
                    if (c instanceof FixedDirectionTargetCalculator) {
                        calculators.add(c);
                        break;
                    }
                }
            } else throw new IncoherentConfigurationException("No last target calculator found");

            calculators.add(distanceTargetCalculator);
            return new CompoundTargetCalculator(calculators);
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
                    for (Player target : targets) {
                        Set<Player> toAdd = new HashSet<>(target.getBlock().getPlayers());
                        toAdd.remove(target.getMatch().getActivePlayer());
                        adaptedTargets.add(toAdd);
                    }
                    return adaptedTargets;
                };
                break;
            case "ROOM":
                adaptToScope = targets -> {
                    Set<Set<Player>> adaptedTargets = new HashSet<>();
                    for (Player target : targets) {
                        Set<Player> toAdd = target
                                .getMatch()
                                .getBoard()
                                .getRoom(target.getBlock())
                                .stream()
                                .flatMap(block -> block.getPlayers().stream())
                                .collect(Collectors.toSet());
                        toAdd.remove(target.getMatch().getActivePlayer());
                        adaptedTargets.add(toAdd);
                    }
                    return adaptedTargets;
                };
                break;
            case "ALL":
                adaptToScope = targets -> {
                    Set<Set<Player>> adaptedTargets = new HashSet<>();
                    adaptedTargets.add(targets);
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
            if ("INCLUDE_LAST".equals(actionObject.get(field).getAsString())) {
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

    private static BiFunction<Set<Set<Player>>, BasicWeapon, Set<Set<Player>>> readVeto(JsonObject actionObject) {
        BiFunction<Set<Set<Player>>, BasicWeapon, Set<Set<Player>>> veto;
        String field = "veto";
        if (actionObject.has(field)) {
            switch (actionObject.get(field).getAsString()) {
                case "LAST_HIT":
                    veto = (potentialTargets, weapon) -> {
                        List<Player> previouslyHit = weapon.getAllTargets();
                        List<Player> vetoList = new LinkedList<>();
                        if (!previouslyHit.isEmpty()) {
                            vetoList.add(previouslyHit.get(previouslyHit.size() - 1));
                        }
                        return WeaponFactory.removeFromSet(potentialTargets, vetoList);
                    };
                    break;
                case "ALL_PREVIOUS":
                    veto = (potentialTargets, weapon) -> WeaponFactory.removeFromSet(potentialTargets, weapon.getAllTargets());
                    break;
                case "ALL_PREVIOUS_BLOCKS":
                    veto = (potentialTargets, weapon) -> WeaponFactory.removeFromSet(potentialTargets, weapon.getAllTargets().stream().flatMap(p -> p.getBlock().getPlayers().stream()).collect(Collectors.toList()));
                    break;
                case "HIT_BY_ADVANCED":
                    veto = (potentialTargets, weapon) -> {
                        List<Attack> poweredAttacks = ((WeaponWithMultipleEffects) weapon).getPoweredAttacks();
                        List<Player> vetoList = new LinkedList<>();
                        for (Attack powered : poweredAttacks) {
                            vetoList.addAll(weapon.wasHitBy(powered));
                        }
                        return WeaponFactory.removeFromSet(potentialTargets, vetoList);
                    };
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
            if (!updatedSet.isEmpty()) {
                updatedTargets.add(updatedSet);
            }
        }
        return updatedTargets;
    }

    private static Function<BasicWeapon, Set<Block>> readStartingPoint(JsonObject actionObject) {
        Function<BasicWeapon, Set<Block>> startingPointCalculator;
        if (actionObject.has(STARTING_POINT)) {
            switch (actionObject.get(STARTING_POINT).getAsString()) {
                case INHERIT:
                    startingPointCalculator = weapon -> new HashSet<>(Collections.singletonList(
                            weapon.getStartingPoint().orElseThrow(() -> new IncoherentConfigurationException("No starting point to inherit"))
                    ));
                    break;
                case INHERIT_IF_PRESENT:
                    startingPointCalculator = weapon -> new HashSet<>(Collections.singletonList(
                            weapon.getStartingPoint().orElse(weapon.getCurrentShooter().getBlock())
                    ));
                    break;
                case ACTIVE_PLAYER:
                    startingPointCalculator = weapon -> new HashSet<>(Collections.singletonList(
                            weapon.getCurrentShooter().getBlock()
                    ));
                    break;
                case VISIBLE:
                    startingPointCalculator = weapon -> {
                        Set<Block> visibleBlocks = weapon.getCurrentShooter().getMatch().getBoard().getVisibleBlocks(weapon.getCurrentShooter().getBlock());
                        visibleBlocks.removeIf(block -> !weapon.validStartingPoint(weapon.getActiveAttack(), block));
                        return visibleBlocks;
                    };
                    if (actionObject.has("andNotStartingPoint") && actionObject.get("andNotStartingPoint").getAsString().equals(ACTIVE_PLAYER)) {
                        startingPointCalculator = weapon -> {
                            Set<Block> blocks = weapon.getCurrentShooter().getMatch().getBoard().getVisibleBlocks(weapon.getCurrentShooter().getBlock());
                            blocks.removeIf(block -> !weapon.validStartingPoint(weapon.getActiveAttack(), block));
                            blocks.remove(weapon.getCurrentShooter().getBlock());
                            return blocks;
                        };
                    }
                    break;
                case "PREVIOUS_TARGET":
                    startingPointCalculator = weapon -> {
                        Block block = null;
                        int index = weapon.getAllTargets().size() - 1;
                        do {
                            block = weapon.getAllTargets().get(index) != weapon.currentShooter ? weapon.getAllTargets().get(index).getBlock() : null;
                            index--;
                        } while (block == null && index >= 0);
                        if (block == null) {
                            throw new IncoherentConfigurationException("No previous target found");
                        }

                        return new HashSet<>(Collections.singletonList(block));
                    };
                    break;
                default:
                    throw new IncoherentConfigurationException("Unknown startingPoint: " + actionObject.get(STARTING_POINT).getAsString());
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
            targets.forEach(target -> target.addMarks(marks));
        };
    }

    private static TriConsumer<Set<Player>, Interviewer, BasicWeapon> readMoveExecutor(final JsonObject actionObject, int boardSize) {
        final Range range = computeRange(actionObject.get("move").getAsJsonObject(), boardSize);
        if (actionObject.has("targetFinalPosition")) {
            String position = actionObject.get("targetFinalPosition").getAsString();
            switch (position) {
                case FIXED:
                    return readFixedTargetMoveExecutor(actionObject, range, boardSize);
                case STRAIGHT:
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
                Point chosenPoint = interviewer.select("Select the destination of the move", coordinates, ClientApi.BLOCK_QUESTION);
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
                                Optional.of(interviewer.select("Fix the direction for the moves", options, ClientApi.DIRECTION_QUESTION)) :
                                interviewer.selectOptional("Fix the direction for the moves", options, ClientApi.DIRECTION_QUESTION);

                for (int i = 0; (i < range.getMax()) && chosenDirection.isPresent(); i++) {
                    board.movePlayer(target, chosenDirection.get());
                    chosenDirection = range.getMin() > (i + 1) ?
                            chosenDirection :
                            interviewer.selectOptional("Select the direction for the move", Collections.singleton(chosenDirection.get()), ClientApi.DIRECTION_QUESTION);
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
                                    Optional.of(interviewer.select("Select the direction for the move", options, ClientApi.DIRECTION_QUESTION)) :
                                    interviewer.selectOptional("Select the direction for the move", options, ClientApi.DIRECTION_QUESTION);
                    chosenDirection.ifPresent(dir -> board.movePlayer(target, dir));
                    i++;
                } while (i < range.getMax() && chosenDirection.isPresent());
            }
        };
    }
}
