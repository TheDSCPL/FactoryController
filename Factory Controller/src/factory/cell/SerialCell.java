/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.cell;

import control.*;
import factory.conveyor.*;

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
        conveyorList = new Conveyor[] {t1, t2, t3, t4, t5, t6, t7};
        
        // Connect conveyors
        t1.connections = new Conveyor[] {null, t2};
        t2.connections = new Conveyor[] {t1, null, null, t3};
        t3.connections = new Conveyor[] {t2, t4};
        t4.connections = new Conveyor[] {t3, t5};
        t5.connections = new Conveyor[] {t4, t6};
        t6.connections = new Conveyor[] {t7, t5, null, null};
        t7.connections = new Conveyor[] {null, t6};
    }

    @Override
    public Conveyor getCornerConveyor(int position) {
        switch (position) {
            case 0: return t1;
            case 1: return t2;
            case 2: return t6;
            case 3: return t7;
            default: throw new IndexOutOfBoundsException("Cell " + id + " doesn't have position " + position);
        }
    }

    @Override
    public void update() {
        super.update();
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
    
}
