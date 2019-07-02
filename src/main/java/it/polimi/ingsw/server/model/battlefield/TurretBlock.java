package it.polimi.ingsw.server.model.battlefield;

import it.polimi.ingsw.server.model.currency.BonusTile;
import it.polimi.ingsw.server.model.events.BonusTileEvent;
import it.polimi.ingsw.server.model.events.WeaponEvent;
import it.polimi.ingsw.server.model.events.listeners.SpawnpointListener;
import it.polimi.ingsw.server.model.events.listeners.TurretBlockListener;
import it.polimi.ingsw.server.model.exceptions.UnauthorizedExchangeException;
import it.polimi.ingsw.server.model.weapons.WeaponTile;
import it.polimi.ingsw.shared.Direction;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * This class represents all blocks of turret type, which are the blocks that refer to the bonus deck when a grab action occurs
 */
public class TurretBlock extends Block {

    /**
     * The bonus tile contained in this turret
     */
    private BonusTile bonusTile;

    /**
     * The listeners of this turret
     */
    private Set<TurretBlockListener> listeners = new HashSet<>();

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
     * Set the turret block BonusTile
     *
     * @param bonusTile the bonus tile to set
     */
    @Override
    public void drop(Droppable bonusTile) {
        if (bonusTile instanceof BonusTile) {
            this.bonusTile = (BonusTile)bonusTile;
            notifyBonusTileDropped((BonusTile)bonusTile);
        } else throw new IllegalArgumentException("Dropping was not possible, the turret only accepts bonus tiles");
    }

    /**
     * Grab the placed bonus tiles if present, otherwise return Optional.empty()
     *
     * @return the previously placed bonus tile
     */
    public Optional<BonusTile> grab() {
        if (bonusTile != null) {
            notifyBonusTileGrabbed(this.bonusTile);
            BonusTile result = this.bonusTile;
            this.bonusTile = null;
            return Optional.of(result);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Get the current placed bonus tile
     *
     * @return the currently available bonus tile
     */
    public Optional<BonusTile> getBonusTile() {
        return Optional.ofNullable(this.bonusTile);
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

    /**
     * Adds a new listener
     *
     * @param l the new listener
     */
    public void addTurretBlockListener(TurretBlockListener l) {
        listeners.add(l);
    }

    /**
     * Removes the given listener
     *
     * @param l the listener to remove
     */
    public void removeTurretBlockListener(TurretBlockListener l) {
        listeners.remove(l);
    }

    /**
     * Notifies the listeners that a bonus tile has been dropped here
     *
     * @param bonusTile the bonus tile that was dropped
     */
    public void notifyBonusTileDropped(BonusTile bonusTile) {
        BonusTileEvent e = new BonusTileEvent(this, bonusTile);
        listeners.forEach(l -> l.onBonusTileDropped(e));
    }

    /**
     * Notifies the listeners that a bonus tile was grabbed from here
     *
     * @param bonusTile the bonus tile that was grabbed
     */
    public void notifyBonusTileGrabbed(BonusTile bonusTile) {
        BonusTileEvent e = new BonusTileEvent(this, bonusTile);
        listeners.forEach(l -> l.onBonusTileGrabbed(e));
    }

}
