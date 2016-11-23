package control.order;

import control.*;
import java.text.*;
import java.util.*;

public abstract class Order {

    public final int id;
    public final int count;
    private Set<Block> blocks = new HashSet<>();

    private int placedCount = 0;
    private int completedCount = 0;

    private final Date dateReceived;
    private Date dateStarted;
    private Date dateFinished;

    public Order(int id, int count) {
        this.id = id;
        this.count = count;
        dateReceived = new Date();
    }

    public final boolean canBeExecuted() {
        return placedCount + completedCount < count;
    }

    public final int getPendingCount() {
        return count - placedCount - completedCount;
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

        if (placedCount == 0) {
            dateStarted = new Date();
        }

        placedCount += list.size();
        blocks.addAll(list);
    }

    public final void complete(Block block) {
        blocks.remove(block);

        placedCount--;
        completedCount++;
        if (completedCount == count) {
            dateFinished = new Date();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        DateFormat df = new SimpleDateFormat("HH:mm:ss");

        sb.append(" - ").append("ID: ").append(id).append("\n");
        sb.append(" - ").append("Type: ").append(orderTypeString()).append("\n");
        sb.append(" - ").append("Count: ").append(count).append("\n");
        sb.append(" - ").append("State: ").append(getStateString()).append("\n");
        sb.append(" - ").append("Date received: ").append(df.format(dateReceived)).append("\n");
        sb.append(" - ").append("Date started: ").append(dateStarted == null ? "not yet started" : df.format(dateStarted)).append("\n");
        sb.append(" - ").append("Date finished: ").append(dateFinished == null ? "not yet finished" : df.format(dateFinished)).append("\n");
        sb.append(" - ").append("Blocks pending: ").append(getPendingCount()).append("\n");
        sb.append(" - ").append("Blocks processing: ").append(placedCount).append("\n");
        
        for (Block b : blocks) {
            sb.append("\t").append(b.type).append(" on ").append(b.path.getCurrent().id).append("\n");
        }

        sb.append(" - ").append("Blocks completed: ").append(completedCount).append("\n");

        return sb.toString();
    }

    public String getStateString() {
        if (completedCount == count) {
            return "Completed";
        }
        if (placedCount + completedCount == 0) {
            return "Received";
        }
        return "Processing";
    }

    public abstract String orderDescription();

    public String orderTypeString() {
        return this.getClass().getSimpleName().replaceFirst("Order", "") + "(" + orderDescription() + ")";
    }

    public boolean isCompleted() {
        return completedCount == count;
    }
}
