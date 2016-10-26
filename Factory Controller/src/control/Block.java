/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import control.order.*;

/**
 * Represents a block.
 *
 * @author Luis Paulo
 * @auhor Alex
 */
public class Block {

    public Block(Block.Type type) {
        this.type = type;
    }
    
    public void completeOrder() {
        if (order != null) {
            order.complete(this);
        }
    }

    public enum Type {
        P1(1), P2(2), P3(3), P4(4), P5(5),
        P6(6), P7(7), P8(8), P9(9),
        /**
         * For blocks that have two pieces one on top of the other
         */
        Stacked(-1),
        /**
         * For blocks whose piece type is unknown, for example, when a block is
         * manually placed on the simulator by some person
         */
        Unknown(-2);

        public final int id;

        Type(int id) {
            this.id = id;
        }

        public static Type getType(int id) {
            for (Type t : Type.values()) {
                if (t.id == id) {
                    return t;
                }
            }

            return null;
        }

    }

    public Order order;
    public Type type;
    public Path path = new Path();
}
