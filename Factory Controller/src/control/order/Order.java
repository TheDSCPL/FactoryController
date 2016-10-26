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
public abstract class Order { // TODO    
    public final int id;
    public final int count;
    public Set<Block> blocks = new HashSet<>();
    private int completedCount = 0;
    
    public Order(int id, int count) {
        this.id = id;
        this.count = count;
    }
    
    public abstract void startExecution(Path blockPath);
    
    public void complete(Block block) {
        blocks.remove(block);
        completedCount++;
    }
}
