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
        
        // Connect conveyors
        t1.connectedConveyors[1] = t2;
        
        t2.connectedConveyors[0] = t1;
        t2.connectedConveyors[3] = t3;
        
        t3.connectedConveyors[0] = t2;
        t3.connectedConveyors[1] = t4;
        
        t4.connectedConveyors[0] = t3;
        t4.connectedConveyors[1] = t5;
        
        t5.connectedConveyors[0] = t4;
        t5.connectedConveyors[1] = t6;
        
        t6.connectedConveyors[0] = t7;
        t6.connectedConveyors[1] = t5;
    }

    @Override
    public Conveyor getCornerConveyor(int position) {
        switch (position) {
            case 0: return t1;
            case 1: return t2;
            case 2: return t6;
            case 3: return t7;
            default: return null;
        }
    }

    @Override
    public void update() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void connectWithRightCell(Cell right) {
        t2.connectedConveyors[2] = right.getCornerConveyor(0);
        t6.connectedConveyors[2] = right.getCornerConveyor(3);
    }

    @Override
    public void connectWithLeftCell(Cell left) {
        t1.connectedConveyors[0] = left.getCornerConveyor(1);
        t7.connectedConveyors[0] = left.getCornerConveyor(2);
    }
    
}
