/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.cell;

import control.*;
import factory.conveyor.*;
import java.util.*;
import main.*;

/**
 *
 * @author Alex
 */
public class Warehouse extends Cell {

    private final Mover out;
    private final Mover in;
    private final int warehouseInID;
    private final int warehouseOutRegister = 0;
    private final Queue<Block> blockOutQueue;
    private boolean waitingForOut = false;
    private boolean waitingForIn = false;
    

    public Warehouse(String id) {
        super(id);
        
        out = new Mover(id + "T1", 1);
        in = new Mover(id + "T2", 1);
        conveyorList = new Conveyor[]{in, out};
        
        blockOutQueue = new LinkedList<>();
        warehouseInID = Main.config.getBaseOutput(id + "T2") + 2;
    }
    
    public void addBlockOut(Block block) {
        blockOutQueue.add(block);
    }
    
    public void addBlocksOut(Block[] blocks) {
        blockOutQueue.addAll(Arrays.asList(blocks));
    }
    
    public int getBlockOutQueueCount() {
        return blockOutQueue.size();
    }

    //int p = 1; // DEMO

    @Override
    public void update() {
        super.update();

        // Signal simulator to remove block in entry conveyor
        if (in.isIdle() && in.hasBlock()) {
            Main.modbus.setOutput(warehouseInID, true);
            waitingForIn = true;
        }
        
        // Block in entry conveyor has disappeared in simulator,
        // remove block from conveyor and reset
        if (waitingForIn && in.isIdle() && !in.presenceSensors[0].on()) {
            Main.modbus.setOutput(warehouseInID, false);
            in.getBlock(0).completeOrder();
            in.removeBlock(0);
            waitingForIn = false;
        }
        
        // Signal simulator to remove block from warehouse
        if (!waitingForOut && blockOutQueue.size() > 0 && out.isIdle() && !out.hasBlock()) {
            Main.modbus.setRegister(warehouseOutRegister, blockOutQueue.element().type.id);
            waitingForOut = true;
        }
        
        // Block has been placed on out conveyor, notify conveyor, remove block from outQueue and reset
        if (waitingForOut && out.isIdle() && out.presenceSensors[0].on()) {
            Main.modbus.setRegister(warehouseOutRegister, 0);
            out.placeBlock(blockOutQueue.remove(), 0);
            waitingForOut = false;
        }
        
        // DEMO
        /*Main.modbus.setRegister(0, out.presenceSensors[0].on() ? 0 : p);

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
        }*/
        
        
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
    public Conveyor getEntryConveyor() {
        return in;
    }
    
    @Override
    public Conveyor getExitConveyor() {
        return out;
    }
}
