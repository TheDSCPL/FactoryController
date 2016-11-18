package factory.cell;

import control.*;
import control.order.UnloadOrder;
import factory.conveyor.*;

public class LoadUnloadBay extends Cell {

    private final Mover t1;
    private final Rotator t2;
    private final Mover t3;
    private final Pusher t4;
    private final Pusher t5;
    private final Mover t6;
    private final Rotator t7;
    private final Mover t8;

    public LoadUnloadBay(String id) {
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
        conveyorList = new Conveyor[]{t1, t2, t3, t4, t5, t6, t7, t8};

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
    public void update() {
        super.update();

        // Detect load orders on pusher t4 -> send block to warehouse in
        if (t3.isPresenceSensorOn(0)) {
            // Create block
            Block block = new Block(Block.Type.Unknown);
            block.path.push(t3, t2, t4, t5, t7);

            // Place block on this conveyor
            t3.placeBlock(block, 0);
        }

        if (t8.isPresenceSensorOn(0)) {
            // Create block
            Block block = new Block(Block.Type.Unknown);
            block.path.push(t8, t7);

            // Place block on this conveyor
            t8.placeBlock(block, 0);
        }
    }

    @Override
    protected boolean processBlockIn(Block block) {
        UnloadOrder order = (UnloadOrder) block.order;

        block.path.push(t4); // Position == 1 or 2
        if (order.position == 2) {
            block.path.push(t5);
        }
        
        return true;
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
    public Rotator getEntryConveyor() {
        return t2;
    }

    @Override
    public Rotator getExitConveyor() {
        return t7;
    }
}
