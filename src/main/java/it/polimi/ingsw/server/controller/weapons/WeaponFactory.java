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

/**
 * This class creates the interactive parts of the weapons (the attacks)
 *
 * @author Adriana Ferrari, Carlo Dell'Acqua
 */
public class WeaponFactory {
    /**
     * The path of the json configuration file
     */
    private static final String WEAPON_JSON_PATH = "./resources/attacks.json";

    /**
     * Enum representing the field values encountered in the json
     *
     * @author Carlo Dell'Acqua, Adriana Ferrari
     */
    private enum Field {
        FIXED("FIXED"),
        STRAIGHT("STRAIGHT"),
        INHERIT("INHERIT"),
        VISIBLE("VISIBLE"),
        ACTIVE_PLAYER("ACTIVE_PLAYER"),
        SELF("SELF"),
        INHERIT_ALL("INHERIT_ALL"),
        INHERIT_LAST("INHERIT_LAST"),
        INHERIT_IF_PRESENT("INHERIT_IF_PRESENT"),
        PREVIOUS_TARGET("PREVIOUS_TARGET"),
        LAST_HIT("LAST_HIT"),
        ALL_PREVIOUS("ALL_PREVIOUS"),
        ALL_PREVIOUS_BLOCKS("ALL_PREVIOUS_BLOCKS"),
        HIT_BY_ADVANCED("HIT_BY_ADVANCED"),
        FIND("FIND"),
        INCLUDE_LAST("INCLUDE_LAST");

        /**
         * Actual {@code String} representing the field
         */
        private String jsonString;

        /**
         * Constructor given the {@code String} value
         *
         * @param jsonString {@code String} equivalent
         */
        Field(String jsonString) {
            this.jsonString = jsonString;
        }

        /**
         * This method transforms the enum into a {@code String}
         *
         * @author Carlo Dell'Acqua
         * @return the {@code String} equivalent value
         */
        @Override
        public String toString() {
            return jsonString;
        }
    }

    /**
     * Enum representing the property values encountered in the json
     *
     * @author Carlo Dell'Acqua, Adriana Ferrari
     */
    private enum Property {
        STARTING_POINT("startingPoint"),
        TARGET_DISTANCE("targetDistance"),
        TARGET_DIRECTION("targetDirection"),
        TARGET_POSITION("targetPosition"),
        AND_TARGETS("andTargets"),
        VETO("veto"),
        MOVE("move"),
        DAMAGE("damage"),
        MARK("mark"),
        TARGET_FINAL_POSITION("targetFinalPosition"),
        TARGET_FINAL_DISTANCE("targetFinalDistance"),
        TARGETS("targets"),
        WEAPON_ID("weaponId"),
        NAME("name"),
        COST("cost"),
        BASIC_ATTACK("basicAttack"),
        ALTERNATIVE_ATTACK("alternativeAttack"),
        ADVANCED_ATTACKS("advancedAttacks"),
        BASIC_MUST_BE_FIRST("basicMustBeFirst"),
        MUST_EXECUTE_IN_ORDER("mustExecuteInOrder"),
        ACTIONS("actions"),
        SKIPPABLE("skippable"),
        GOES_THROUGH_WALLS("goesThroughWalls");

        /**
         * Actual {@code String} representing the property
         */
        private String jsonString;

        /**
         * Constructor given the {@code String} value
         *
         * @param jsonString {@code String} equivalent
         */
        Property(String jsonString) {
            this.jsonString = jsonString;
        }

        /**
         * This method transforms the enum into a {@code String}
         *
         * @author Carlo Dell'Acqua
         * @return the {@code String} equivalent value
         */
        @Override
        public String toString() {
            return jsonString;
        }
    }


    /**
     * A {@code Map} associating each weapon to its relative {@code JsonObject}
     */
    private static Map<Weapon.Name, JsonObject> weaponMap;

    /**
     * Private empty constructor because this class should not have instances
     */
    private WeaponFactory() { }

    /**
     * This method is used to create any weapon
     *
     * @author Adriana Ferrari
     * @param name the enum corresponding to the desired weapon
     * @return the weapon, ready to be bought
     */
    public static BasicWeapon create(Weapon.Name name, Board board) {
        if (weaponMap == null) {
            readFromFile();
        }
        return readWeapon(weaponMap.get(name), board);
    }

