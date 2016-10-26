/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.order;

import control.*;

/**
 *
 * @author Alex
 */
public class MachiningOrder extends Order { // TODO
    
    public final Block.Type startType;
    public final Block.Type endType;
    
    public MachiningOrder(int id, int count, Block.Type startType, Block.Type endType) {
        super(id, count); 
        this.startType = startType;
        this.endType = endType;
    }

    @Override
    public void startExecution(Path path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
