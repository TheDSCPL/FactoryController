package factory.cell;

import control.*;
import control.order.*;
import factory.conveyor.*;
import java.util.*;
import main.*;
import main.Optimizer.OrderPossibility;

public abstract class Cell {

    public static void connect(List<Cell> cells) {
        if (cells.size() <= 1) {
            return;
        }
        for (int i = 1; i < cells.size(); i++) {
            Cell.connect(cells.get(i - 1), cells.get(i));
        }
    }

    public static void connect(Cell left, Cell right) {
        left.connectWithRightCell(right);
        right.connectWithLeftCell(left);
    }

    public final String id;
    protected Conveyor[] conveyors;
    protected final Set<Block> blocksInside = new HashSet<>();
    protected final Set<Block> blocksIncoming = new HashSet<>();

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
     * @return The rotator on the top
     */
    public abstract Conveyor getTopTransferConveyor();

    /**
     * @return The rotator on the bottom
     */
    public abstract Conveyor getBottomTransferConveyor();

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

    public void addIncomingBlocks(List<Block> blocks) {
        blocksIncoming.addAll(blocks);
    }

    public abstract List<OrderPossibility> getOrderPossibilities(Set<Order> orders, double arrivalDelayEstimate);

    abstract protected boolean processBlockIn(Block block);

    abstract protected Cell processBlockOut(Block block);

    /**
     * Update the FSM of this cell and the FSMs of the conveyors
     */
    public void update() {
        // Update all conveyors in the cell
        for (Conveyor conveyor : conveyors) {
            conveyor.update();
        }

        // Process blocks coming in
        Conveyor entry = getEntryConveyor();
        if (entry != null) {
            if (entry.isIdle() && entry.hasBlock()) {
                Block b = entry.getOneBlock();

                // If block wants to get inside this cell
                if (!b.path.hasNext() && blocksIncoming.contains(b)) {
                    if (processBlockIn(b)) {
                        Main.stats.inc(id, Statistics.Type.BlocksReceived, b.type);
                        blocksInside.add(b);
                        blocksIncoming.remove(b);
                    }
                }
            }
        }

        // Process blocks going out
        Conveyor exit = getExitConveyor();
        if (exit != null) {
            if (exit.isIdle() && exit.hasBlock()) {
                Block b = exit.getOneBlock();

                // If block came from this cell and is not just passing by
                if (!b.path.hasNext() && blocksInside.contains(b)) {
                    Cell destination = processBlockOut(b);
                    Main.stats.inc(id, Statistics.Type.BlocksSent, b.type);
                    b.path.append(Main.factory.blockTransportPath(this, destination));
                    blocksInside.remove(b);
                    if(b.isStacked() && b.otherAssemblyBlock != null)
                        blocksInside.remove(b.otherAssemblyBlock);
                    System.err.println("out block path: " + b.path);
                }
            }
        }

        // Process any pre-selection of tools on machines
        processToolPreSelection();
    }

    private void processToolPreSelection() {
        for (Conveyor c : conveyors) {
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

        // Get all blocks
        Set<Block> allBlocks = new HashSet(blocksInside);
        allBlocks.addAll(blocksIncoming);
        
        for (Block b : allBlocks) {

            Path path;

            // Get path for block
            if (blocksInside.contains(b)) {
                path = b.path;
            }
            else {
                path = b.path.copy();
                path.append(pathEstimateForIncomingBlock(b));
            }

            // Block has to go to that machine in the future and be processed there
            if (path.contains(machine) && b.getNextTransformationOnMachine(machine.type) != null) {

                // Calculate number of steps to machine for this block
                int count = 0;
                for (int i = 0; i < path.length(); i++) {
                    count++;

                    if (path.get(i) == machine) {
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
    
    protected Path pathEstimateForIncomingBlock(Block b) {
        return new Path();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(id).append(": ");
        for (Conveyor conv : conveyors) {
            sb.append(conv.id).append("(").append(conv.getClass().getSimpleName()).append(")").append(" ");
        }

        return sb.toString();
    }
}
