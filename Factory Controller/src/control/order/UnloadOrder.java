package control.order;

import control.*;
import java.util.*;

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

    public List<Block> execute(Path path, Object info) {
        if (!isPending()) {
            return new ArrayList<>();
        }

        Block b = new Block(blockType);
        b.path = path;
        b.order = this;

        incrementPlacement();
        return Arrays.asList(b);
    }

    @Override
    public String orderDescription() {
        return blockType + "#" + position;
    }

}
