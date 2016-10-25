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
public class LoadOrder extends Order { // TODO
        
    public LoadOrder(int id, Block block) {
        super(id, 1);
    }

    @Override
    public OrderExecution startExecution(Path path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
