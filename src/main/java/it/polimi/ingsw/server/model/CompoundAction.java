package it.polimi.ingsw.server.model;

import java.util.List;

/**
 * This class is used to offer to a player the list of all possible actions
 */
public final class CompoundAction {

    /**
     * List of all basic actions. The compound action is a combination of this set of actions
     */
    private List<BasicAction> actions;

    /**
     * Class constructor
     * @param actions actions that can be selected
     */
    public CompoundAction(List<BasicAction> actions) {
        this.actions = actions;
    }


    public List<BasicAction> getActions() {
        //TODO all function
        return actions;
    }
}
