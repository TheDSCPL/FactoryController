package control.order;

import control.*;
import java.text.*;
import java.util.*;

public abstract class Order {

    public final int id;
    public final int count;
    public Set<Block> blocks = new HashSet<>();

    private int placedCount = 0;
    private int completedCount = 0;
    private State state = State.Received;

    private final Date dateReceived;
    private Date dateStarted;
    private Date dateFinished;

    public enum State {
        Received("Received"),
        Initiated("Processing"),
        AllPlaced("Processing (all blocks on factory)"),
        Completed("Completed");

        private final String desc;
        private State(String desc) {
            this.desc = desc;
        }
        
        @Override
        public String toString() {
            return desc;
        }
    }

    public boolean isPending() {
        return state == State.Received || state == State.Initiated;
    }

    public Order(int id, int count) {
        this.id = id;
        this.count = count;
        dateReceived = new Date();
    }

    protected void incrementPlacement() {
        if (!isPending()) {
            return;
        }

        if (placedCount == 0) {
            dateStarted = new Date();
        }

        placedCount++;
        if (placedCount == count) {
            state = State.AllPlaced;
        }
        else {
            state = State.Initiated;
        }
    }

    public void complete(Block block) {
        blocks.remove(block);

        placedCount--;
        completedCount++;
        if (completedCount == count) {
            state = State.Completed;
            dateFinished = new Date();
        }
    }

    public Order.State getState() {
        return state;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        DateFormat df = new SimpleDateFormat("HH:mm:ss");

        sb.append(" - ").append("ID: ").append(id).append("\n");
        sb.append(" - ").append("Type: ").append(orderTypeString()).append("\n");
        sb.append(" - ").append("Count: ").append(count).append("\n");
        sb.append(" - ").append("State: ").append(state).append("\n");
        sb.append(" - ").append("Blocks pending: ").append(count - placedCount - completedCount).append("\n"); // TODO: sometimes is a negative value
        sb.append(" - ").append("Blocks processing: ").append(placedCount).append("\n");
        sb.append(" - ").append("Blocks completed: ").append(completedCount).append("\n");        
        sb.append(" - ").append("Date received: ").append(df.format(dateReceived)).append("\n");
        sb.append(" - ").append("Date started: ").append(dateStarted == null ? "not yet started" : df.format(dateStarted)).append("\n");
        sb.append(" - ").append("Date finished: ").append(dateFinished == null ? "not yet finished" : df.format(dateFinished)).append("\n");

        return sb.toString();
    }
    
    public abstract String orderDescription();
    
    public String orderTypeString() {
        return this.getClass().getSimpleName().replaceFirst("Order", "") + "(" + orderDescription() + ")";
    }
}
