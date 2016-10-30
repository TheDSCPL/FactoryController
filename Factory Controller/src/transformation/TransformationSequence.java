package transformation;

import control.*;
import factory.conveyor.*;
import java.util.*;

public class TransformationSequence {
    
    public final Block.Type start;
    public final Block.Type end;
    public final Machine.Type.Set machineSet;
    public final List<Transformation> sequence;
    
    public TransformationSequence(Block.Type start, Block.Type end, Machine.Type.Set machineSet, List<Transformation> sequence) {
        this.start = start;
        this.end = end;
        this.machineSet = machineSet;
        this.sequence = Collections.unmodifiableList(sequence);
    }
    
    public double totalDuration() {
        return sequence.stream().map(t -> t.duration).reduce(0.0, Double::sum);
    }
    
    public String toString() {
        return start + "-" + machineSet + "->" + end + ": " + sequence;
    }
}
