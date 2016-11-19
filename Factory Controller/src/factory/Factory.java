package factory;

import control.*;
import control.order.*;
import factory.cell.*;
import factory.cell.assemb.*;
import factory.conveyor.*;
import java.util.*;
import main.*;
import transformation.*;

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

        // TODO: Do this
        if (cw.getBlockOutQueueCount() == 0) {
            for (Order order : Main.orderc.getPendingOrders()) {

                // DEMO for unload orders
                if (order instanceof UnloadOrder) {
                    cw.addBlockOut(((UnloadOrder) order).execute(entryPathFromWarehouse(cb)));
                }
                
                // DEMO for serial/parallel cells
                else if (order instanceof MachiningOrder) {
                    MachiningOrder mo = (MachiningOrder) order;

                    Optional<TransformationSequence> seqAB;
                    seqAB = mo.possibleSequences().stream().filter(s -> s.machineSet == Machine.Type.Set.AB).findFirst();

                    Optional<TransformationSequence> seqBC;
                    seqBC = mo.possibleSequences().stream().filter(s -> s.machineSet == Machine.Type.Set.BC).findFirst();

                    if (seqAB.isPresent()) {
                        cw.addBlockOut(mo.execute(entryPathFromWarehouse(c2), seqAB.get()));
                    }
                    else if (seqBC.isPresent()) {
                        cw.addBlockOut(mo.execute(entryPathFromWarehouse(c1), seqBC.get()));
                    }
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
            else {
                throw new Error("Invalid conveyor type on top distribution path");
            }

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
        
        for (Cell cell : cellList) {
            sb.append(cell).append("\n");
        }

        return sb.toString();
    }
    
}
