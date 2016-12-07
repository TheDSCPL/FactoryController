package factory;

import control.*;
import factory.cell.*;
import factory.cell.assemb.*;
import factory.conveyor.*;
import java.util.*;
import main.*;

public class Factory {

    public final Warehouse warehouse;
    public final LoadUnloadCell loadUnloadCell;
    public final List<Cell> cells = new ArrayList<>();

    public Factory() {

        // Create cells
        warehouse = new Warehouse(Main.config.getS("cell.warehouse.id"));
        loadUnloadCell = new LoadUnloadCell(Main.config.getS("cell.loadunload.id"));

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
        cells.stream()/*.filter(c -> !(c instanceof Assembler))*/.forEach(Cell::update);
    }

    public int indexForCell(Cell c) {
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
