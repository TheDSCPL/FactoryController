/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.order;

import control.*;
import java.util.*;

/**
 *
 * @author Alex
 */
public abstract class Order {

    public final int id;
    public final int count;
    public Set<Block> blocks = new HashSet<>();
    
    private int placedCount = 0;
    private int completedCount = 0;
    private State state = State.Received;

    public enum State {
        Received, Initiated, AllPlaced, Completed // TODO, better names, javadoc
    }
    
    public boolean isPending() {
        return state == State.Received || state == State.Initiated;
    }
    
    public Order(int id, int count) {
        this.id = id;
        this.count = count;
    }
    
    public Block[] execute(Path blockPath) {
        if (state == State.AllPlaced || state == State.Completed) { return null; }
        
        Block[] blockPacket = createBlocksForExecution();
        state = State.Initiated;
        
        for (Block b : blockPacket) {
            b.path = blockPath;
            b.order = this;
            blocks.add(b);
        }
        
        placedCount++;
        if (placedCount == count) {
            state = State.AllPlaced;
        }
        
        return blockPacket;
    }
    
    abstract Block[] createBlocksForExecution();

    public void complete(Block block) {
        blocks.remove(block);
        
        completedCount++;
        if (completedCount == count) {
            state = State.Completed;
        }
    }
    
    public Order.State getState() {
        return state;
    }
}
