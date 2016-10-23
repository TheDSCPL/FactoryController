/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.order;

import control.*;

/**
 *
 * @author Alex
 */
public abstract class Order {
    // TODO: write class
    
    public final int id;
    public final int count;
    public OrderExecution[] executions;
    
    public Order(int id, int count) {
        this.id = id;
        this.count = count;
    }
    
    //public abstract void execute();
}
