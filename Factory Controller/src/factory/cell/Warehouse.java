/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.cell;

import control.*;
import factory.conveyor.*;
import main.*;

/**
 *
 * @author Alex
 */
public class Warehouse extends Cell {

    private final Mover out;
    private final Mover in;

    public Warehouse(String id) {
        super(id);

        // TODO: decide if the warehouse needs to be initialized
        //  >> let's not worry about initialization for now
        //state = State.
        out = new Mover(id + "T1", 1);
        in = new Mover(id + "T2", 1);
        conveyorList = new Conveyor[]{in, out};
    }

    int p = 1; // DEMO

    @Override
    public void update() {
        super.update();

        // DEMO
        Main.modbus.setRegister(0, out.presenceSensors[0].on() ? 0 : p);

        if (out.presenceSensors[0].on() && !out.hasBlock()) {
            Block b = new Block(Block.Type.P1);

            int[] n = {1, 1, 3, 2, 1, 3, 2, 1, 1, 1, 0, 0, 0, 2, 1, 2, 1, 1, 2, 1, 3, 1, 1, 2, 1, 1, 0, 0, 2};
            Conveyor last = out;

            b.path.push(last);
            for (int i : n) {
                b.path.push(last.connections[i]);
                last = last.connections[i];
            }

            out.placeBlock(b, 0);

            p++;
            if (p == 10) {
                p = 1;
            }
        }
    }

    public void addPendingOutBlock(Block block) {

    }

    @Override
    public Conveyor getCornerConveyor(int position) {
        switch (position) {
            case 1: return out;
            case 2: return in;
            default:
                throw new IndexOutOfBoundsException("Cell " + id + " doesn't have position " + position);
        }
    }

    @Override
    public void connectWithRightCell(Cell right) {
        out.connections[1] = right.getCornerConveyor(0);
        in.connections[1] = right.getCornerConveyor(3);
    }

    @Override
    public void connectWithLeftCell(Cell left) {
    }

    @Override
    public Rotator getEntryConveyor() {
        return null; // Does not apply to Warehouses
    }
}
