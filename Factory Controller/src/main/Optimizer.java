package main;

import control.*;
import control.order.*;
import factory.cell.*;
import java.util.*;

public class Optimizer {

    public void distributeNextOrder() {
        if (shouldDistribute()) {
            Set<Order> orders = Main.orderc.getPendingOrders();

            OrderPossibility best = Main.factory.cells.stream()
                    .map(c -> c.getOrderPossibilities(orders, Main.factory.cellEntryPathFromWarehouse(c).timeEstimate() + Main.factory.warehouse.reactionTime)) // Get all order possibilities from each cell
                    .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll).stream() // Collapse List<List<X>> into List<X>
                    .map(v -> (OrderPossibility) v) // Cast from Object to OrderPossibility
                    .reduce(null, this::bestOrderPossibility); // Get best OrderPossibility

            if (best != null) {
                System.out.println("Executing possibility: " + best);
                for (int i = 0; i < best.possibleExecutionCount; i++) {
                    List<Block> bl = best.order.execute(best.executionInfo);

                    bl.stream().forEach(b -> b.path = Main.factory.cellEntryPathFromWarehouse(best.cell));
                    Main.factory.warehouse.addBlocksOut(bl);
                    best.cell.addIncomingBlocks(bl);
                }
            }
        }
    }
    
    public boolean shouldDistribute() {
        return Main.factory.warehouse.isBlockOutQueueEmpty() && Main.factory.warehouse.isOutConveyorEmpty();
    }
    
    private OrderPossibility bestOrderPossibility(OrderPossibility op1, OrderPossibility op2) {
        if (op1 == null) {
            return op2;
        }
        if (op2 == null) {
            return op1;
        }

        int value = 0;

        // Execute each set of rules, sequentially, from most important to least important
        // Stop execution when a rule has decided the order between the possibilities (value != 0)
        for (int i = 0; i <= 3; i++) {
            switch (i) {
                case 0: // Leave for last possibilities that cannot be processed immediately
                    if (!op1.entersCellImmediately) {
                        value = -1;
                    }
                    if (!op2.entersCellImmediately) {
                        value = 1;
                    }
                    break;
                case 1:
                    if (op1.cell == op2.cell) { // Respect priority of orders in the same cell
                        value = op1.priority - op2.priority;
                    }
                    else { // For different cells, prefer to send blocks to cells far away first
                        value = Main.factory.indexForCell(op1.cell) - Main.factory.indexForCell(op2.cell);
                    }
                    break;
                case 2: // For the same order, prefer cells that are quicker
                    if (op1.order == op2.order) {
                        value = (int) (op2.processingTime - op1.processingTime);
                    }
                    break;
                case 3: // Prefer possibilities that get more done at once
                    value = op1.possibleExecutionCount - op2.possibleExecutionCount;
                    break;
            }

            if (value != 0) {
                break;
            }
        }

        if (value == 1) {
            return op1;
        }
        else {
            return op2;
        }
    }

    public static class OrderPossibility {

        public final Cell cell; // "I [this cell],
        public final Order order; // can do this order
        public final int possibleExecutionCount; // this many times at once
        public final Object executionInfo; // in this way
        public final double processingTime; // taking this long per order execution."

        public final boolean entersCellImmediately; // If it is projected that blocks will enter the cell immediately when received
        public final int priority; // Of all my possibilities returned in the list, the one with the highest priority is the one I prefer to do first (and so on for the others with lower priority)

        public OrderPossibility(Cell cell, Order order, int possibleExecutionCount, Object executionInfo, double processingTime, boolean entersCellImmediately, int priority) {
            this.cell = cell;
            this.order = order;
            this.possibleExecutionCount = possibleExecutionCount;
            this.executionInfo = executionInfo;
            this.processingTime = processingTime;
            this.entersCellImmediately = entersCellImmediately;
            this.priority = priority;
        }

        @Override
        public String toString() {
            return cell.id + " " + order.id + " " + possibleExecutionCount + " " + executionInfo + " " + processingTime + " " + entersCellImmediately + " " + priority;
        }

    }

}
