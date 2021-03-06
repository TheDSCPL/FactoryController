package control.order;

import control.*;
import java.text.*;
import java.util.*;

public abstract class Order {

    public final int id;
    public final int totalOrdersCount;
    private final Set<Block> blocks = new HashSet<>();

    private int placedOrdersCount = 0;
    private int completedOrdersCount = 0;

    private final Date dateReceived;
    private Date dateStarted;
    private Date dateFinished;

    public Order(int id, int count) {
        this.id = id;
        this.totalOrdersCount = count;
        dateReceived = new Date();
    }

    public final boolean canBeExecuted() {
        return placedOrdersCount + completedOrdersCount < totalOrdersCount;
    }

    public final int getPendingCount() {
        return totalOrdersCount - placedOrdersCount - completedOrdersCount;
    }

    public boolean receivedBefore(Order o) {
        return dateReceived.before(o.dateReceived);
    }

    /**
     *
     * @param info Object that contains necessary information about that order.
     * For example, in TransformationOrder, this should be a
     * TransformationSequence object.
     * @return List of blocks that the warehouse is supposed to put on its exit
     * conveyor
     */
    public abstract List<Block> execute(Object info);

    protected final void addBlocksPlaced(List<Block> list) {
        if (!canBeExecuted()) {
            return;
        }

        if (placedOrdersCount == 0) {
            dateStarted = new Date();
        }

        placedOrdersCount++;
        blocks.addAll(list);
    }

    public final void complete(Block block) {
        blocks.remove(block);
        if (block.isStacked()) {
            blocks.remove(block.otherAssemblyBlock);
        }

        placedOrdersCount--;
        completedOrdersCount++;
        if (completedOrdersCount == totalOrdersCount) {
            dateFinished = new Date();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        DateFormat df = new SimpleDateFormat("HH:mm:ss");

        sb.append(" - ").append("ID: ").append(id).append("\n");
        sb.append(" - ").append("Type: ").append(orderTypeString()).append("\n");
        sb.append(" - ").append("Instances count: ").append(totalOrdersCount).append("\n");
        sb.append(" - ").append("State: ").append(getStateString()).append("\n");
        sb.append(" - ").append("Date received: ").append(df.format(dateReceived)).append("\n");
        sb.append(" - ").append("Date started: ").append(dateStarted == null ? "not yet started" : df.format(dateStarted)).append("\n");
        sb.append(" - ").append("Date finished: ").append(dateFinished == null ? "not yet finished" : df.format(dateFinished)).append("\n");
        sb.append(" - ").append("Order instances pending: ").append(getPendingCount()).append("\n");
        sb.append(" - ").append("Order instances processing: ").append(placedOrdersCount).append("\n");

        blocks.stream()/*.filter((b) -> (!b.isBottomBlock))*/.forEach((b) -> {
            sb.append("\t").append(b.type).append(": ").append(b.path).append("\n");
        });

        sb.append(" - ").append("Order instances completed: ").append(completedOrdersCount).append("\n");

        return sb.toString();
    }

    public String getStateString() {
        if (completedOrdersCount == totalOrdersCount) {
            return "Completed";
        }
        if (placedOrdersCount + completedOrdersCount == 0) {
            return "Received";
        }
        return "Processing";
    }

    public String getSmallStateString() {
        if (completedOrdersCount == totalOrdersCount) {
            return "[X]";
        }
        if (placedOrdersCount + completedOrdersCount == 0) {
            return "[ ]";
        }
        return "[.]";
    }

    public abstract String orderDescription();

    public String orderTypeString() {
        return this.getClass().getSimpleName().replaceFirst("Order", "") + "(" + orderDescription() + ")";
    }

    public boolean isCompleted() {
        return completedOrdersCount == totalOrdersCount;
    }
}
