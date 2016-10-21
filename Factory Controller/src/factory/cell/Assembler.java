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
public class Assembler extends Cell {

    private final Mover t1;
    private final Rotator t2;
    private final Mover t3;
    private final Mover t4;
    private final Rotator t5;
    private final Mover t6;
    
    public Assembler(String id) {
        super(id);
        
        t1 = new Mover( id + "T1" , 1 );    //top left mover
        t2 = new Rotator(id + "T2");        //top rotator
        t3 = new Mover( id + "T3" , 2 );    //...
        t4 = new Mover( id + "T4" , 1 );
        t5 = new Rotator(id + "T5");
        t6 = new Mover( id + "T6" , 1 );
        
        conveyorList = new Conveyor[] {t1, t2, t3, t4, t5, t6};
        
        t1.connectedConveyors = new Conveyor[] {null,t2};
        t2.connectedConveyors = new Conveyor[] {t1,null,null,t3};
        t3.connectedConveyors = new Conveyor[] {t2,t4};
        t4.connectedConveyors = new Conveyor[] {t3,t5};
        t5.connectedConveyors = new Conveyor[] {t6,t4,null,null};
        t6.connectedConveyors = new Conveyor[] {null,t5};
        
    }
    
    @Override
    public Conveyor getCornerConveyor(int position) {
        switch (position) {
            case 0: return t1;
            case 1: return t2;
            case 2: return t5;
            case 3: return t6;
            default: throw new IndexOutOfBoundsException("Cell " + id + " doesn't have position " + position);
        }
    }

    @Override
    public void update()
    {
        switch(state)
        {
            case Initializing:
                
            break;
            case Working:
                
            break;
            default:
                throw new IllegalStateException("Cell " + id + " reached invalid state!");
        }
    }

    @Override
    public void connectWithRightCell(Cell right)
    {
        t2.connectedConveyors[2] = right.getCornerConveyor(0);
        t5.connectedConveyors[2] = right.getCornerConveyor(3);
    }

    @Override
    public void connectWithLeftCell(Cell left)
    {
        t1.connectedConveyors[0] = left.getCornerConveyor(1);
        t6.connectedConveyors[0] = left.getCornerConveyor(2);
    }
    
}
