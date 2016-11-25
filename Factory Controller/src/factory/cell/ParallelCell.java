package factory.cell;

import control.*;
import control.order.*;
import factory.conveyor.*;
import java.util.*;
import main.*;
import main.Optimizer.OrderPossibility;
import transformation.*;

public class ParallelCell extends Cell {
    
    private static final int MAX_BLOCKS_IN_CELL = 2;
    
    private final Mover t1;
    private final Rotator t2;
    private final Mover t3;
    private final Rail t4;
    private final Machine t5;
    private final Machine t6;
    private final Rail t7;
    private final Mover t8;
    private final Rotator t9;
    private final Mover t10;

    //private BlockRotation currentBlockRotation = BlockRotation.Undefined;
    @Override
    public Conveyor getEntryConveyor() {
        return t2;
    }

    @Override
    public Conveyor getExitConveyor() {
        return t9;
    }

    @Override
    protected Cell processBlockOut(Block block) {
        return Main.factory.warehouse;
    }

    public ParallelCell(String id) {
        super(id);

        // Create conveyors
        t1 = new Mover(id + "T1", 1);
        t2 = new Rotator(id + "T2");
        t3 = new Mover(id + "T3", 2);
        t4 = new Rail(id + "T4");
        t5 = new Machine(id + "T5", Machine.Type.B);
        t6 = new Machine(id + "T6", Machine.Type.C);
        t7 = new Rail(id + "T7");
        t8 = new Mover(id + "T8", 1);
        t9 = new Rotator(id + "T9");
        t10 = new Mover(id + "T10", 2);
        conveyors = new Conveyor[]{t1, t2, t3, t4, t5, t6, t7, t8, t9, t10};

        // Connect conveyors
        t1.connections = new Conveyor[]{null, t2};
        t2.connections = new Conveyor[]{t1, null, t3, t4};
        t3.connections = new Conveyor[]{t2, null};
        t4.connections = new Conveyor[]{t2, null, t6, t5};
        t5.connections = new Conveyor[]{t4, t7};
        t6.connections = new Conveyor[]{t4, t7};
        t7.connections = new Conveyor[]{t5, t6, null, t9};
        t8.connections = new Conveyor[]{null, t9};
        t9.connections = new Conveyor[]{t8, t7, t10, null};
        t10.connections = new Conveyor[]{t9, null};

        // Set transfer priorities
        //t4.highestPriorityConnection = 0;
    }

    @Override
    public void update() {
        super.update();

        refreshBlockPaths();
    }

    private void refreshBlockPaths() {

        // Machine B, conveyor t5
        if (!t5.isSending() && !t5.isReceiving() && t5.hasBlock()) {

            Block b = t5.getOneBlock();

            if (!b.path.hasNext()) {
                Transformation t = b.getNextTransformation();

                if (t != null) {
                    if (t.machine == Machine.Type.C) {
                        if (hasBlockRotatingCW()) { // Go using top conveyor
                            b.path.push(t4, t6);
                        }
                        else { // Go using bottom conveyor
                            b.path.push(t7, t6);
                        }
                    }
                }
                else {
                    int x = 0;
                    if (hasBlockRotatingCW()) {
                        b.path.push(t4, t6, t7, t9);
                    }
                    else {
                        b.path.push(t7, t9);
                    }
                }
            }
        }

        // Machine C, conveyor t6
        if (!t6.isSending() && !t6.isReceiving() && t6.hasBlock()) {
            Block b = t6.getOneBlock();

            if (!b.path.hasNext()) {
                Transformation t = b.getNextTransformation();

                if (t != null) {
                    if (t.machine == Machine.Type.B) {
                        if (hasBlockRotatingCW()) { // Go using bottom conveyor
                            b.path.push(t7, t5);
                        }
                        else { // Go using top conveyor
                            b.path.push(t4, t5);
                        }
                    }
                }
                else {
                    int x = 0;
                    if (hasBlockRotatingCCW()) {
                        b.path.push(t4, t5, t7, t9);
                    }
                    else {
                        b.path.push(t7, t9);
                    }
                }
            }
        }

    }

