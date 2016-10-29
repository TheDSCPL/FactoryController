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
public class Transformation {
    public final Block.Type start;
    public final Block.Type end;
    public final Machine.Type machine;
    public final Machine.Tool tool;
    
    /**
     * Process duration in milliseconds
     */
    public final double duration;
    
    public Transformation(Block.Type start, Block.Type end, Machine.Type machine, Machine.Tool tool, double duration) {
        this.start = start;
        this.end = end;
        this.machine = machine;
        this.tool = tool;
        this.duration = duration;
    }
}
