package it.polimi.ingsw.server.model.battlefield;

import it.polimi.ingsw.server.model.exceptions.UnauthorizedGrabException;
import it.polimi.ingsw.server.model.weapons.Weapon;

/**
 * This class represents all blocks of turret type, which are the blocks that refer to the bonus deck when a grab action occurs
 */
public class TurretBlock extends Block {
    /**
     * Class constructor given the position in the board and every BoarderType
     * @param row an int representing the row of the block in the board
     * @param column an int representing the column of the block in the board
     * @param borderNorth BorderType of the northern border
     * @param borderEast BorderType of the eastern border
     * @param borderSouth BorderType of the southern border
     * @param borderWest BorderType of the western border
     */
    public TurretBlock(int row, int column, BorderType borderNorth, BorderType borderEast, BorderType borderSouth, BorderType borderWest) {
        super(row, column, borderNorth, borderEast, borderSouth, borderWest);
    }

    @Override
    public void drop(Weapon w) {
        throw new UnauthorizedGrabException("Player is trying to drop a weapon in a Turret");
    }
}