    private boolean hasBlockRotatingCW() {
        for (Block block : blocksInside) {
            if (block.path.contains(t5, t4) || block.path.contains(t6, t7) || block.path.contains(t4, t6) || block.path.contains(t7, t5)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasBlockRotatingCCW() {
        for (Block block : blocksInside) {
            if (block.path.contains(t6, t4) || block.path.contains(t4, t5) || block.path.contains(t5, t7) || block.path.contains(t7, t6)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<OrderPossibility> getOrderPossibilities(Set<Order> orders, double arrivalDelayEstimate) {
        List<OrderPossibility> ret = new ArrayList<>();

        orders.stream().filter((o) -> (o instanceof MachiningOrder)).map((o) -> (MachiningOrder) o).forEach((order) -> {
            order.possibleSequences(Machine.Type.Set.BC).stream().forEach((seq) -> {

                // >>>>> TODO: Calculate entersImmediately using arrivalDelayEstimate
                boolean entersImmediately = blocksIncoming.size() + blocksInside.size() + 1 <= MAX_BLOCKS_IN_CELL;

                // >>>>> TODO: Calculate priority
                int priority = 1;

                // >>>>> TODO: Calculate possibleExecutionCount
                int possibleExecutionCount = MAX_BLOCKS_IN_CELL - blocksIncoming.size() - blocksInside.size();
                //if (possibleExecutionCount < 1) {
                //    possibleExecutionCount = 1; // TODO: needed?
                //}

                // >>>>> Calculate totalDuration
                double totalDuration = seq.totalDuration();// + blockPathForTransformationSequence(seq).timeEstimate(); TODO: finish

                // >>>>> Add possibility
                ret.add(new OrderPossibility(
                        this, order, possibleExecutionCount, seq,
                        totalDuration, entersImmediately, priority
                ));
            });
        });

        return ret;
    }

    @Override
    protected boolean processBlockIn(Block block) {

        // Cannot have more than two blocks at once on the cell
        if (blocksInside.size() >= MAX_BLOCKS_IN_CELL) {
            return false;
        }

        Machine.Type firstMachine = block.transformations.getFirstTransformation().machine;

        if (firstMachine == Machine.Type.B) {

            boolean hasOtherBlockGoingUnderCW = false;

            for (Block b : blocksInside) {
                if (b.path.contains(t6, t7) || b.path.contains(t7, t5)) {
                    hasOtherBlockGoingUnderCW = true;
                    break;
                }
            }

            if (hasOtherBlockGoingUnderCW) {
                block.path.push(t4, t6, t7, t5);
            }
            else {
                block.path.push(t4, t5);
            }

        }
        else if (firstMachine == Machine.Type.C) {

            boolean hasOtherBlockGoingUnderCCW = false;

            for (Block b : blocksInside) {
                if (b.path.contains(t5, t7) || b.path.contains(t7, t6)) {
                    hasOtherBlockGoingUnderCCW = true;
                    break;
                }
            }

            if (hasOtherBlockGoingUnderCCW) {
                block.path.push(t4, t5, t7, t6);
            }
            else {
                block.path.push(t4, t6);
            }

        }

        return true;
    }

    @Override
    public Conveyor getCornerConveyor(int position) {
        switch (position) {
            case 0: return t1;
            case 1: return t3;
            case 2: return t10;
            case 3: return t8;
            default:
                throw new IndexOutOfBoundsException("Cell " + id + " doesn't have position " + position);
        }
    }

    @Override
    public void connectWithRightCell(Cell right) {
        t3.connections[1] = right.getCornerConveyor(0);
        t10.connections[1] = right.getCornerConveyor(3);
    }

    @Override
    public void connectWithLeftCell(Cell left) {
        t1.connections[0] = left.getCornerConveyor(1);
        t8.connections[0] = left.getCornerConveyor(2);
    }

    @Override
    public Rotator getTopTransferConveyor() {
        return t2;
    }

    @Override
    public Conveyor getBottomTransferConveyor() {
        return t9;
    }
}
