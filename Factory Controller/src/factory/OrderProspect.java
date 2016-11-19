package factory;

import control.order.*;
import factory.cell.*;

public class OrderProspect {

    public final Cell cell; // "I [this cell],
    public final Order order; // can do this order
    public final int possibleExecutionCount; // this many times at once
    public final Object executionInfo; // in this way
    public final long processingTime; // taking this long per order execution."
    
    public final boolean entersCellImmediately; // If it is projected that blocks will enter the cell immediately when received
    public final int priority; // Of all my prospects returned in the list, the one with the highest priority is the one I prefer to do first (and so on for the others with lower priority)

    public OrderProspect(Cell cell, Order order, int possibleExecutionCount, Object executionInfo, long processingTime, boolean entersCellImmediately, int priority) {
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
