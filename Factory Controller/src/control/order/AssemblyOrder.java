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
        // Vê o código da MachiningOrder antes de escreveres isto
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String orderDescription() {
        return bottomType + "\\" + topType;
    }
}
