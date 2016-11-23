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

//        if (machine.id.startsWith("P")) {
//            System.out.format("[cell %s] processToolPreSelection machine = %s%n", id, machine.id);
//        }

        // Get next block to go to that machine and be processed there
        Block closestBlockToBeProcessed = null;
        int minStepCount = Integer.MAX_VALUE;

        for (Block b : blocksInside) {

            // Block has to go to that machine in the future and be processed there
            if (b.path.contains(machine) && b.getNextTransformationOnMachine(machine.type) != null) {

                // Calculate number of steps to machine for this block
                int count = 0;
                for (int i = 0; i < b.path.length(); i++) {
                    count++;

                    if (b.path.get(i) == machine) {
                        break;
                    }
                }
//                
//                if (machine.id.startsWith("P")) {
//                    System.out.format("[cell %s] processToolPreSelection loop: block = %s; steps = %d; path = %s%n", id, b, count, b.path);
//                }

                // If this block is closest, save that information
                if (count < minStepCount) {
//                    if (machine.id.startsWith("P")) {
//                        System.out.format("[cell %s] count < minStepCount: set closestBlockToBeProcessed = %s%n", id, b);
//                    }

                    minStepCount = count;
                    closestBlockToBeProcessed = b;
                }
            }
        }

        // Get next transformation that block will have on that machine, and pre-select the apropriate tool
        if (closestBlockToBeProcessed != null) {
            if (machine.id.startsWith("P")) {
//                System.out.format("[cell %s] machine %s preSelectTool %s for block %s%n",
//                                  id,
//                                  machine.id,
//                                  closestBlockToBeProcessed.getNextTransformationOnMachine(machine.type).tool,
//                                  closestBlockToBeProcessed);
            }

            machine.preSelectTool(closestBlockToBeProcessed.getNextTransformationOnMachine(machine.type).tool); // TODO: preselection for incomingBlocks too
        }
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
