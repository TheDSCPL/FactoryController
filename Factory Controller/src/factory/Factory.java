package factory;

import control.*;
import control.order.*;
import factory.cell.*;
import factory.cell.assemb.*;
import factory.conveyor.*;
import java.util.*;
import main.*;

public class Factory {

    private final Warehouse cw;
    private final Cell[] processingCells;
    
    public Factory() {

        // Create cells
        cw = new Warehouse("A");
        ParallelCell c1 = new ParallelCell("Pa");
        SerialCell c2 = new SerialCell("Sa");
        ParallelCell c3 = new ParallelCell("Pb");
        SerialCell c4 = new SerialCell("Sb");
        Assembler ca = new Assembler("M");
        LoadUnloadBay cb = new LoadUnloadBay("C");

        // Connect cells
        Cell[] allCells = new Cell[]{cw, c1, c2, c3, c4, ca, cb};
        Cell.connect(allCells);

        // Fill lists
        processingCells = new Cell[]{c1, c2, c3, c4, ca, cb};
    }

    public void update() {

        // Update cells
        cw.update();
        for (Cell cell : processingCells) {
            cell.update();
        }

        // Choose next order to be executed
        if (cw.getBlockOutQueueCount() == 0) {
            Set<Order> orders = Main.orderc.getPendingOrders();

            Arrays.asList(processingCells) // Get all cells
                    .stream()
                    .map((c) -> c.getOrderProspects(orders, cellEntryPathFromWarehouse(c).timeEstimate())) // Get all order prospects from each cell
                    .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll) // Collapse List<List<X>> into List<X>
                    .stream()
                    .map((op) -> (OrderProspect) op) // Cast from Object to OrderProspect
                    .sorted(this::orderProspectsSortFunction) // Sort (better prospect becomes first on stream)
                    .findFirst() // Get first prospect
                    .ifPresent( // Execute prospect
                            (op) -> {
                                System.out.println("Executing prospect: " + op);
                                for (int i = 0; i < op.possibleExecutionCount; i++) {
                                    List<Block> bl = op.order.execute(cellEntryPathFromWarehouse(op.cell), op.executionInfo);
                                    cw.addBlocksOut(bl);
                                    op.cell.addIncomingBlocks(bl);
                                }
                            }
                    );
        }
    }

    // TODO: sorting algorithm
    private int orderProspectsSortFunction(OrderProspect op1, OrderProspect op2) {
        int value = 0;

        // Execute each set of rules, sequentially, from most important to least important
        // Stop execution when a rule has decided the order between the prospects (value != 0)
        for (int i = 0; i <= 3; i++) {
            switch (i) {
                case 0: // Leave for last prospects that cannot be processed immediately
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
                        value = indexForCell(op1.cell) - indexForCell(op2.cell);
                    }
                    break;
                case 2: // For the same order, prefer cells that are quicker
                    if (op1.order == op2.order) {
                        value = (int) (op2.processingTime - op1.processingTime);
                    }
                    break;
                case 3: // Prefer prospects that get more done at once
                    value = op1.possibleExecutionCount - op2.possibleExecutionCount;
                    break;
            }

            if (value != 0) {
                break;
            }
        }

        return -value; // Apparently, sorting order needs to be reversed
    }

    private int indexForCell(Cell c) {
        for (int i = 0; i < processingCells.length; i++) {
            if (processingCells[i] == c) {
                return i;
            }
        }

        return -1;
    }

    public Path cellEntryPathFromWarehouse(Cell cell) {
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
            else {
                throw new Error("Invalid conveyor type on top distribution path");
            }

            path.push(c);
        }

        return path;
    }

    public Path cellExitPathToWarehouse(Cell cell) {
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
            else {
                throw new Error("Invalid conveyor type on bottom return path");
            }

            path.push(c);
        }

        return path;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(cw).append("\n");
        for (Cell cell : processingCells) {
            sb.append(cell).append("\n");
        }

        return sb.toString();
    }

}
