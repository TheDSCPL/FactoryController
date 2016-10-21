/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.cell.assemb;

import factory.*;
import factory.cell.*;
import factory.conveyor.*;
import main.*;

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
    
    private final Sensor table1;
    private final Sensor table2;
    private final Sensor table3;
    
    public Assembler(String id) {
        super(id);
        
        t1 = new Mover(id + "T1", 1);
        t2 = new Rotator(id + "T2");
        t3 = new Mover(id + "T3", 2);
        t4 = new Mover(id + "T4", 1);
        t5 = new Rotator(id + "T6");
        t6 = new Mover(id + "T5", 1);
        conveyorList = new Conveyor[] {t1, t2, t3, t4, t5, t6};
        
        t1.connections = new Conveyor[] {null, t2};
        t2.connections = new Conveyor[] {t1, null, null, t3};
        t3.connections = new Conveyor[] {t2, t4};
        t4.connections = new Conveyor[] {t3, t5};
        t5.connections = new Conveyor[] {t6, t4, null, null};
        t6.connections = new Conveyor[] {null, t5};
        
        table1 = new Sensor(Main.config.getBaseInput(id + "M") + 0);
        table2 = new Sensor(Main.config.getBaseInput(id + "M") + 1);
        table3 = new Sensor(Main.config.getBaseInput(id + "M") + 2);
    }
    
    @Override
    public Conveyor getCornerConveyor(int position) {
        switch (position) {
            case 0: return t1;
            case 1: return t2;
            case 2: return t6;
            case 3: return t5;
            default: throw new IndexOutOfBoundsException("Cell " + id + " doesn't have position " + position);
        }
    }

    @Override
    public void update()
    {
        super.update();
        
        // TODO: review: is this necessary?
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
        t2.connections[2] = right.getCornerConveyor(0);
        t5.connections[2] = right.getCornerConveyor(3);
    }

    @Override
    public void connectWithLeftCell(Cell left)
    {
        t1.connections[0] = left.getCornerConveyor(1);
        t6.connections[0] = left.getCornerConveyor(2);
    }

}
