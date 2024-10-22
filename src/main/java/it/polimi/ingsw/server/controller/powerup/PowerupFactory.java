package it.polimi.ingsw.server.controller.powerup;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import it.polimi.ingsw.server.model.battlefield.Block;
import it.polimi.ingsw.server.model.battlefield.Board;
import it.polimi.ingsw.server.model.player.DamageToken;
import it.polimi.ingsw.server.model.player.Player;
import it.polimi.ingsw.server.view.Interviewer;
import it.polimi.ingsw.shared.Direction;
import it.polimi.ingsw.shared.messages.ClientApi;
import it.polimi.ingsw.utils.ConfigFileMaker;
import it.polimi.ingsw.utils.EnumValueByString;
import it.polimi.ingsw.utils.Range;

import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Builds all the powerup with the associated effects
 *
 * @author Carlo Dell'Acqua
 */
public class PowerupFactory {

    /**
     * JSON conversion utility
     */
    private static final Gson gson = new Gson();

    /**
     * Configuration class used for JSON deserialization
     */
    private static class PowerupConfig {
        /**
         * Powerup name
         */
        public String name;
        /**
         * Powerup trigger
         */
        public Powerup.Trigger trigger;
        /**
         * Powerup target
         */
        public Powerup.Target target;
        /**
         * Powerup target constraint
         */
        public Powerup.TargetConstraint targetConstraint;
        /**
         * Powerup cost
         */
        public int cost;
        /**
         * Powerup effect specification
         */
        public JsonObject[] effects;
    }

    /**
     * Supported effect types
     */
    private enum EffectType {
        MARK,
        DAMAGE,
        TELEPORT,
        MOVE_IN_DIRECTION
    }

    /**
     * The path of the JSON configuration file
     */
    private static final String POWERUP_JSON_PATH = "./config/powerupEffects.json";
    private static final String POWERUP_JSON_PATH_RES = "/config/powerupEffects.json";

    /**
     * A {@code Map} associating each name to the associated {@code Powerup}
     */
    private static Map<String, Powerup> powerupMap;

    /**
     * Private empty constructor because this class should not have instances
     */
    private PowerupFactory() { }

    /**
     * Gets all the powerup at once
     *
     * @return a collection of all known powerups
     */
    public static Collection<Powerup> getAll() {
        return getPowerupMap().values();
    }


    /**
     * Lazy loader of the powerup map
     *
     * @return the loaded powerup map
     */
    public static Map<String, Powerup> getPowerupMap() {
        if (powerupMap == null) {
            powerupMap = new HashMap<>();
            PowerupConfig[] powerupConfigs;

            powerupConfigs = gson.fromJson(
                    ConfigFileMaker.load(POWERUP_JSON_PATH, POWERUP_JSON_PATH_RES),
                    PowerupConfig[].class
            );

            for (PowerupConfig config: powerupConfigs) {
                powerupMap.put(config.name, new Powerup(config.name, config.trigger, config.target, config.targetConstraint, config.cost, ((owner, target, interviewer) -> {
                    for (JsonObject effect : config.effects) {
                        EffectType type = EnumValueByString.findByString(
                                effect.get("type").getAsString(),
                                EffectType.class
                        );
                        switch (type) {
                            case MARK:
                                manageDamageToken(target, new DamageToken(owner), true);
                                break;
                            case DAMAGE:
                                manageDamageToken(target, new DamageToken(owner), false);
                                break;
                            case MOVE_IN_DIRECTION:
                                manageMoveInDirection(owner, target, interviewer, gson.fromJson(effect.get("range"), Range.class));
                                break;
                            case TELEPORT:
                                manageTeleport(target, interviewer);
                                break;
                            default:
                                throw new UnsupportedOperationException("Effect type " + type + " not supported");
                        }
                    }
                })));
            }
        }
        return powerupMap;
    }

    /**
     * Manages effects which give a mark or a damage token to a target
     *
     * @param target the target of this effect
     * @param token the damage token to give to the target
     * @param isMark whether or not the damage token should be added as a mark or a damage
     */
    private static void manageDamageToken(Player target, DamageToken token, boolean isMark) {
        if (isMark) {
            target.addMark(token);
        } else {
            target.addDamageToken(token);
        }
    }

    /**
     * Manages effects which moves the target in a fixed direction
     *
     * @param owner the owner of the powerup
     * @param target the target of this effect
     * @param interviewer the communication proxy
     * @param range the range of blocks available for the moves
     */
    private static void manageMoveInDirection(Player owner, Player target, Interviewer interviewer, Range range) {
        Block start = target.getBlock();
        Board board = target.getMatch().getBoard();
        Set<Direction> options = Arrays
                .stream(Direction.values())
                .filter(dir -> start.getBorderType(dir) != Block.BorderType.WALL)
                .collect(Collectors.toSet());

        String question = "In which direction do you want to move " + (target == owner ? " yourself" : target.getPlayerInfo().getNickname()) + "?";

        Optional<Direction> chosenDirection =
                (range.getMin() > 0) ?
                        Optional.of(interviewer.select(question, options, ClientApi.DIRECTION_QUESTION)) :
                        interviewer.selectOptional(question, options, ClientApi.DIRECTION_QUESTION);

        for (int i = 0; (i < range.getMax()) && chosenDirection.isPresent() && target.getBlock().getBorderType(chosenDirection.get()) != Block.BorderType.WALL; i++) {
            board.movePlayer(target, chosenDirection.get());
            if (target.getBlock().getBorderType(chosenDirection.get()).equals(Block.BorderType.WALL)) {
                chosenDirection = Optional.empty();
            } else {
                chosenDirection = range.getMin() > (i + 1) ?
                        chosenDirection :
                        interviewer.selectOptional("Continue?", Collections.singleton(chosenDirection.get()), ClientApi.DIRECTION_QUESTION);
            }
        }
    }

    /**
     * Manages effects which teleport the target on an arbitrary block
     *
     * @param target the target of this effect
     * @param interviewer the communication proxy
     */
    private static void manageTeleport(Player target, Interviewer interviewer) {
        Set<Block> potentialBlocks = target.getMatch().getBoard().getBlocks();
        Point blockAsPoint = interviewer.select(
                "Where do you want to teleport?",
                potentialBlocks.stream().map(block -> new Point(block.getColumn(), block.getRow())).collect(Collectors.toSet()),
                ClientApi.BLOCK_QUESTION
        );
        target.getMatch().getBoard().teleportPlayer(
                target,
                target
                    .getMatch()
                    .getBoard()
                    .getBlock(blockAsPoint.y, blockAsPoint.x)
                    .orElseThrow(() -> new IllegalStateException("Non existent block was chosen"))
        );
    }
}
