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
        
        t1 = new Mover( id + "T1" , 1 );    //top right mover
        t2 = new Rotator(id + "T2");        //top rotator
        t3 = new Mover( id + "T3" , 1 );    //... não precisas de estes comentários todos...
        t4 = new Mover( id + "T4" , 2 );
        t5 = new Rotator(id + "T5");
        t6 = new Mover( id + "T6" , 1 );
        
        // Connect conveyors
        //...
    }
    
    @Override
    public Conveyor getCornerConveyor(int position) {
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
