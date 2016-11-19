package transformation;

import control.*;
import factory.conveyor.*;

public class Transformation {
    public final Block.Type start;
    public final Block.Type end;
    public final Machine.Type machine;
    public final Machine.Tool.Type tool;
    
    /**
     * Process duration in milliseconds
     */
    public final long duration;
    
    public Transformation(Block.Type start, Block.Type end, Machine.Type machine, Machine.Tool.Type tool, long duration) {
        this.start = start;
        this.end = end;
        this.machine = machine;
        this.tool = tool;
        this.duration = duration;
    }
    
    public String toString() {
        return start + "->" + end + " (" + tool + "@" + machine + "/" + duration + ")";
    }
}
