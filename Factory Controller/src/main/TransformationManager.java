/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import control.*;
import factory.conveyor.*;

/**
 *
 * @author Alex
 */
public class TransformationManager {
    
    public final Transformation[] transformations;
    
    public TransformationManager() {
        transformations = new Transformation[] {
            new Transformation(Block.Type.P1, Block.Type.P2, Machine.Type.B, Machine.Tool.T1, 1000),
            new Transformation(Block.Type.P1, Block.Type.P2, Machine.Type.B, Machine.Tool.T1, 1000)
            // TODO
        };
    }
    
    public Transformation[][] getTransformationSequences(Block.Type from, Block.Type to) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO
        // Note: this result should be calculated once for each "from/to" pair and then cached, for a speed improvement
    }
    
    public Block.Type newPieceType(Block.Type old, Transformation transf) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO
    }
}
