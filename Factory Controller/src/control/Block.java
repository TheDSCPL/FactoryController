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
        this(type,(Order) null);
    }
    
    public Block(Block.Type type, Order order) {
        this.type = type;
        this.order = order;
    }
    public enum Type {
        P1(1), P2(2), P3(3), P4(4), P5(5),
        P6(6), P7(7), P8(8), P9(9),
        /**
         * For blocks whose piece type is unknown, for example, when a block is
         * manually placed on the simulator by some person
         */
        Unknown(-1);

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

    public Path timeTravel(double advance) {
        Path newPath = path.copy();
        double timePosition = 0;
        Type currentBlockType = type;

        while (timePosition < advance && newPath.hasNext()) {

            if (newPath.getCurrent() instanceof Machine) {
                Transformation t = transformations.getNextTransformation(currentBlockType);
                if (t != null) {
                    currentBlockType = t.end;
                    timePosition += t.duration;
                }
            }

            timePosition += Conveyor.transferTimeEstimate(newPath.getCurrent(), newPath.getNext());
            newPath.advance();
        }

        return newPath;
    }
    
    @Override
    public String toString() {
        return super.toString() + "[" + type + "]: " + path;
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

    //Methods and fields for blocks meant to be assembled
    public Block otherAssemblyBlock;
    public boolean isBottomBlock = false;

    public boolean canBeBottomBlock()
    {
        if(!(order instanceof AssemblyOrder))
            return false;
        AssemblyOrder o = (AssemblyOrder) this.order;
        return o.bottomType == type;
    }
    
    public boolean canBeTopBlock()
    {
        if(!(order instanceof AssemblyOrder))
            return false;
        AssemblyOrder o = (AssemblyOrder) this.order;
        return o.topType == type;
    }
    
    public boolean isStacked()
    {
        return otherAssemblyBlock != null;
    }
    
    /**
     * Places a block on top of this block if there was none already on top of it
     * @param b block to place on top of this one
     * @return wethwer this block was altered or not
     */
    public boolean placeOnTop(Block b)
    {
        if(b == null || this.isStacked())
            return false;
        this.isBottomBlock = true;
        this.otherAssemblyBlock = b;
        b.isBottomBlock = false;
        b.otherAssemblyBlock = this;
        return true;
    }
}
