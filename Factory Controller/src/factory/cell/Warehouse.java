/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.cell;

import factory.conveyor.Conveyor;
import factory.conveyor.Mover;

/**
 *
 * @author Alex
 */
public class Warehouse extends Cell {
    
    private final Mover out;
    private final Mover in;
    
    public Warehouse(String id) {
        super(id);
        
        //TODO: decide if the warehouse needs to be initialized
        //state = State.
        
        out = new Mover(id + "T1", 1);
        in = new Mover(id + "T2", 1);
        conveyorList = new Conveyor[] {in, out};
    }

    @Override
    public Conveyor getCornerConveyor(int position) {
        switch (position) {
            case 1: return out;
            case 2: return in;
            default: throw new IndexOutOfBoundsException("Cell " + id + " doesn't have position " + position);
        }
    }

    @Override
    public void update() {
        super.update();
    }
    
    @Override
    public void connectWithRightCell(Cell right) {
        out.connections[1] = right.getCornerConveyor(0);
        in.connections[1] = right.getCornerConveyor(3);
    }

    @Override
    public void connectWithLeftCell(Cell left) {
        return;
    }
    
}
