/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.cell;

import control.*;
import factory.conveyor.*;

/**
 *
 * @author Alex
 */
public abstract class Cell {

    public final String id;
    protected Conveyor[] conveyorList;

    /**
     * Not null whenever there is a block present in this cell's entryConveyor
     * that wants to get in the cell for processing. Set this variable to null
     * *after setting a path to the block* to signal the Cell class that the
     * subclass is ready to process another block
     */
    protected Block incomingBlock;

    public Cell(String id) {
        this.id = id;
    }
    
    /**
     * @return A time estimate in milliseconds of how much a block on the
     * entry conveyor right now would have to wait to get in the cell
     */
    public abstract long getEntryDelayTimeEstimate();

    /**
     * @param position 0 = top left, turns clockwise
     * @return The conveyor in that position
     * @throws Error if the position is invalid
     */
    public abstract Conveyor getCornerConveyor(int position);

    /**
     * @return The Conveyor that is the entry point for blocks on this cell
     * (normally, the rotator on the top)
     */
    public abstract Conveyor getEntryConveyor();

    /**
     * @return The Conveyor that is the exit point for blocks on this cell
     * (normally, the rotator on the bottom)
     */
    public abstract Conveyor getExitConveyor();

    /**
     * This cell will be connected to a cell on its right
     *
     * @param right Cell on the right side of this
     */
    public abstract void connectWithRightCell(Cell right);

    /**
     * This cell will be connected to a cell on its left
     *
     * @param left Cell on the left side of this
     */
    public abstract void connectWithLeftCell(Cell left);

    /**
     * Update the FSM of this cell and the FSMs of the conveyors
     */
    public void update() {
        // Update all conveyors in the cell
        for (Conveyor conveyor : conveyorList) {
            conveyor.update();
        }

        // See if there is any block waiting to get in the cell
        if (incomingBlock == null) {
            Conveyor entryConveyor = getEntryConveyor();
            
            if (entryConveyor != null) {
                if (entryConveyor.isIdle() && entryConveyor.hasBlock()) {
                    Block b = entryConveyor.getOneBlock();

                    if (!b.path.hasNext()) {
                        incomingBlock = b;
                    }
                }
            }
        }
    }

    public static void connect(Cell... cells) {
        if (cells.length == 0) {
            return;
        }
        for (int i = 1; i < cells.length; i++) {
            Cell.connect(cells[i - 1], cells[i]);
        }
    }

    public static void connect(Cell left, Cell right) {
        left.connectWithRightCell(right);
        right.connectWithLeftCell(left);
    }
}
