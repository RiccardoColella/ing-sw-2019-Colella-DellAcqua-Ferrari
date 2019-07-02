package it.polimi.ingsw.server.model.player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents a player's Action Tile
 *
 * @author Carlo Dell'Acqua
 */
public class ActionTile {

    /**
     * The possible types of Action Tile
     */
    public enum Type {
        STANDARD,
        ADRENALINE_1,
        ADRENALINE_2,
        FINAL_FRENZY_SINGLE_MODE,
        FINAL_FRENZY_DOUBLE_MODE
    }

    /**
     * The compound action associated with this action tile
     */
    private final List<List<CompoundAction>> compoundActions;
    /**
     * The type of this action tile
     */
    private final Type type;

    /**
     * Creates an ActionTile given a list of compound actions
     *
     * @param type the type of action tile based on the current status of the player and of the match
     * @param compoundActions the list of list of compound actions this action tile should contain
     */
    public ActionTile(Type type, List<List<CompoundAction>> compoundActions) {
        this.type = type;
        this.compoundActions = Collections.unmodifiableList(
                compoundActions.stream().map(Collections::unmodifiableList)
                .collect(Collectors.toList())
        );
    }

    /**
     * @return the read-only list of compound actions
     */
    public List<List<CompoundAction>> getCompoundActions() {
        return compoundActions;
    }

    /**
     * @return the type of action tile
     */
    public Type getType() {
        return this.type;
    }
}
