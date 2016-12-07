package factory.cell;

import control.*;
import control.order.*;
import factory.conveyor.*;
import java.util.*;
import main.Main;
import main.Optimizer.OrderPossibility;
import transformation.*;

public class SerialCell extends Cell {

    private static final int MAX_BLOCKS_IN_CELL = 2; // TO|DO: A: decide on which optimization options to choose (Apparently, three blocks makes times worse)

    private final Mover t1;
    private final Rotator t2;
    private final Machine t3;
    private final Mover t4;
    private final Machine t5;
    private final Rotator t6;
    private final Mover t7;

    private boolean firstConveyorsBlocked = false;

    public SerialCell(String id) {
        super(id);

        // Create conveyors
        t1 = new Mover(id + "T1", 1);
        t2 = new Rotator(id + "T2");
        t3 = new Machine(id + "T3", Machine.Type.A);
        t4 = new Mover(id + "T4", 1);
        t5 = new Machine(id + "T5", Machine.Type.B);
        t6 = new Rotator(id + "T6");
        t7 = new Mover(id + "T7", 1);
        conveyors = new Conveyor[]{t1, t2, t3, t4, t5, t6, t7};

        // Connect conveyors
        t1.connections = new Conveyor[]{null, t2};
        t2.connections = new Conveyor[]{t1, null, null, t3};
        t3.connections = new Conveyor[]{t2, t4};
        t4.connections = new Conveyor[]{t3, t5};
        t5.connections = new Conveyor[]{t4, t6};
        t6.connections = new Conveyor[]{t7, t5, null, null};
        t7.connections = new Conveyor[]{null, t6};
    }

    @Override
    public void update() {
        super.update();

        refreshConveyorBlocking();
    }

    private void refreshConveyorBlocking() {
        boolean blocked = false;

        // This loop looks at all the blocks and sees if any
        // part of their paths contains the following transitions:
        //      t5 -> t4   or   t4 -> t3
        // Those transitions are backwards regarding the normal flow of blocks
        // in the cell (from top to bottom), meaning the conveyors t3 and t4
        // cannot have blocks there if this happens and are hence blocked
        outerLoop:
        for (Block b : blocksInside) {
            if (b.path.hasNext()) {
                for (int i = 1; i < b.path.length(); i++) {
                    Conveyor last = b.path.get(i - 1);
                    Conveyor current = b.path.get(i);

                    if ((last == t5 && current == t4) || (last == t4 && current == t3)) {
                        blocked = true;
                        break outerLoop;
                    }
                }
            }
        }

        firstConveyorsBlocked = blocked;
    }

    @Override
    protected Path pathEstimateForIncomingBlock(Block b) {
        return blockPathForTransformationSequence(b.transformations);
    }

    @Override
    public List<OrderPossibility> getOrderPossibilities(Set<Order> orders, double arrivalDelayEstimate) {
        List<OrderPossibility> ret = new ArrayList<>();

        orders.stream().filter((o) -> (o instanceof MachiningOrder)).map((o) -> (MachiningOrder) o).forEach((order) -> {
            order.possibleSequences(Machine.Type.Set.AB).stream().forEach((seq) -> {

                // >>>>> Calculate entersImmediately
                boolean entersImmediately = !firstConveyorsBlocked // TO|DO: A: decide on which optimization options to choose
                                            && !blocksIncoming
                        .stream()
                        .filter(b -> b.transformations.containsMachineType(Machine.Type.A))
                        .findFirst()
                        .isPresent()
                                            && !blocksInside
                        .stream()
                        .filter(b -> b.timeTravel(arrivalDelayEstimate).contains(t3))
                        .findFirst()
                        .isPresent()
                                            && blocksIncoming.size() + blocksInside.size() <= MAX_BLOCKS_IN_CELL;
                
                // >>>>> Calculate totalDuration
                double totalDuration = seq.totalDuration() + blockPathForTransformationSequence(seq).timeEstimate();

                // >>>>> Calculate priority                
                int priority;

                // Give higher priority to sequences where only machine B is present, since with those 3 blocks can enter the cell at once
                if (!seq.containsMachineType(Machine.Type.A)) {
                    priority = 1;
                }
                // Give lower priority to sequences that contain A after B, since with those the top conveyors are blocked
                else if (seq.containsMachineSequence(Machine.Type.B, Machine.Type.A)) {
                    priority = -1;
                }
                else {
                    priority = 0;
                }

                // >>>>> Calculate possibleExecutionCount TO|DO: A: decide on which optimization options to choose
                // If sequence only contains machine B, three blocks can enter at once TO|DO: A: might send more blocks than can fit in the cell
                int possibleExecutionCount = //min(
                        seq.containsMachineType(Machine.Type.A) ? 1 : MAX_BLOCKS_IN_CELL;
                //        MAX_BLOCKS_IN_CELL - blocksIncoming.size() + blocksInside.size()
                //);

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
        if (firstConveyorsBlocked) {
            return false;
        }

        block.path.append(blockPathForTransformationSequence(block.transformations));
        return true;
    }

    private Path blockPathForTransformationSequence(TransformationSequence seq) {
        Path path = new Path();

        path.push(t3);
        Conveyor current = t3;

        for (Transformation t : seq.sequence) {
            switch (t.machine) {
                case A:
                    if (current == t5) {
                        path.push(t4, t3);
                        current = t3;
                    }
                    break;
                case B:
                    if (current == t3) {
                        path.push(t4, t5);
                        current = t5;
                    }
                    break;
                default:
                    throw new Error("Invalid machine on sequence of a block");
            }
        }

        if (current == t3) {
            path.push(t4, t5);
        }

        path.push(t6);
        return path;
    }

    @Override
    public Conveyor getCornerConveyor(int position) {
        switch (position) {
            case 0: return t1;
            case 1: return t2;
            case 2: return t6;
            case 3: return t7;
            default:
                throw new IndexOutOfBoundsException("Cell " + id + " doesn't have position " + position);
        }
    }

    @Override
    public void connectWithRightCell(Cell right) {
        t2.connections[2] = right.getCornerConveyor(0);
        t6.connections[2] = right.getCornerConveyor(3);
    }

    @Override
    public void connectWithLeftCell(Cell left) {
        t1.connections[0] = left.getCornerConveyor(1);
        t7.connections[0] = left.getCornerConveyor(2);
    }

    @Override
    public Rotator getTopTransferInConveyor() {
        return t2;
    }
    
    @Override
    public Rotator getTopTransferOutConveyor() {
        return t2;
    }

    @Override
    public Rotator getBottomTransferConveyor() {
        return t6;
    }

    @Override
    public Conveyor getEntryConveyor() {
        return t2;
    }

    @Override
    public Conveyor getExitConveyor() {
        return t6;
    }

    @Override
    protected Cell processBlockOut(Block block) {
        return Main.factory.warehouse;
    }
}
