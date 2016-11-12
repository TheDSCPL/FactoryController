/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.cell;

import control.*;
import factory.conveyor.*;
import java.util.HashSet;
import java.util.Set;
import main.Main;

/**
 *
 * @author Alex
 */
public abstract class Cell {

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

    public final String id;
    protected Conveyor[] conveyorList;
    protected final Set<Block> blocksInside = new HashSet<>();

    /**
     * Not null whenever there is a block present in this cell's entryConveyor
     * that wants to get in the cell for processing. Set this variable to null
     * *after setting a path to the block* to signal the Cell class that the
     * subclass is ready to process another block
     */
    //protected Block incomingBlock;
    public Cell(String id) {
        this.id = id;
    }

    /**
     * @return A time estimate in milliseconds of how much a block on the entry
     * conveyor right now would have to wait to get in the cell
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

        // Process blocks coming in
        Conveyor entry = getEntryConveyor();
        if (entry != null) {
            if (entry.isIdle() && entry.hasBlock()) {
                Block b = entry.getOneBlock();

                if (!b.path.hasNext()) {
                    if (processBlockIn(b)) {
                        blocksInside.add(b);
                    }
                }
            }
        }

        // Process blocks going out
        Conveyor exit = getExitConveyor();
        if (exit != null) {
            if (exit.isIdle() && exit.hasBlock()) {
                // If block came from this cell and is not just passing by
                if (!exit.getOneBlock().path.hasNext()) {
                    Block b = exit.getOneBlock();
                    processBlockOut(b);
                    b.path.append(Main.factory.exitPathToWarehouse(this));
                    blocksInside.remove(b);
                }
            }
        }

        // Process any pre-selection of tools on machines
        processToolPreSelection();
    }

    protected boolean processBlockIn(Block block) {
        return false;
    }

    protected void processBlockOut(Block block) {

    }

    private void processToolPreSelection() {
        for (Conveyor c : conveyorList) {
            if (c instanceof Machine) {
                Machine m = (Machine) c;
                if (m.canPreSelectTool()) {
                    processToolPreSelection(m);
                }
            }
        }
    }

    private void processToolPreSelection(Machine machine) {

        // Get next block to go to that machine and be processed there
        Block closestBlockToBeProcessed = null;
        int minStepCount = Integer.MAX_VALUE;

        for (Block b : blocksInside) {

            // Block has to go to that machine in the future and be processed there
            if (b.path.path.contains(machine) && b.getNextTransformationOnMachine(machine.type) != null) {

                // Calculate number of steps to machine for this block
                int count = 0;
                for (Conveyor c : b.path.path) {
                    count++;
                    if (c == machine) {
                        break;
                    }
                }

                // If this block is closest, save that information
                if (count < minStepCount) {
                    minStepCount = count;
                    closestBlockToBeProcessed = b;
                }
            }
        }

        // Get next transformation that block will have on that machine, and pre-select the apropriate tool
        if (closestBlockToBeProcessed != null) {
            machine.preSelectTool(closestBlockToBeProcessed.getNextTransformationOnMachine(machine.type).tool);
        }
    }
}
