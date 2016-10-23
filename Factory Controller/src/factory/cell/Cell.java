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

    public String id;
    public Conveyor[] conveyorList;
    public Conveyor entryConveyor;
    public State state = State.Initializing;

    public Cell(String id) {
        this.id = id;
    }

    /**
     * @param position 0 = top left, turns clockwise
     * @return The conveyor in that position
     * @throws Error if the position is invalid
     */
    public abstract Conveyor getCornerConveyor(int position);

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
        for (Conveyor conveyor : conveyorList) {
            conveyor.update();
        }
        
        if (entryConveyor != null) {
            if (entryConveyor.isIdle() && entryConveyor.hasBlock()) {
                Block b = entryConveyor.blocks[0]; // TODO access block in a safer way
                
                if (!b.path.hasNext()) {
                    registerNewIncomingBlock(b);
                }
            }
        }
    }
    
    public abstract void registerNewIncomingBlock(Block b);

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

    // TODO: review: is this state necessary
    public enum State {
        Initializing, Working;
    }
}
