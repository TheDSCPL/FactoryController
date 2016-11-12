/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.cell;

import control.*;
import factory.conveyor.*;
import transformation.*;

/**
 *
 * @author Alex
 */
public class SerialCell extends Cell {

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
        conveyorList = new Conveyor[]{t1, t2, t3, t4, t5, t6, t7};

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
                for (int i = 1; i < b.path.path.size(); i++) {
                    Conveyor last = b.path.path.get(i - 1);
                    Conveyor current = b.path.path.get(i);

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
    protected boolean processBlockIn(Block block) {
        if (firstConveyorsBlocked) {
            return false;
        }

        Path path = block.path;

        path.push(t3);
        Conveyor current = t3;

        for (Transformation t : block.sequence.sequence) {
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
                default: throw new Error("Invalid machine on sequence of a block");
            }
        }

        if (current == t3) {
            path.push(t4, t5);
        }

        path.push(t6);
        return true;
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
    public Rotator getEntryConveyor() {
        return t2;
    }

    @Override
    public Rotator getExitConveyor() {
        return t6;
    }

    @Override
    public long getEntryDelayTimeEstimate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