    /**
     * This method analyzes the json configuration file and fills the {@code Map} with the appropriate {@code JsonObject}
     * instances
     *
     * @author Adriana Ferrari, Carlo Dell'Acqua
     */
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
                            .findByString(weaponObject.get(Property.WEAPON_ID.toString()).getAsString(), Weapon.Name.class),
                    weaponObject
            );
        });
    }

    /**
     * This method reads the properties of the given {@code JsonObject} that represents the weapon, in order to find the
     * right type and attacks
     *
     * @author Adriana Ferrari, Carlo Dell'Acqua
     * @param weaponObject the {@code JsonObject} representing the weapon
     * @param board the {@code Board} that is being used
     * @return the corresponding {@code BasicWeapon}
     */
    private static BasicWeapon readWeapon(JsonObject weaponObject, Board board) {

        Weapon.Name weaponId = EnumValueByString.findByString(weaponObject.get(Property.WEAPON_ID.toString()).getAsString(), Weapon.Name.class);
        Attack basicAttack = readAttack(weaponObject.get(Property.BASIC_ATTACK.toString()).getAsJsonObject(), board);
        if (weaponObject.has(Property.ALTERNATIVE_ATTACK.toString())) {
            Attack alternativeAttack = readAttack(weaponObject.get(Property.ALTERNATIVE_ATTACK.toString()).getAsJsonObject(), board);
            return new WeaponWithAlternative(weaponId, basicAttack, alternativeAttack);
        } else if (weaponObject.has(Property.ADVANCED_ATTACKS.toString())) {
            List<Attack> advancedAttacks = new LinkedList<>();
            weaponObject.get(Property.ADVANCED_ATTACKS.toString()).getAsJsonArray().forEach(attack -> {
                Attack read = readAttack(attack, board);
                boolean basicFirst = attack.getAsJsonObject().has(Property.BASIC_MUST_BE_FIRST.toString()) && attack.getAsJsonObject().get("basicMustBeFirst").getAsBoolean();
                Attack advanced = new Attack(read, basicFirst);
                advancedAttacks.add(advanced);
            });
            boolean mustExecuteInOrder = weaponObject.has(Property.MUST_EXECUTE_IN_ORDER.toString()) && weaponObject.get(Property.MUST_EXECUTE_IN_ORDER.toString()).getAsBoolean();
            return new WeaponWithMultipleEffects(weaponId, basicAttack, advancedAttacks, mustExecuteInOrder);
        } else {
            return new BasicWeapon(weaponId, basicAttack);
        }
    }

    /**
     * This method reads the {@code JsonElement} representing the {@code Attack} that shall be created
     *
     * @author Adriana Ferrari
     * @param jsonElement the {@code JsonElement} to analyze
     * @param board the {@code Board} that is being used
     * @return the corresponding {@code Attack}
     */
    private static Attack readAttack(JsonElement jsonElement, Board board) {
        JsonObject attackObject = jsonElement.getAsJsonObject();
        String name = attackObject.get(Property.NAME.toString()).getAsString();
        List<Coin> cost = new ArrayList<>();
        if (attackObject.has(Property.COST.toString())) {
            attackObject.get(Property.COST.toString()).getAsJsonArray().forEach(color -> cost.add(AmmoCubeFactory.create(EnumValueByString.findByString(color.getAsString(), CurrencyColor.class))));
        }
        JsonArray actions = attackObject.get(Property.ACTIONS.toString()).getAsJsonArray();
        TargetCalculator lastTargetCalculator = null;
        List<ActionConfig> actionConfigs = new ArrayList<>();
        for (JsonElement action : actions) {

            ActionConfig config = readAction(action, lastTargetCalculator, board);
            lastTargetCalculator = config.getCalculator().orElse(lastTargetCalculator);
            actionConfigs.add(config);
        }
        return new Attack(name, actionConfigs, board, cost);
    }

    /**
     * This method reads the {@code JsonElement} representing the {@code ActionConfig} that shall be created
     *
     * @author Adriana Ferrari
     * @param action the {@code JsonElement} to analyze
     * @param lastTargetCalculator the last {@code TargetCalculator} created for the {@code Attack} (it may be {@code null})
     * @param board the {@code Board} that is being used
     * @return the corresponding {@code ActionConfig}
     */
    private static ActionConfig readAction(JsonElement action, @Nullable TargetCalculator lastTargetCalculator, Board board) {
        JsonObject actionObject = action.getAsJsonObject();
        TargetCalculator targetCalculator = readTargetCalculator(actionObject, lastTargetCalculator, board);
        BiFunction<List<Player>, Player, Set<Player>> bonusTargets = readBonusTarget(actionObject);
        Function<BasicWeapon, Set<Player>> targetsToChooseFrom = readAvailableTargets();
        Function<Set<Player>, Set<Set<Player>>> adaptToScope = readScope(actionObject);
        BiFunction<Set<Set<Player>>, BasicWeapon, Set<Set<Player>>> addToEach = readMandatoryExtras(actionObject);
        BiFunction<Set<Set<Player>>, BasicWeapon, Set<Set<Player>>> veto = readVeto(actionObject);
        boolean skippable = actionObject.get(Property.SKIPPABLE.toString()).getAsBoolean();
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

        if (Field.FIND.toString().equals(actionObject.get(Property.TARGETS.toString()).getAsString())) {
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
        Field value = EnumValueByString
                .findByString(
                        actionObject.get(Property.TARGETS.toString()).getAsString(),
                        Field.class
                );
        //The field targets of actionObject is analyzed
        switch (value) {
            case INHERIT_LAST:
                bonusTarget = (previouslyHit, activePlayer) -> {
                    //scanning the targets previously hit by the weapon and retrieving the last that is not the active player
                    Set<Player> last = new HashSet<>();
                    Optional<Player> playerToAdd = inheritLastTarget(previouslyHit, activePlayer);
                    playerToAdd.ifPresent(last::add);
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
    private static TargetCalculator elaborateFindingTarget(JsonObject actionObject, Board board, @Nullable TargetCalculator lastTargetCalculator) {
        Field value = EnumValueByString
                .findByString(
                        actionObject.get(Property.TARGET_POSITION.toString()).getAsString(),
                        Field.class
                );
        switch (value) {
            case FIXED:
                return fixedDistanceTargetCalculator(actionObject, board);
            case VISIBLE:
                return fixedVisibilityTargetCalculator(actionObject, board);
            case STRAIGHT:
                return straightTargetCalculator(actionObject, board, lastTargetCalculator);
            default:
                throw new IncoherentConfigurationException("Unknown value for targetPosition: " + value.toString());
        }
    }

    /**
     * This method creates the {@code FixedDistanceTargetCalculator} having the distance specified in {@code actionObject}
     *
     * @author Adriana Ferrari
     * @param actionObject the {@code JsonObject} to analyze
     * @param board the {@code Board} on which the targets will be found
     * @return the new {@code TargetCalculator}
     */
    private static TargetCalculator fixedDistanceTargetCalculator(JsonObject actionObject, Board board) {
        JsonObject distance = actionObject.get(Property.TARGET_DISTANCE.toString()).getAsJsonObject();
        return new FixedDistanceTargetCalculator(board, computeRange(distance, board.getBlocks().size()));
    }

    /**
     * This method creates the {@code FixedVisibilityTargetCalculator} having the visibility specified in {@code actionObject}.
     * If there is also a constraint for the distance, it will create a {@code CompoundTargetCalculator} containing both
     * requested calculators
     *
     * @author Adriana Ferrari
     * @param actionObject the {@code JsonObject} to analyze
     * @param board the {@code Board} on which the targets will be found
     * @return the new {@code TargetCalculator}
     */
    private static TargetCalculator fixedVisibilityTargetCalculator(JsonObject actionObject, Board board) {
        boolean isVisible = true;
        //if the visibility is specified, it is read from the json, otherwise the default value is used
        if (actionObject.has("visibility")) {
            isVisible = actionObject.get("visibility").getAsBoolean();
        }
        //creating the calculator
        FixedVisibilityTargetCalculator visibilityTargetCalculator = new FixedVisibilityTargetCalculator(board, isVisible);
        if (actionObject.has(Property.TARGET_DISTANCE.toString())) {
            //if there is a target distance specified as well, a FixedDistanceTargetCalculator is created as well
            TargetCalculator distanceTargetCalculator = fixedDistanceTargetCalculator(actionObject, board);
            List<TargetCalculator> calculators = new ArrayList<>();
            calculators.add(visibilityTargetCalculator);
            calculators.add(distanceTargetCalculator);
            //the two calculators are added to a list and a CompoundTargetCalculator is created
            return new CompoundTargetCalculator(calculators);
        }
        return visibilityTargetCalculator;
    }

    /**
     * This method creates the {@code FixedDirectionTargetCalculator}, either inheriting it or instancing a new one. If
     * a distance is specified as well, the calculator will be a {@code CompoundTargetCalculator}.
     * The property {@code targetDirection} must be present in {@code actionObject} and it must be one of the
     * supported values, see {@see jsonAttacks.md} for further info
     *
     * @author Adriana Ferrari
     * @param actionObject the {@code JsonObject} to analyze
     * @param board the {@code Board} on which the targets will be found
     * @param lastTargetCalculator the previous {@code TargetCalculator} for this attack, which can be {@code null}
     * @return the new {@code TargetCalculator}
     * @throws IncoherentConfigurationException if the value of {@code targetDirection} is not supported
     */
    private static TargetCalculator straightTargetCalculator(JsonObject actionObject, Board board, @Nullable TargetCalculator lastTargetCalculator) {
        boolean goesThroughWalls = false;
        //if the property goesThroughWalls is specified, it is read from the json
        //otherwise the default value will be used
        if (actionObject.has(Property.GOES_THROUGH_WALLS.toString())) {
            goesThroughWalls = actionObject.get(Property.GOES_THROUGH_WALLS.toString()).getAsBoolean();
        }
        String direction = actionObject.get(Property.TARGET_DIRECTION.toString()).getAsString();
        if (direction.equals(Field.FIXED.toString())) {
            return fixedDirectionTargetCalculator(actionObject, board, goesThroughWalls);
        } else if (direction.equals(Field.INHERIT.toString())) {
            return inheritedTargetCalculator(actionObject, board, lastTargetCalculator);
        } else throw new IncoherentConfigurationException("Unknown value for targetDirection: " + direction);
    }

    /**
     * This method creates the {@code FixedDirectionTargetCalculator} corresponding to the value {@code FIXED} of the property
     * {@code targetDirection} of the {@code actionObject}. If a distance is specified as well, the calculator will be a
     * {@code CompoundTargetCalculator}
     *
     * @author Adriana Ferrari
     * @param actionObject the {@code JsonObject} to analyze
     * @param board the {@code Board} on which the targets will be found
     * @param goesThroughWalls {@code true} if the weapon can hit targets that are behind a wall, false otherwise
     * @return the new {@code TargetCalculator}
     */
    private static TargetCalculator fixedDirectionTargetCalculator(JsonObject actionObject, Board board, boolean goesThroughWalls) {
        if (!actionObject.has(Property.TARGET_DISTANCE.toString())) {
            //if no distance is specified, a generic FixedDirectionTargetCalculator is returned
            return new FixedDirectionTargetCalculator(board, goesThroughWalls);
        } else {
            //a FixedDistanceTargetCalculator is created as well
            TargetCalculator distanceTargetCalculator = fixedDistanceTargetCalculator(actionObject, board);
            List<TargetCalculator> calculators = new ArrayList<>();
            calculators.add(distanceTargetCalculator);
            calculators.add(new FixedDirectionTargetCalculator(board, goesThroughWalls));
            return new CompoundTargetCalculator(calculators);
        }
    }

    /**
     * This method inherits the last {@code FixedDirectionTargetCalculator}, whether it is alone or inside a {@code CompoundTargetCalculator}
     * If a distance is specified as well, the calculator will be a {@code CompoundTargetCalculator}
     *
     * @author Adriana Ferrari
     * @param actionObject the {@code JsonObject} to analyze
     * @param board the {@code Board} on which the targets will be found
     * @param lastTargetCalculator the previous {@code TargetCalculator} for this attack, which might, but should not, be {@code null}
     * @return the new (or inherited) {@code TargetCalculator}
     * @throws IncoherentConfigurationException if the {@code TargetCalculator} that should be inherited is null or is not a FixedDistanceTargetCalculator
     */
    private static TargetCalculator inheritedTargetCalculator(JsonObject actionObject, Board board, @Nullable TargetCalculator lastTargetCalculator) {
        TargetCalculator toInherit = null;
        if (lastTargetCalculator != null) {
            //finding the calculator to inherit: it must be a FixedDirectionTargetCalculator
            for (TargetCalculator c : lastTargetCalculator.getSubCalculators()) {
                if (c instanceof FixedDirectionTargetCalculator) {
                    toInherit = c;
                    break;
                }
            }
            if (toInherit == null) {
                throw new IncoherentConfigurationException("Last target calculator is not a fixed direction calculator");
            }
        } else throw new IncoherentConfigurationException("No last target calculator found");

        if (!actionObject.has(Property.TARGET_DISTANCE.toString())) {
            return toInherit;
        } else {
            //adding a FixedDistanceTargetCalculator as well
            TargetCalculator distanceTargetCalculator = fixedDistanceTargetCalculator(actionObject, board);
            List<TargetCalculator> calculators = new ArrayList<>();
            calculators.add(toInherit);
            calculators.add(distanceTargetCalculator);
            return new CompoundTargetCalculator(calculators);
        }
    }

    /**
     * This method creates a {@code Range} to store a {@code JsonObject} containing {@code min} and/or {@code max} value.
     * Default value for {@code min} is {@code 0}, default value for {@code max} is {@code maxAvailable}
     *
     * @author Adriana Ferrari
     * @param distance the {@code JsonObject} containing a {@code Range}
     * @param maxAvailable the default max value to use if max property does not exist
     * @return a {@code Range} with the given values
     */
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

    /**
     * This method reads the scope of the action, represented by the property {@code targetAmount} in {@code actionObject}.
     * This property must exist, see {@see jsonAttacks.md} for further info about the supported values
     *
     * @author Adriana Ferrari
     * @param actionObject the {@code JsonObject} to analyze
     * @return a {@code Function} taking a {@code Set<Player>}, the targets computed so far, as input and returning a {@code Set<Set<Player>>}
     * containing the grouped targets and eventual extras due to the grouping method
     * @throws IncoherentConfigurationException if the value of {@code targetAmount} is not supported
     */
    private static Function<Set<Player>, Set<Set<Player>>> readScope(JsonObject actionObject) {
        Function<Set<Player>, Set<Set<Player>>> adaptToScope;
        switch (actionObject.get("targetAmount").getAsString()) {
            case "ONE":
                //each target should form a new set of size 1
                adaptToScope = targets -> {
                    Set<Set<Player>> adaptedTargets = new HashSet<>();
                    for (Player target : targets) {
                        adaptedTargets.add(new HashSet<>(Collections.singletonList(target)));
                    }
                    return adaptedTargets;
                };
                break;
            case "BLOCK":
                //the sets will be formed by the targets and the players on their block (active player excluded)
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
                //the sets will be formed by the targets and the players in their room (active player excluded)
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
                //all the targets are united in a single set
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

    /**
     * This method reads the property {@code andTargets}, if it is present, and adds those targets to each set.
     * See {@see jsonAttacks.md} for further info about this property
     *
     * @author Adriana Ferrari
     * @param actionObject the {@code JsonObject} to analyze
     * @return a {@code BiFunction} taking a {@code Set<Set<Player>>}, the pre-computed targets, and a {@code BasicWeapon},
     * the weapon this attacks belongs to, and returns the updated {@code Set<Set<Player>>}
     */
    private static BiFunction<Set<Set<Player>>, BasicWeapon, Set<Set<Player>>> readMandatoryExtras(JsonObject actionObject) {
        BiFunction<Set<Set<Player>>, BasicWeapon, Set<Set<Player>>> addToEach;
        if (actionObject.has(Property.AND_TARGETS.toString())) {
            addToEach = readCompoundTargetRequirements(actionObject);
        } else {
            addToEach = (potentialTargets, toAdd) -> potentialTargets;
        }
        return addToEach;
    }

    /**
     * This method, assuming that the field {@code andTargets} exists, reads its value and updates the targets accordingly.
     * See {@see jsonAttacks.md} for the supported values
     *
     * @author Adriana Ferrari
     * @param actionObject the {@code JsonObject} to analyze
     * @return a {@code BiFunction} taking a {@code Set<Set<Player>>}, the pre-computed targets, and a {@code BasicWeapon},
     * the weapon this attacks belongs to, and returns the updated {@code Set<Set<Player>>}
     * @throws IncoherentConfigurationException if the value of {@code andTargets} is not supported
     */
    private static BiFunction<Set<Set<Player>>, BasicWeapon, Set<Set<Player>>> readCompoundTargetRequirements(JsonObject actionObject) {
        BiFunction<Set<Set<Player>>, BasicWeapon, Set<Set<Player>>> addToEach;
        if (Field.INCLUDE_LAST.toString().equals(actionObject.get(Property.AND_TARGETS.toString()).getAsString())) {
            addToEach = (potentialTargets, weapon) -> {
                Set<Set<Player>> result = new HashSet<>(potentialTargets);
                Optional<Player> toAdd = inheritLastTarget(weapon.getAllTargets(), weapon.getCurrentShooter());
                //if there is a target to add, it is added
                if (toAdd.isPresent()) {
                    if (result.isEmpty()) {
                        //if there were no previous targets, a new set containing the extra target is created and added
                        Set<Player> set = new HashSet<>();
                        set.add(toAdd.get());
                        result.add(set);
                    } else {
                        //the extra target is added to each set
                        result.forEach(set -> set.add(toAdd.get()));
                    }
                }
                return result;
            };
        } else throw new IncoherentConfigurationException("Unknown andTargets specification: " + actionObject.get(Property.AND_TARGETS.toString()).toString());
        return addToEach;
    }

    /**
     * This method scans a {@code List<Player>} looking for the last {@code Player} that can be found in it as long as it
     * is different from the {@code activePlayer}
     *
     * @author Adriana Ferrari
     * @param allTargets the {@code List<Player>} to analyze
     * @param activePlayer the {@code Player} that should be excluded
     * @return an {@code Optional} containing the desired Player, if present
     */
    private static Optional<Player> inheritLastTarget(List<Player> allTargets, Player activePlayer) {
        Player toAdd = null;
        for (int i = allTargets.size() - 1; i >= 0 && toAdd == null; i--) {
            Player t = allTargets.get(i);
            if (t != activePlayer) {
                toAdd = t;
            }
        }
        return Optional.ofNullable(toAdd);
    }

    /**
     * This method creates a {@code BiFunction} that will remove some targets from the pre-computed {@code Set} of potential targets
     * See {@see jsonAttacks.md} to view the supported values for the {@code veto} property
     *
     * @author Adriana Ferrari
     * @param actionObject the {@code JsonObject} to analyze
     * @return a {@code BiFunction} that takes a {@code Set<Set<Player>>}, the pre-computed targets, and a {@code BasicWeapon}
     * and returns the updated {@code Set<Set<Player>>}
     * @throws IncoherentConfigurationException if the value of {@code veto} is not supported
     */
    private static BiFunction<Set<Set<Player>>, BasicWeapon, Set<Set<Player>>> readVeto(JsonObject actionObject) {
        BiFunction<Set<Set<Player>>, BasicWeapon, Set<Set<Player>>> veto;
        if (actionObject.has(Property.VETO.toString())) {
            Field value = EnumValueByString.findByString(actionObject.get(Property.VETO.toString()).getAsString(), Field.class);
            switch (value) {
                case LAST_HIT:
                    veto = (potentialTargets, weapon) -> {
                        List<Player> previouslyHit = weapon.getAllTargets();
                        List<Player> vetoList = new LinkedList<>();
                        if (!previouslyHit.isEmpty()) {
                            vetoList.add(previouslyHit.get(previouslyHit.size() - 1));
                        }
                        //the veto list contains only the last target hit by the weapon
                        return WeaponFactory.removeFromSet(potentialTargets, vetoList);
                    };
                    break;
                case ALL_PREVIOUS:
                    //all previously hit targets are to be removed
                    veto = (potentialTargets, weapon) -> WeaponFactory.removeFromSet(potentialTargets, weapon.getAllTargets());
                    break;
                case ALL_PREVIOUS_BLOCKS:
                    //all previously hit targets - and all the players on their block - are to be removed
                    veto = (potentialTargets, weapon) -> WeaponFactory.removeFromSet(potentialTargets, weapon.getAllTargets().stream().flatMap(p -> p.getBlock().getPlayers().stream()).collect(Collectors.toList()));
                    break;
                case HIT_BY_ADVANCED:
                    //all targets hit by advanced attacks are to be removed (valid only for WeaponWithMultipleEffects)
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
                    throw new IncoherentConfigurationException("Unknown veto: " + value);
            }
        } else {
            //no target is removed
            veto = (potentialTargets, previouslyHit) -> potentialTargets;
        }
        return veto;
    }

    /**
     * This method removes all the {@code T} elements contained in {@code toRemove} from the sets contained in {@code original}
     *
     * @author Adriana Ferrari
     * @param original the {@code Set<Set<T>>} from which the elements will be removed
     * @param toRemove the {@code List<T>} of elements to remove
     * @param <T> the type of the elements
     * @return the updated {@code Set<Set<T>>}
     */
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

    /**
     * This method reads the {@code startingPoint} property of {@code actionObject}. See {@see jsonAttacks.md} for info
     * about the supported values
     *
     * @author Adriana Ferrari
     * @param actionObject the {@code JsonObject} to analyze
     * @return a {@code Function} that appropriately computes a {@code Set<Block>} representing the possible starting points,
     * taking a {@code BasicWeapon}, the current weapon, as a parameter
     * @throws IncoherentConfigurationException if the value of {@code startingPoint} is not supported
     */
    private static Function<BasicWeapon, Set<Block>> readStartingPoint(JsonObject actionObject) {
        Function<BasicWeapon, Set<Block>> startingPointCalculator;
        if (actionObject.has(Property.STARTING_POINT.toString())) {
            Field value = EnumValueByString
                    .findByString(
                            actionObject.get(Property.STARTING_POINT.toString()).getAsString(),
                            Field.class
                    );
            switch (value) {
                case INHERIT:
                    startingPointCalculator = inheritStartingPoint();
                    break;
                case INHERIT_IF_PRESENT:
                    startingPointCalculator = inheritStartingPointIfPresent();
                    break;
                case ACTIVE_PLAYER:
                    startingPointCalculator = activePlayerStartingPoint();
                    break;
                case VISIBLE:
                    startingPointCalculator = visibleStartingPoint(actionObject);
                    break;
                case PREVIOUS_TARGET:
                    startingPointCalculator = previousTargetStartingPoint();
                    break;
                default:
                    throw new IncoherentConfigurationException("Unknown startingPoint: " + value);
            }
        } else {
            //previous starting point is maintained if present
            startingPointCalculator = weapon -> new HashSet<>(Collections.singletonList(
                    weapon.getStartingPoint().orElse(null)
            ));
        }
        return startingPointCalculator;
    }

    /**
     * This method returns a {@code Function} that inherits the previous starting point, which must be present
     *
     * @author Adriana Ferrari
     * @return a {@code Function} that appropriately computes a {@code Set<Block>} representing the possible starting points,
     * taking a {@code BasicWeapon}, the current weapon, as a parameter
     * @throws IncoherentConfigurationException if there was no previous starting point
     */
    private static Function<BasicWeapon, Set<Block>> inheritStartingPoint() {
        return weapon -> new HashSet<>(Collections.singletonList(
                weapon.getStartingPoint().orElseThrow(() -> new IncoherentConfigurationException("No starting point to inherit"))
        ));
    }

    /**
     * This method returns a {@code Function} that inherits the previous starting point if it is present, otherwise it sets it to the
     * default value (the {@code Block} of the active player)
     *
     * @author Adriana Ferrari
     * @return a {@code Function} that appropriately computes a {@code Set<Block>} representing the possible starting points,
     * taking a {@code BasicWeapon}, the current weapon, as a parameter
     */
    private static Function<BasicWeapon, Set<Block>> inheritStartingPointIfPresent() {
        return weapon -> new HashSet<>(Collections.singletonList(
                weapon.getStartingPoint().orElse(weapon.getCurrentShooter().getBlock())
        ));
    }

    /**
     * This method returns a {@code Function} that sets the starting point as the {@code Block} of the active player
     *
     * @author Adriana Ferrari
     * @return a {@code Function} that appropriately computes a {@code Set<Block>} representing the possible starting points,
     * taking a {@code BasicWeapon}, the current weapon, as a parameter
     */
    private static Function<BasicWeapon, Set<Block>> activePlayerStartingPoint() {
        return weapon -> new HashSet<>(Collections.singletonList(
                weapon.getCurrentShooter().getBlock()
        ));
    }

    /**
     * This method returns a {@code Function} that returns the potential starting points, which must be visible by the active player.
     * It also checks for the presence of {@code andNotStartingPoint}, which may add a further restriction
     *
     * @author Adriana Ferrari
     * @param actionObject the {@code JsonObject} to analyze
     * @return a {@code Function} that appropriately computes a {@code Set<Block>} representing the possible starting points,
     * taking a {@code BasicWeapon}, the current weapon, as a parameter
     */
    private static Function<BasicWeapon, Set<Block>> visibleStartingPoint(JsonObject actionObject) {
        if (actionObject.has("andNotStartingPoint") && actionObject.get("andNotStartingPoint").getAsString().equals(Field.ACTIVE_PLAYER.toString())) {
            return weapon -> {
                Set<Block> blocks = weapon.getCurrentShooter().getMatch().getBoard().getVisibleBlocks(weapon.getCurrentShooter().getBlock());
                blocks.removeIf(block -> !weapon.validStartingPoint(weapon.getActiveAttack(), block));
                blocks.remove(weapon.getCurrentShooter().getBlock());
                return blocks;
            };
        } else {
            return weapon -> {
                Set<Block> visibleBlocks = weapon.getCurrentShooter().getMatch().getBoard().getVisibleBlocks(weapon.getCurrentShooter().getBlock());
                visibleBlocks.removeIf(block -> !weapon.validStartingPoint(weapon.getActiveAttack(), block));
                return visibleBlocks;
            };
        }
    }

    /**
     * This method returns a {@code Function} that sets the starting point as the {@code Block} on which the last hit target
     * (different from the active player) can be found
     *
     * @author Adriana Ferrari
     * @return a {@code Function} that appropriately computes a {@code Set<Block>} representing the possible starting points,
     * taking a {@code BasicWeapon}, the current weapon, as a parameter
     * @throws IncoherentConfigurationException if no previous target could be found
     */
    private static Function<BasicWeapon, Set<Block>> previousTargetStartingPoint() {
        return weapon -> {
            Block block;
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
    }

    /**
     * This method reads the type of the action, possible values are specified in {@see jsonAttacks.md}
     *
     * @author Adriana Ferrari
     * @param actionObject the {@code JsonObject} to analyze
     * @param boardSize the dimension of the {@code Board}
     * @return a {@code TriConsumer} that handles the execution of the given action, given the targets (a {@code Set<Player}),
     * an {@code Interviewer} used to ask for eventual feedback and the {@code BasicWeapon}
     * @throws IncoherentConfigurationException if none of the supported actions was found
     */
    private static TriConsumer<Set<Player>, Interviewer, BasicWeapon> readActionType(final JsonObject actionObject, int boardSize) {
        TriConsumer<Set<Player>, Interviewer, BasicWeapon> executor;
        if (actionObject.has(Property.DAMAGE.toString())) {
            executor = readDamageExecutor(actionObject);
        } else if (actionObject.has(Property.MARK.toString())) {
            executor = readMarkExecutor(actionObject);
        } else if (actionObject.has(Property.MOVE.toString())) {
            executor = readMoveExecutor(actionObject, boardSize);
        } else throw new IncoherentConfigurationException("No action specified");
        return executor;
    }

    /**
     * This method returns a {@code TriConsumer} that will handle an action of type damage
     *
     * @author Adriana Ferrari
     * @param actionObject the {@code JsonObject} to analyze
     * @return a {@code TriConsumer} that handles the execution of a damage action, given the targets (a {@code Set<Player}),
     * an {@code Interviewer} used to ask for eventual feedback and the {@code BasicWeapon}
     */
    private static TriConsumer<Set<Player>, Interviewer, BasicWeapon> readDamageExecutor(final JsonObject actionObject) {
        return  (targets, interviewer, weapon) -> {
            int damageAmount = actionObject.get(Property.DAMAGE.toString()).getAsInt();
            List<DamageToken> tokens = new ArrayList<>();
            for (int i = 0; i < damageAmount; i++) {
                tokens.add(new DamageToken(weapon.getCurrentShooter()));
            }
            targets.forEach(target -> target.addDamageTokens(tokens));
        };
    }

    /**
     * This method returns a {@code TriConsumer} that will handle an action of type mark
     *
     * @author Adriana Ferrari
     * @param actionObject the {@code JsonObject} to analyze
     * @return a {@code TriConsumer} that handles the execution of a mark action, given the targets (a {@code Set<Player}),
     * an {@code Interviewer} used to ask for eventual feedback and the {@code BasicWeapon}
     */
    private static TriConsumer<Set<Player>, Interviewer, BasicWeapon> readMarkExecutor(final JsonObject actionObject) {
        return (targets, interviewer, weapon) -> {
            int markAmount = actionObject.get(Property.MARK.toString()).getAsInt();
            List<DamageToken> marks = new ArrayList<>();
            for (int i = 0; i < markAmount; i++) {
                marks.add(new DamageToken(weapon.getCurrentShooter()));
            }
            targets.forEach(target -> target.addMarks(marks));
        };
    }

    /**
     * This method returns a {@code TriConsumer} that will handle an action of type move
     *
     * @author Adriana Ferrari
     * @param actionObject the {@code JsonObject} to analyze
     * @param boardSize the size of the {@code Board}
     * @return a {@code TriConsumer} that handles the execution of a move action, given the targets (a {@code Set<Player}),
     * an {@code Interviewer} used to ask for eventual feedback and the {@code BasicWeapon}
     * @throws IncoherentConfigurationException if a {@code targetFinalPosition} is specified with a non-valid value (see {@see jsonAttacks.md} for info)
     */
    private static TriConsumer<Set<Player>, Interviewer, BasicWeapon> readMoveExecutor(final JsonObject actionObject, int boardSize) {
        final Range range = computeRange(actionObject.get(Property.MOVE.toString()).getAsJsonObject(), boardSize);
        if (actionObject.has(Property.TARGET_FINAL_POSITION.toString())) {
            Field position = EnumValueByString.findByString(actionObject.get(Property.TARGET_FINAL_POSITION.toString()).getAsString(), Field.class);
            switch (position) {
                case FIXED:
                    return readFixedTargetMoveExecutor(actionObject, range, boardSize);
                case STRAIGHT:
                    return readStraightTargetMoveExecutor(range);
                default:
                    throw new IncoherentConfigurationException("Unrecognized targetFinalPosition: " + position);
            }
        } else {
            return readStandardMoveExecutor(range);
        }
    }

    /**
     * This method returns a {@code TriConsumer} that will handle an action of type move with a {@code targetFinalPosition}
     * having {@code FIXED} as value
     *
     * @author Adriana Ferrari
     * @param actionObject the {@code JsonObject} to analyze
     * @param boardSize the size of the {@code Board}
     * @param range the {@code Range} of possible moves
     * @return a {@code TriConsumer} that handles the execution of a move action, given the targets (a {@code Set<Player}),
     * an {@code Interviewer} used to ask for eventual feedback and the {@code BasicWeapon}
     */
    private static TriConsumer<Set<Player>, Interviewer, BasicWeapon> readFixedTargetMoveExecutor(final JsonObject actionObject, final Range range, int boardSize) {
        final Range rangeFromStartingBlock = computeRange(actionObject.get(Property.TARGET_FINAL_DISTANCE.toString()).getAsJsonObject(), boardSize);
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

    /**
     * This method returns a {@code TriConsumer} that will handle an action of type move with a {@code targetFinalPosition}
     * having {@code STRAIGHT} as value
     *
     * @author Adriana Ferrari
     * @param range the {@code Range} of possible moves
     * @return a {@code TriConsumer} that handles the execution of a move action, given the targets (a {@code Set<Player}),
     * an {@code Interviewer} used to ask for eventual feedback and the {@code BasicWeapon}
     */
    private static TriConsumer<Set<Player>, Interviewer, BasicWeapon> readStraightTargetMoveExecutor(final Range range) {
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

    /**
     * This method returns a {@code TriConsumer} that will handle an action of type move without a {@code targetFinalPosition}
     *
     * @author Adriana Ferrari
     * @param range the {@code Range} of possible moves
     * @return a {@code TriConsumer} that handles the execution of a move action, given the targets (a {@code Set<Player}),
     * an {@code Interviewer} used to ask for eventual feedback and the {@code BasicWeapon}
     */
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
