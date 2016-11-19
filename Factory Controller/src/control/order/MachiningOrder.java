package control.order;

import control.*;
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

    public List<TransformationSequence> possibleSequences() {
        return Main.transfm.getTransformationSequences(startType, endType, null);
    }

    public List<Block> execute(Path path, Object info) {
        if (!(info instanceof TransformationSequence)) {
            throw new Error("Invalid info type passed to Order::execute");
        }

        if (!isPending()) {
            return new ArrayList<>();
        }

        Block b = new Block(startType);
        b.path = path;
        b.order = this;
        b.transformations = (TransformationSequence) info;

        incrementPlacement();
        return Arrays.asList(b);
    }

    @Override
    public String orderDescription() {
        return startType + ">" + endType;
    }

}
