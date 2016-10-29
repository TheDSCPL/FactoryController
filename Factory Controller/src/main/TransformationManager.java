/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import control.*;
import factory.conveyor.*;
import java.util.*;
import static java.util.stream.Collectors.*;

/**
 *
 * @author Alex
 */
public class TransformationManager {

    private final List<Transformation> transformations = new ArrayList<>();
    //private final 

    public TransformationManager() {
        loadTransformations();
        //loadSequences();
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

            Machine.Tool machineTool;
            switch (tool) {
                case 1:
                case 4:
                case 7:
                    machineTool = Machine.Tool.T1;
                    break;
                case 2:
                case 5:
                case 8:
                    machineTool = Machine.Tool.T2;
                    break;
                case 3:
                case 6:
                case 9:
                    machineTool = Machine.Tool.T3;
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
    
    /*private void loadSequences() {
        List<Set<Machine.Type>> machineSets = getValidMachineSets();
        
        for (Block.Type t1 : Block.Type.values()) {            
            for (Block.Type t2 : Block.Type.values()) {
                if (t1 == t2) continue;
                
                for (Set<Machine.Type> set : machineSets) {
                    getSeq(t1, t2, set);
                }
                
                
            }
        }
    }
    
    private Map<Set<Machine.Type>, List<Transformation>> getSeq(Block.Type start, Block.Type end, Set<Block.Type> visited, Set<Machine.Type> mtSet) {
        if (start == end) {
            Map<Set<Machine.Type>, List<Transformation>> ret;
            
            ret = new HashMap();
            
            for ()
            
            return ret;
            
        }
        
        visited.add(start);
        
        List<Map<Set<Machine.Type>, List<Transformation>>> result;
        
        result = transformations.stream()
                .filter(t -> t.start == start)
                .filter(t -> !visited.contains(t.end))
                .map(t -> getSeq(t.end, end, visited, mtSet))
                .reduce(null, (v1, v2) -> v1.merge(mtSet, transformations, remappingFunction))
                .collect(toList());
        
        visited.remove(start);
        
        return null;
    }*/
    
    private List<Set<Machine.Type>> getValidMachineSets() {
        Set<Machine.Type> machineSetAB = new HashSet<>();        
        machineSetAB.add(Machine.Type.A);
        machineSetAB.add(Machine.Type.B);
        
        Set<Machine.Type> machineSetBC = new HashSet<>();
        machineSetBC.add(Machine.Type.B);
        machineSetBC.add(Machine.Type.C);
        
        List<Set<Machine.Type>> list = new ArrayList<>();
        list.add(machineSetAB);
        list.add(machineSetBC);

        return list;
    }

    public Transformation[][] getTransformationSequences(Block.Type from, Block.Type to) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO
        // Note: this result should be calculated once for each "from/to" pair and then cached, for a speed improvement
    }

    public Block.Type newPieceType(Block.Type old, Transformation transf) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO
    }
}
