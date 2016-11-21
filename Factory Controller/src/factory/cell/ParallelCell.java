package factory.cell;

import control.*;
import control.order.*;
import factory.*;
import factory.conveyor.*;
import java.util.*;
import main.Main;
import transformation.*;

public class ParallelCell extends Cell {

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

    private BlockRotation currentBlockRotation = BlockRotation.Undefined;

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

    private enum BlockRotation {
        Clockwise, CounterClockwise, Undefined;

        public boolean compatibleWith(BlockRotation other) {
            return this == Undefined || other == Undefined || this == other;
        }
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

        refreshBlockRotation();
    }

    private void refreshBlockRotation() {
        boolean free = true;

        for (Block block : blocksInside) {
            if (block.path.length() > 2) { // TODO: optimize and do > 3
                free = false;
                break;
            }
        }

        if (free) {
            currentBlockRotation = BlockRotation.Undefined;
        }
    }

    @Override
    public List<OrderPossibility> getOrderPossibilities(Set<Order> orders, double arrivalDelayEstimate) {
        List<OrderPossibility> ret = new ArrayList<>();

        for (Order o : orders) {
            if (o instanceof MachiningOrder) {
                MachiningOrder order = (MachiningOrder) o;

                for (TransformationSequence seq : order.possibleSequences()) {
                    if (seq.machineSet == Machine.Type.Set.BC) {

                        int priority = 1; // TODO: x

                        int possibleExecutionCount = 2 - blocksIncoming.size() - blocksInside.size();
                        if (possibleExecutionCount < 1) {
                            possibleExecutionCount = 1;
                        }

                        boolean entersImmediately = blocksIncoming.size() + blocksInside.size() < 2
                                                    && blockRotationForTransformationSequence(seq).compatibleWith(currentBlockRotation)
                                                    && !t4.hasBlock();

                        double totalDuration = seq.totalDuration() + blockPathForTransformationSequence(seq).timeEstimate();

                        ret.add(new OrderPossibility(
                                this, order, possibleExecutionCount, seq,
                                totalDuration, entersImmediately, priority
                        ));
                    }
                }
            }
        }

        return ret;
    }

    @Override
    protected boolean processBlockIn(Block block) {
        BlockRotation rotation = blockRotationForTransformationSequence(block.transformations);

        // Cannot have more than three blocks at once on the cell
        if (blocksInside.size() >= 3) {
            return false;
        }

        // Cannot have two blocks with opposite rotations
        if (!currentBlockRotation.compatibleWith(rotation)) {
            return false;
        }

        currentBlockRotation = rotation;
        block.path.append(blockPathForTransformationSequence(block.transformations));
        //t7.highestPriorityConnection = rotation == BlockRotation.Clockwise ? 1 : 0;

        //System.out.println("processBlockIn: currentBlockRotation = " + currentBlockRotation);
        return true;
    }

    private BlockRotation blockRotationForTransformationSequence(TransformationSequence seq) {
        if (seq.getFirstTransformation().machine == Machine.Type.B) {
            return BlockRotation.CounterClockwise;
        }
        else {
            return BlockRotation.Clockwise;
        }
    }

    private Path blockPathForTransformationSequence(TransformationSequence seq) {
        BlockRotation rotation = blockRotationForTransformationSequence(seq);
        Path path = new Path();

        path.push(t4);
        Conveyor current, next;

        if (seq.getFirstTransformation().machine == Machine.Type.B) {
            path.push(t5);
            current = t5;
        }
        else {
            path.push(t6);
            current = t6;
        }

        for (Transformation t : seq.sequence) {
            switch (t.machine) {
                case C:
                    if (current == t5) {
                        if (rotation == BlockRotation.CounterClockwise) {
                            next = t7;
                        }
                        else {
                            next = t4;
                        }

                        path.push(next, t6);
                    }

                    current = t6;
                    break;
                case B:
                    if (current == t6) {
                        if (rotation == BlockRotation.CounterClockwise) {
                            next = t4;
                        }
                        else {
                            next = t7;
                        }

                        path.push(next, t5);
                    }

                    current = t5;
                    break;
                default:
                    throw new Error("Invalid machine on sequence of a block");
            }
        }

        if (current == t5 && rotation == BlockRotation.Clockwise) {
            path.push(t4, t6);
        }
        else if (current == t6 && rotation == BlockRotation.CounterClockwise) {
            path.push(t4, t5);
        }

        path.push(t7, t9);
        return path;
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
