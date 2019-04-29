package it.polimi.ingsw.server.model.battlefield;

import it.polimi.ingsw.server.model.exceptions.UnauthorizedExchangeException;
import it.polimi.ingsw.server.model.weapons.WeaponTile;
import it.polimi.ingsw.shared.Direction;

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

    /**
     * Turret blocks do not support the dropping action, so an exception must be thrown
     *
     * @param weapon the weapon to drop
     */
    @Override
    public void drop(WeaponTile weapon) {
        throw new UnauthorizedExchangeException("Player is trying to drop a weapon in a Turret");
    }

    /**
     * Copies the current block creating a new one constructed with the same initial parameters
     *
     * @return the copy
     */
    @Override
    public Block copy() {
        return new TurretBlock(
                this.getRow(),
                this.getColumn(),
                this.getBorderType(Direction.NORTH),
                this.getBorderType(Direction.EAST),
                this.getBorderType(Direction.SOUTH),
                this.getBorderType(Direction.WEST)
        );
    }

}
