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
    }

    @Override
    public Conveyor getCornerConveyor(int position) {
        switch (position) {
            case 1: return out;
            case 2: return in;
            default: return null;
        }
    }

    @Override
    public void update() {
        out.update();
        in.update();
    }
    
    @Override
    public void connectWithRightCell(Cell right) {
        out.connectedConveyors[1] = right.getCornerConveyor(0);
        in.connectedConveyors[1] = right.getCornerConveyor(3);
    }

    @Override
    public void connectWithLeftCell(Cell left) {
        return;
    }
    
}
