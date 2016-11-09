package control.order;

import control.*;

public class AssemblyOrder extends Order {

    public final Block.Type bottomType;
    public final Block.Type topType;

    public AssemblyOrder(int id, int count, Block.Type bottomType, Block.Type topType) {
        super(id, count);
        this.bottomType = bottomType;
        this.topType = topType;
    }

    public Block[] execute(Path blockPath) {
        if (!isPending()) {
            return null;
        }

        Block b1 = new Block(bottomType);
        b1.path = blockPath;
        b1.order = this;

        Block b2 = new Block(topType);
        b2.path = blockPath;
        b2.order = this;

        b1.isBottomBlock = true;
        b1.otherAssemblyBlock = b2;

        b2.isBottomBlock = false;
        b2.otherAssemblyBlock = b1;

        incrementPlacement();
        return new Block[]{b1, b2};
    }

}
