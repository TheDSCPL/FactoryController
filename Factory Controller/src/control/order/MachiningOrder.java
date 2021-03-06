package control.order;

import control.*;
import factory.conveyor.Machine;
import java.util.*;
import main.*;
import transformation.*;

public class MachiningOrder extends Order {

    public final Block.Type startType;
    public final Block.Type endType;

    public MachiningOrder(int id, int count, Block.Type startType, Block.Type endType) {
        super(id, count);
        this.startType = startType;
        this.endType = endType;
    }

    public List<TransformationSequence> possibleSequences(Machine.Type.Set mts) {
        return Main.transfm.getTransformationSequences(startType, endType, mts);
    }

    public List<Block> execute(Object info) {
        if (!(info instanceof TransformationSequence)) {
            throw new Error("Invalid info type passed to Order::execute");
        }

        if (!canBeExecuted()) {
            return new ArrayList<>();
        }

        Block b = new Block(startType);
        b.order = this;
        b.transformations = (TransformationSequence) info;

        List<Block> l = Arrays.asList(b);
        addBlocksPlaced(l);
        return l;
    }

    @Override
    public String orderDescription() {
        return startType + ">" + endType;
    }

}
