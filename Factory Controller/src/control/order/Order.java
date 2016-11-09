package control.order;

import control.*;
import java.util.*;

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
    
    protected void incrementPlacement() {
        if (!isPending()) { return; }
        
        placedCount++;
        if (placedCount == count) {
            state = State.AllPlaced;
        }
        else {
            state = State.Initiated;
        }
    }

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
