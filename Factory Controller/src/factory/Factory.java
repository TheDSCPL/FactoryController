package factory;

import control.*;
import control.order.*;
import factory.cell.*;
import factory.cell.assemb.*;
import factory.conveyor.*;
import java.util.*;
import main.*;

public class Factory {

    public final Warehouse warehouse;
    public final LoadUnloadBay loadUnloadCell;
    private final List<Cell> cells = new ArrayList<>();

    public Factory() {

        // Create cells
        warehouse = new Warehouse(Main.config.getS("cell.warehouse.id"));
        loadUnloadCell = new LoadUnloadBay(Main.config.getS("cell.loadunload.id"));

        cells.add(warehouse);
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
        if (warehouse.isBlockOutQueueEmpty() && warehouse.isOutConveyorEmpty()) {
            Set<Order> orders = Main.orderc.getPendingOrders();

            OrderPossibility bestOP = cells.stream()
                    .filter(c -> c != warehouse) // Warehouse does not process Orders
                    .map(c -> c.getOrderPossibilities(orders, cellEntryPathFromWarehouse(c).timeEstimate() + warehouse.reactionTime)) // Get all order possibilities from each cell
                    .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll).stream() // Collapse List<List<X>> into List<X>
                    .map(v -> (OrderPossibility) v) // Cast from Object to OrderPossibility
                    .reduce(null, this::compareOrderPossibilities); // Get best OrderPossibility

            if (bestOP != null) {
                System.out.println("Executing possibility: " + bestOP);
                for (int i = 0; i < bestOP.possibleExecutionCount; i++) {
                    List<Block> bl = bestOP.order.execute(bestOP.executionInfo);

                    bl.stream().forEach(b -> b.path = cellEntryPathFromWarehouse(bestOP.cell));
                    warehouse.addBlocksOut(bl);
                    bestOP.cell.addIncomingBlocks(bl);
                }
            }
        }
    }

    // TODO: sorting algorithm
    private OrderPossibility compareOrderPossibilities(OrderPossibility op1, OrderPossibility op2) {
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

        if (value == 1) {
            return op1;
        }
        else {
            return op2;
        }
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
        return blockTransportPath(warehouse, cell);
    }

    public Path cellExitPathToWarehouse(Cell cell) {
        return blockTransportPath(cell, warehouse);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(warehouse).append("\n");
        for (Cell cell : cells) {
            sb.append(cell).append("\n");
        }

        return sb.toString();
    }

}
