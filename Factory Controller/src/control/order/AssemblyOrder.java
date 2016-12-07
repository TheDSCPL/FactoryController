package control.order;

import control.*;
import java.util.*;

public class AssemblyOrder extends Order {

    public final Block.Type bottomType;
    public final Block.Type topType;

    public AssemblyOrder(int id, int count, Block.Type bottomType, Block.Type topType) {
        super(id, count);
        this.bottomType = bottomType;
        this.topType = topType;
    }

    @Override
    public List<Block> execute(Object info) {
        if(!canBeExecuted())
            return new ArrayList<>();
        
        Block[] blocksToAdd = new Block[] {new Block(bottomType, this),new Block(topType, this)};
        
        addBlocksPlaced(Arrays.asList(blocksToAdd));
        return Arrays.asList(blocksToAdd);
    }

    @Override
    public String orderDescription() {
        return bottomType + "\\" + topType;
    }
}
