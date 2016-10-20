/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory;

import factory.cell.ParallelCell;
import factory.cell.Cell;
import factory.cell.SerialCell;
import factory.cell.Warehouse;

/**
 *
 * @author Alex
 */
public class Factory {
    
    public Factory() {
        
        // Create cells
        Warehouse w1 = new Warehouse("A");
        
        SerialCell c1 = new SerialCell("Sa");
        ParallelCell c2 = new ParallelCell("Pa");
        SerialCell c3 = new SerialCell("Sb");
        
        // Connect cells
        Cell.connect(w1, c1);
        Cell.connect(c1, c2);
        Cell.connect(c2, c3);
        //...
        
    }

}
