/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package factory.cell;

import control.*;
import control.order.Order;
import factory.conveyor.*;
import java.util.*;
import main.*;
import main.Optimizer.OrderPossibility;

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

    public final double reactionTime = Main.config.getD("timing.warehouse.reactionTime");

    public Warehouse(String id) {
        super(id);

        out = new Mover(id + "T1", 1);
        in = new Mover(id + "T2", 1);
        conveyors = new Conveyor[]{in, out};

        blockOutQueue = new LinkedList<>();
        warehouseInID = Main.config.getBaseOutput(id + "T2") + 2;
    }

    public void addBlockOut(Block block) {
        blockOutQueue.add(block);
    }

    public void addBlocksOut(List<Block> blocks) {
        blockOutQueue.addAll(blocks);
    }

    public boolean isBlockOutQueueEmpty() {
        return blockOutQueue.isEmpty();
    }

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
        if (waitingForIn && in.isIdle() && !in.isPresenceSensorOn(0)) {
            Main.modbus.setOutput(warehouseInID, false);
            in.removeBlock(0).completeOrder();
            waitingForIn = false;
        }
        
        // Signal simulator to remove block from warehouse
        if (!waitingForOut && blockOutQueue.size() > 0 && isOutConveyorEmpty()) {
            Main.modbus.setRegister(warehouseOutRegister, blockOutQueue.element().type.id);
            waitingForOut = true;
        }

        // Block has been placed on out conveyor, notify conveyor, remove block from outQueue and reset
        if (waitingForOut && out.isIdle() && out.isPresenceSensorOn(0)) {
            Main.modbus.setRegister(warehouseOutRegister, 0);
            out.placeBlock(blockOutQueue.remove(), 0);
            waitingForOut = false;
        }
    }
    
    public boolean isOutConveyorEmpty() {
        return !waitingForOut && out.isIdle() && !out.hasBlock();
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
    public Conveyor getTopTransferConveyor() {
        return out;
    }

    @Override
    public Conveyor getBottomTransferConveyor() {
        return in;
    }

    @Override
    public Conveyor getEntryConveyor() {
        return null;
    }

    @Override
    public Conveyor getExitConveyor() {
        return null;
    }

    @Override
    protected boolean processBlockIn(Block block)  {
        return false;
    }

    @Override
    protected Cell processBlockOut(Block block) {
        return null;
    }
    
    @Override
    public List<OrderPossibility> getOrderPossibilities(Set<Order> orders, double arrivalDelayEstimate)  {
        return new ArrayList<>();
    }

}
