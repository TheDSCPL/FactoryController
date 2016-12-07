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

    public Transformation getFirstTransformation() {
        return sequence.stream().findFirst().orElse(null);
    }

    public Transformation getNextTransformation(Block.Type block) {
        return sequence.stream().filter(s -> s.start == block).findFirst().orElse(null);
    }
    
    public boolean containsMachineType(Machine.Type type) {
        return sequence.stream().filter(s -> s.machine == type).findFirst().isPresent();
    }

    public double totalDuration() {
        return (double) sequence.stream().map(t -> t.duration).reduce((long) 0.0, Long::sum);
    }

    public String toString() {
        return start + "-" + machineSet + "->" + end + ": " + sequence;
    }
}
