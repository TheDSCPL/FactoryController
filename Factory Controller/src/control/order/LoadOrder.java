/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.order;

/**
 *
 * @author Alex
 */
public class LoadOrder extends Order {
    
    public final int position;
    
    public LoadOrder(int id, int count, int position) {
        super(id, count);
        this.position = position;
    }
    
}
