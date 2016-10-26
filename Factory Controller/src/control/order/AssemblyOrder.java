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
public class AssemblyOrder extends Order { // TODO
    
    public final Block.Type bottomType;
    public final Block.Type topType;

    
    public AssemblyOrder(int id, int count, Block.Type bottomType, Block.Type topType) {
        super(id, count);
        this.bottomType = bottomType;
        this.topType = topType;
    }

    @Override
    public void startExecution(Path path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
