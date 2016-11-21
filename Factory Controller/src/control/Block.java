package control;

import control.order.*;
import factory.conveyor.*;
import java.util.*;
import transformation.*;

/**
 * Represents a block, meaning, one occupied space in a conveyor
 */
public class Block {

    public Block(Block.Type type) {
        this.type = type;
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

    public void completeOrder() {
        if (order != null) {
            order.complete(this);
        }
    }

    @Override
    public String toString() {
        return "Block." + type;
    }
    

    /**
     * For blocks used in MachiningOrder
     */
    public TransformationSequence transformations;
    public boolean hasNextTransformation() {
        return type != transformations.end;
    }
    public Transformation getNextTransformation() {
        return transformations.getNextTransformation(type);
    }
    public Transformation getNextTransformationOnMachine(Machine.Type machineType) {
        List<Transformation> nextTransformations = new ArrayList<>(transformations.sequence);

        while (true) {
            if (nextTransformations.isEmpty()) {
                break;
            }
            if (nextTransformations.get(0).start == type) {
                break;
            }
            
            nextTransformations.remove(0);
        }

        return nextTransformations.stream().filter(s -> s.machine == machineType).findFirst().orElse(null);
    }
    public void applyNextTransformation() {
        type = getNextTransformation().end;
    }

    /**
     * For blocks used in AssemblyOrder
     */
    public Block otherAssemblyBlock;
    public boolean isBottomBlock;
}
