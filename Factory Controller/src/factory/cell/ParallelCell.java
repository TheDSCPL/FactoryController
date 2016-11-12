package factory.cell;

import control.*;
import factory.conveyor.*;
import transformation.Transformation;

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
        conveyorList = new Conveyor[]{t1, t2, t3, t4, t5, t6, t7, t8, t9, t10};

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
    }

    protected boolean processBlockIn(Block block) {        
        if (blocksInside.size() >= 3) {
            return false;
        }

        Path path = block.path;

        path.push(t4, t5);
        Conveyor current = t5;

        for (Transformation t : block.sequence.sequence) {
            switch (t.machine) {
                case C:
                    if (current == t5) {
                        path.push(t7, t6);
                    }
                    current = t6;
                    break;
                case B:
                    if (current == t6) {
                        path.push(t4, t5);
                    }
                    current = t5;
                    break;
                default: throw new Error("Invalid machine on sequence of a block");
            }
        }

        if (current == t6) {
            path.push(t4, t5);
        }

        path.push(t7, t9);
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
    public Rotator getEntryConveyor() {
        return t2;
    }

    @Override
    public Conveyor getExitConveyor() {
        return t9;
    }

    @Override
    public long getEntryDelayTimeEstimate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
