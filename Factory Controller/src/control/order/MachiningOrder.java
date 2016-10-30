package control.order;

import control.*;
import java.util.*;
import transformation.*;
import main.*;

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
    
    public Block execute(Path blockPath, TransformationSequence sequence) {
        if (!isPending()) { return null; }
        
        Block b = new Block(startType);
        b.path = blockPath;
        b.order = this;
        b.sequence = sequence;
        
        incrementPlacement();
        return b;
    }
    
}
