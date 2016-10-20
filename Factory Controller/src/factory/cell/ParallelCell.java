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
public class ParallelCell extends Cell {

    private final Mover t1;
    //...
        
    public ParallelCell(String id) {
        super(id);
        
        // Create conveyors
        t1 = new Mover(id + "T1", 1);
        //...
        
        // Connect conveyors
        //...
        
    }
    
    @Override
    public Conveyor cornerConveyor(int position) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
