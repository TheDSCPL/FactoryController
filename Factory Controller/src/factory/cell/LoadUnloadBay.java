/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.cell;

import factory.conveyor.*;

/**
 *
 * @author Alex
 */
public class LoadUnloadBay extends Cell {

    private final Mover t1;
    private final Rotator t2;
    private final Pusher t4;
    private final Pusher t5;
    private final Mover t6;
    private final Rotator t7;
    
    public LoadUnloadBay(String id) {
        super(id);
        
        // Create conveyors
        t1 = new Mover(id + "T1", 1);
        t2 = new Rotator(id + "T2");
        t4 = new Pusher(id + "T4");
        t5 = new Pusher(id + "T5");
        t6 = new Mover(id + "T6", 1);
        t7 = new Rotator(id + "T7");
        conveyorList = new Conveyor[] {t1, t2, t4, t5, t6, t7};
        
        // Connect conveyors
        t1.connectedConveyors = new Conveyor[] {null, t2};
        t2.connectedConveyors = new Conveyor[] {t1, null, null, t4};
        t4.connectedConveyors = new Conveyor[] {t2, t5};
        t5.connectedConveyors = new Conveyor[] {t4, t7};
        t6.connectedConveyors = new Conveyor[] {null, t7};
        t7.connectedConveyors = new Conveyor[] {t6, t5, null, null};
    }
    
    @Override
    public Conveyor getCornerConveyor(int position) {
        switch (position) {
            case 0: return t1;
            case 1: return null;
            case 2: return null;
            case 3: return t6;
            default: return null;
        }
    }

    @Override
    public void update() {
        for (Conveyor conveyor : conveyorList) {
            conveyor.update();
        }
    }

    @Override
    public void connectWithRightCell(Cell right) {
        //t3.connectedConveyors[1] = right.getCornerConveyor(0);
        //t10.connectedConveyors[1] = right.getCornerConveyor(3);
    }
    
    @Override
    public void connectWithLeftCell(Cell left) {
        t1.connectedConveyors[0] = left.getCornerConveyor(1);
        t6.connectedConveyors[0] = left.getCornerConveyor(2);
    }
    
}
