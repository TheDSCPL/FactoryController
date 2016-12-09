package factory.cell;

import control.*;
import control.order.*;
import factory.conveyor.*;
import java.util.*;
import static java.util.stream.Collectors.*;
import main.*;
import main.Optimizer.OrderPossibility;

public class LoadUnloadCell extends Cell {

    private final Mover t1;
    private final Rotator t2;
    private final Mover t3;
    private final Pusher t4;
    private final Pusher t5;
    private final Mover t6;
    private final Rotator t7;
    private final Mover t8;

    public LoadUnloadCell(String id) {
        super(id);

        // Create conveyors
        t1 = new Mover(id + "T1", 1);
        t2 = new Rotator(id + "T2");
        t3 = new Mover(id + "T3", 1);
        t4 = new Pusher(id + "T4", "PM1");
        t5 = new Pusher(id + "T5", "PM2");
        t6 = new Mover(id + "T6", 1);
        t7 = new Rotator(id + "T7");
        t8 = new Mover(id + "T8", 1);
        conveyors = new Conveyor[]{t1, t2, t3, t4, t5, t6, t7, t8};

        // Connect conveyors
        t1.connections = new Conveyor[]{null, t2};
        t2.connections = new Conveyor[]{t1, null, t3, t4};
        t3.connections = new Conveyor[]{t2, null};
        t4.connections = new Conveyor[]{t2, t5};
        t5.connections = new Conveyor[]{t4, t7};
        t6.connections = new Conveyor[]{null, t7};
        t7.connections = new Conveyor[]{t6, t5, t8, null};
        t8.connections = new Conveyor[]{t7, null};
    }

    @Override
    public void update() {
        super.update();

        // Detect load orders on pusher t4 -> send block to warehouse in
        if (t3.isPresenceSensorOn(0)) {
            // Create block
            Block block = new Block(Block.Type.Unknown);
            block.path.push(t3, t2, t4, t5, t7);

            // Place block on this conveyor
            t3.placeBlock(block, 0);

            // Add block to cell
            blocksInside.add(block);
        }

        if (t8.isPresenceSensorOn(0)) {
            // Create block
            Block block = new Block(Block.Type.Unknown);
            block.path.push(t8, t7);

            // Place block on this conveyor
            t8.placeBlock(block, 0);

            // Add block to cell
            blocksInside.add(block);
        }

        if (t4.isIdle() && t4.hasBlock()) {
            Block block = t4.getOneBlock();

            if (block.isStacked()) {

                // All stacked blocks enter cell and go to pusher t4
                // If pusher is full but contains one such block,
                // divert block to next pusher (t5)
                if (t4.roller.isFull() && !block.path.hasNext()) {
                    if (t5.isIdle() && !t5.hasBlock()) {
                        block.path.push(t5);
                    }
                }
            }

        }
    }

    public void blockPushed(Block block) {
        blocksInside.remove(block);
    }

    @Override
    protected boolean processBlockIn(Block block) {
        block.path.push(t4);

        if (block.isStacked()) {
            /*if (t4.roller.isFull()) {
                block.path.push(t5);
            }*/
        }
        else {
            UnloadOrder order = (UnloadOrder) block.order;

            // Position == 1 or 2
            if (order.position == 2) {
                block.path.push(t5);
            }
        }

        return true;
    }

    @Override
    public List<OrderPossibility> getOrderPossibilities(Set<Order> orders, double arrivalDelayEstimate) {
        return orders
                .stream()
                .filter((o) -> o instanceof UnloadOrder)
                .map((o) -> (UnloadOrder) o)
                .map((order) -> new OrderPossibility(
                        this, order, getPusherForPosition(order.position).roller.isFull() ? 1 : 2, null, 0,
                        order.position == 1
                        ? (blocksIncoming.size() + blocksInside.size() + (t4.roller.isFull() ? 2 : 1) < 3)
                        : (blocksIncoming.size() + blocksInside.size() + (t5.roller.isFull() ? 2 : 1) < 3),
                        order.position
                ))
                .collect(toList());
    }

    private Pusher getPusherForPosition(int position) {
        switch (position) {
            case 1: return t4;
            case 2: return t5;
            default:
                throw new IndexOutOfBoundsException("No pusher at position " + position);
        }
    }

    @Override
    public Conveyor getCornerConveyor(int position) {
        switch (position) {
            case 0: return t1;
            case 1: return t3;
            case 2: return t8;
            case 3: return t6;
            default:
                throw new IndexOutOfBoundsException("Cell " + id + " doesn't have position " + position);
        }
    }

    @Override
    public void connectWithRightCell(Cell right) {
        t3.connections[1] = right.getCornerConveyor(0);
        t8.connections[1] = right.getCornerConveyor(3);
    }

    @Override
    public void connectWithLeftCell(Cell left) {
        t1.connections[0] = left.getCornerConveyor(1);
        t6.connections[0] = left.getCornerConveyor(2);
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
        return t7;
    }

    @Override
    public Conveyor getEntryConveyor() {
        return t2;
    }

    @Override
    public Conveyor getExitConveyor() {
        return t7;
    }

    @Override
    protected Cell processBlockOut(Block block) {
        return Main.factory.warehouse;
    }
}
