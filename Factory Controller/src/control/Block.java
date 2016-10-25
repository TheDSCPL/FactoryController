/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

/**
 * Represents a block.
 * @author Luis Paulo
 * @auhor Alex
 */
public class Block {
    
    public Block(Block.Type type) {
        this.type = type;
    }
    
    public enum Type {
        P1, P2, P3, P4, P5,
        P6, P7, P8, P9,
        /**
         * For blocks that have two pieces one on top of the other
         */
        Stacked,
        /**
         * For blocks whose piece type is unknown, for example, when a block is
         * manually placed on the simulator by some person
         */
        Unknown;
    }
    
    public Type type;
    public Path path = new Path();
}
