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
        this.id = id;
        
        out = new Mover(id + "T1", 1);
        in = new Mover(id + "T2", 1);
    }

    @Override
    public Conveyor cornerConveyor(int position) {
        switch (position) {
            case 1: return out;
            case 2: return in;
            default: return null;
        }
    }

    @Override
    public void update() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void connectWithRightCell(Cell right) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void connectWithLeftCell(Cell left) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
