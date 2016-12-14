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
                    .filter(c -> !(c instanceof Warehouse)) // cellEntryPathFromWarehouse does not work on warehouses
                    .map(c -> c.getOrderPossibilities(orders, Main.factory.cellEntryPathFromWarehouse(c).timeEstimate() + Main.factory.warehouse.reactionTime)) // Get all order possibilities from each cell
                    .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll).stream() // Collapse List<List<X>> into List<X>
                    .map(v -> (OrderPossibility) v) // Cast from Object to OrderPossibility
                    .reduce(null, this::bestOrderPossibility); // Get best OrderPossibility

            if (best != null) {
                if (best.entersCellImmediately) {
                    System.out.print("\b\bExecuting possibility: " + best + System.lineSeparator() + "> ");

                    for (int i = 0; i < best.possibleExecutionCount; i++) {
                        List<Block> bl = best.order.execute(best.executionInfo);

                        bl.stream().forEach(b -> b.path = Main.factory.cellEntryPathFromWarehouse(best.cell));
                        Main.factory.warehouse.addBlocksOut(bl);
                        best.cell.addIncomingBlocks(bl);
                    }

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
        for (int i = 0; i <= 4; i++) {
            switch (i) {
                case 0: // Leave for last possibilities that cannot be processed immediately
                    if (!op1.entersCellImmediately || op1.possibleExecutionCount == 0) {
                        value = -1;
                    }
                    if (!op2.entersCellImmediately || op2.possibleExecutionCount == 0) {
                        value = 1;
                    }
                    break;
                case 1:
                    if (op1.order != op2.order) {
                        if (op1.order.receivedBefore(op2.order)) {
                            value = 1;
                        }
                        else {
                            value = -1;
                        }
                    }
                    break;
                case 2:
                    if (op1.cell == op2.cell) { // For same cell, respect order priority
                        value = op1.priority - op2.priority;
                    }
                    break;
                case 3: // Prefer cell & order combination that have are quicker from exiting the warehouse to entering it again
                    double travel1 = Main.factory.cellEntryPathFromWarehouse(op1.cell).timeEstimate() * 2;
                    double travel2 = Main.factory.cellEntryPathFromWarehouse(op2.cell).timeEstimate() * 2;
                    
                    if (op1.order == op2.order) {
                        value = (int) ((op2.processingTime + travel2) - (op1.processingTime + travel1));
                    }
                    else { // This else is not really needed given condition 1 (different orders)
                        value = (int) (travel2 - travel1);
                    }

                    break;
                case 4: // Prefer possibilities that get more done at once
                    value = op1.possibleExecutionCount - op2.possibleExecutionCount;
                    break;
            }

            if (value != 0) {
                break;
            }
        }

        if (value > 0) {
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
            return "Cell: " + cell.id + " | Order: " + order.id + " | Exec count: " + possibleExecutionCount + " | Exec info: " + executionInfo + " | Estimate processing time: " + processingTime + " | Per-cell priority: " + priority;
        }

    }

}
