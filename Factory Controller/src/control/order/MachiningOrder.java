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
public class MachiningOrder extends Order {
    
    public final Block.Type startType;
    public final Block.Type endType;
    
    public MachiningOrder(int id, int count, Block.Type startType, Block.Type endType) {
        super(id, count);
        this.startType = startType;
        this.endType = endType;
    }
}
