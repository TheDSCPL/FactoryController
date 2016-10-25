/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory;

import factory.cell.assemb.Assembler;
import control.order.Order;
import control.*;
import factory.cell.*;
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

        // Decide next order to be executed
        Set<Order> orders = Main.orderc.pendingOrders;
        if (!orders.isEmpty()) {

            Order order = chooseNextOrder(orders);
            Path blockPath = choosePathForOrder(order);
            OrderExecution exec = order.startExecution(blockPath);

            if (/*block starts by exiting warehouse*/false) {
                for (Block b : exec.blocks) {
                    cw.addPendingOutBlock(b);
                }
            }
            else if (/*block starts on pusher (load order)*/false) {

            }
        }
    }

    private Order chooseNextOrder(Set<Order> orders) {
        return orders.iterator().next(); // TODO
    }

    private Path choosePathForOrder(Order order) {
        return new Path(); // TODO
    }

}
