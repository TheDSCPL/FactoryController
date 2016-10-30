package transformation;

import control.*;
import factory.conveyor.*;
import java.util.*;
import static java.util.stream.Collectors.*;
import main.*;

public class TransformationManager {

    private final List<Transformation> transformations = new ArrayList<>();
    private final List<TransformationSequence> sequences = new ArrayList<>();

    public TransformationManager() {
        loadTransformations();
        loadSequences();
    }

    private void loadTransformations() {
        for (int id = 1; Main.config.getS("transformation." + id + ".initial") != null; id++) {
            int initial = Main.config.getI("transformation." + id + ".initial");
            int result = Main.config.getI("transformation." + id + ".final");
            int tool = Main.config.getI("transformation." + id + ".tool");
            int duration = Main.config.getI("transformation." + id + ".duration");

            Machine.Type machineType;
            switch (tool) {
                case 1:
                case 2:
                case 3:
                    machineType = Machine.Type.A;
                    break;
                case 4:
                case 5:
                case 6:
                    machineType = Machine.Type.B;
                    break;
                case 7:
                case 8:
                case 9:
                    machineType = Machine.Type.C;
                    break;
                default: throw new Error("XXX"); // TODO
            }

            Machine.Tool.Type machineTool;
            switch (tool) {
                case 1:
                case 4:
                case 7:
                    machineTool = Machine.Tool.Type.T1;
                    break;
                case 2:
                case 5:
                case 8:
                    machineTool = Machine.Tool.Type.T2;
                    break;
                case 3:
                case 6:
                case 9:
                    machineTool = Machine.Tool.Type.T3;
                    break;
                default: throw new Error("XXX"); // TODO
            }

            Transformation t = new Transformation(
                    Block.Type.getType(initial),
                    Block.Type.getType(result),
                    machineType,
                    machineTool,
                    duration
            );

            transformations.add(t);
        }
    }

    private void loadSequences() {
        for (Block.Type t1 : Block.Type.values()) {
            for (Block.Type t2 : Block.Type.values()) {
                if (t1 != t2) {
                    for (Machine.Type.Set mts : Machine.Type.Set.values()) {
                        List<List<Transformation>> seq = recursiveSequenceBuild(t1, t2, mts, new HashSet<>());
                        
                        if (seq != null) {
                            sequences.addAll(
                                    seq.stream()
                                    .filter(list -> list != null)
                                    .map(list -> new TransformationSequence(t1, t2, mts, list))
                                    .collect(toList())
                            );
                        }
                    }
                }
            }
        }
    }

    private List<List<Transformation>> recursiveSequenceBuild(Block.Type start, Block.Type end, Machine.Type.Set mts, Set<Block.Type> visited) {
        List<List<Transformation>> ret = new ArrayList();

        if (start == end) {
            return ret;
        }

        visited.add(start);

        for (Transformation t : transformations) {
            if (t.start == start && !visited.contains(t.end) && mts.contains(t.machine)) {
                List<List<Transformation>> seq = recursiveSequenceBuild(t.end, end, mts, visited);

                if (seq != null) {
                    if (seq.isEmpty()) {
                        seq.add(new ArrayList<>());
                    }

                    seq.stream().forEach(s -> s.add(0, t));
                    ret.addAll(seq);
                }
            }
        }

        visited.remove(start);

        if (ret.isEmpty()) {
            return null;
        }

        return ret;
    }

    /**
     * Returns all TransformationSequence's possible from block type {@param from} to block type {@param to} using machine set {@param mts}
     * @param from 
     * @param to
     * @param mts Machine type set to filter by. Can be null, in which case returns list for all machine type sets
     * @return 
     */
    public List<TransformationSequence> getTransformationSequences(Block.Type from, Block.Type to, Machine.Type.Set mts) {
        return sequences.stream()
                .filter(t -> t.start == from)
                .filter(t -> t.end == to)
                .filter(t -> (mts != null ? (t.machineSet == mts) : true))
                .collect(toList());
    }
    
    public Block.Type newPieceType(Block.Type old, Transformation transf) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO - not sure if necessary
    }
}
