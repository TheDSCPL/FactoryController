package factory;

import control.*;
import control.order.*;
import factory.cell.*;
import factory.cell.assemb.*;
import factory.conveyor.*;
import java.util.*;
import main.*;

public class Factory {

    public final Warehouse warehouseCell;
    public final LoadUnloadBay loadUnloadCell;
    private final List<Cell> cells = new ArrayList<>();

    public Factory() {

        // Create cells
        warehouseCell = new Warehouse(Main.config.getS("cell.warehouse.id"));
        loadUnloadCell = new LoadUnloadBay(Main.config.getS("cell.loadunload.id"));
        
        cells.add(warehouseCell);
        for (int i = 1; Main.config.getS("cell." + i + ".type") != null; i++) {
            String type = Main.config.getS("cell." + i + ".type");
            String id = Main.config.getS("cell." + i + ".id");

            Cell cell;
            switch (type) {
                case "serial":
                    cell = new SerialCell(id);
                    break;
                case "parallel":
                    cell = new ParallelCell(id);
                    break;
                case "assembly":
                    cell = new Assembler(id);
                    break;
                default: throw new Error("Invalid machine type in config file");
            }

            cells.add(cell);
        }
        cells.add(loadUnloadCell);
        
        // Connect cells
        Cell.connect(cells);
    }

    public void update() {
        
        // Update cells
        cells.stream().forEach(Cell::update);

        // Choose next order to be executed
        if (warehouseCell.getBlockOutQueueCount() == 0) {
            Set<Order> orders = Main.orderc.getPendingOrders();
            
            cells.stream()
                    .filter((c) -> c != warehouseCell) // Warehouse does not process Orders
                    .map((c) -> c.getOrderPossibilities(orders, cellEntryPathFromWarehouse(c).timeEstimate() + warehouseCell.reactionTime)) // Get all order possibilities from each cell
                    .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll).stream() // Collapse List<List<X>> into List<X>
                    .map((op) -> (OrderPossibility) op) // Cast from Object to OrderPossibility
                    .sorted(this::orderPossibilitiesSortFunction) // Sort (better possibility becomes first on stream)
                    .findFirst() // Get first possibility
                    .ifPresent( // Execute possibility
                            (op) -> {
                                System.out.println("Executing possibility: " + op);
                                for (int i = 0; i < op.possibleExecutionCount; i++) {
                                    List<Block> bl = op.order.execute(cellEntryPathFromWarehouse(op.cell), op.executionInfo);
                                    warehouseCell.addBlocksOut(bl);
                                    op.cell.addIncomingBlocks(bl);
                                }
                            }
                    );
        }
    }

    // TODO: sorting algorithm
    private int orderPossibilitiesSortFunction(OrderPossibility op1, OrderPossibility op2) {
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
                        value = indexForCell(op1.cell) - indexForCell(op2.cell);
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

        return -value; // Apparently, sorting order needs to be reversed
    }

    private int indexForCell(Cell c) {
        for (int i = 0; i < cells.size(); i++) {
            if (cells.get(i) == c) {
                return i;
            }
        }

        return -1;
    }

    public Path blockTransportPath(Cell from, Cell to) {
        if (from == to) {
            return null;
        }

        Path path = new Path();

        // Transport block left to right, meaning using top conveyors
        if (indexForCell(from) < indexForCell(to)) {
            path.push(from.getTopTransferConveyor());

            while (path.getLast() != to.getTopTransferConveyor()) {
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
        }

        // Transport block right to left, meaning using bottom conveyors
        else {
            path.push(from.getBottomTransferConveyor());

            while (path.getLast() != to.getBottomTransferConveyor()) {
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
        }

        return path;
    }

    public Path cellEntryPathFromWarehouse(Cell cell) {
        return blockTransportPath(warehouseCell, cell);
    }

    public Path cellExitPathToWarehouse(Cell cell) {
        return blockTransportPath(cell, warehouseCell);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(warehouseCell).append("\n");
        for (Cell cell : cells) {
            sb.append(cell).append("\n");
        }

        return sb.toString();
    }

}
