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
public class UnloadOrder extends Order {

    public final int position;
    public final Block.Type blockType;

    public UnloadOrder(int id, int count, Block.Type blockType, int position) {
        super(id, count);
        
        if (position != 1 && position != 2) {
            throw new Error("Invalid position for UnloadOrder: " + position);
        }
        
        this.position = position;
        this.blockType = blockType;
    }

    @Override
    Block[] createBlocksForExecution() {
        return new Block[]{ new Block(blockType) };
    }
    
}
