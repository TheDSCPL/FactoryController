package control.order;

import control.*;

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

    public Block execute(Path blockPath) {
        if (!isPending()) { return null; }
        
        Block b = new Block(blockType);
        b.path = blockPath;
        b.order = this;
        
        incrementPlacement();
        return b;
    }

    @Override
    public String orderDescription() {
        return blockType + "#" + position;
    }
    
}
