/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory;

import factory.cell.assemb.*;
import control.order.*;
import control.*;
import factory.cell.*;
import factory.conveyor.*;
import main.*;
import java.util.*;

/**
 *
 * @author Alex
 */
public class Factory {

    private final Warehouse cw;
    private final ParallelCell c1, c3;
    private final SerialCell c2, c4;
    private final Assembler ca;
    private final LoadUnloadBay cb;
    private final Cell[] cellList;

    public Factory() {

        // Create cells
        cw = new Warehouse("A");
        c1 = new ParallelCell("Pa");
        c2 = new SerialCell("Sa");
        c3 = new ParallelCell("Pb");
        c4 = new SerialCell("Sb");
        ca = new Assembler("M");
        cb = new LoadUnloadBay("C");

        // Connect cells
        cellList = new Cell[]{cw, c1, c2, c3, c4, ca, cb};
        Cell.connect(cellList);
    }
    
    public void update() {

        // Update cells
        for (Cell cell : cellList) {
            cell.update();
        }
        
        // DEMO for unload orders
        if (cw.getBlockOutQueueCount() == 0) {
            for (Order order : Main.orderc.getPendingOrders()) {
                if (order instanceof UnloadOrder) {
                    cw.addBlocksOut(order.execute(entryPathFromWarehouse(cb)));
                }
            }
        }
    }
    
    public Path entryPathFromWarehouse(Cell cell) {
        Path path = new Path();
        
        path.push(cw.getExitConveyor());
        
        while (path.getLast() != cell.getEntryConveyor()) {
            Conveyor c;
            
            // Get conveyor on the right
            if (path.getLast() instanceof Mover) {
                c = path.getLast().connections[1];
            }
            else if (path.getLast() instanceof Rotator) {
                c = path.getLast().connections[2];
            }
            else throw new Error("Invalid conveyor type on top distribution path");
            
            path.push(c);
        }
        
        return path;
    }
    
    public Path exitPathToWarehouse(Cell cell) {
        Path path = new Path();
        
        path.push(cell.getExitConveyor());
        
        while (path.getLast() != cw.getEntryConveyor()) {
            Conveyor c;
            
            // Get conveyor on the right
            if (path.getLast() instanceof Mover) {
                c = path.getLast().connections[0];
            }
            else if (path.getLast() instanceof Rotator) {
                c = path.getLast().connections[0];
            }
            else throw new Error("Invalid conveyor type on bottom return path");
            
            path.push(c);
        }
        
        return path;
    }
    
    private Order chooseNextOrder(Set<Order> orders) {
        return orders.iterator().next(); // TODO
    }

    private Path choosePathForOrder(Order order) {
        return new Path(); // TODO
    }

}
