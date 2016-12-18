package control;

import coms.*;
import control.order.*;
import java.util.*;
import static java.util.stream.Collectors.*;
import main.*;

public class OrderController {

    /**
     * All orders waiting to be executing, currently executing and that have
     * finished executing
     */
    public Set<Order> orders = new HashSet<>();

    private final SocketInput socket;

    public OrderController() {
        socket = new SocketInput(Main.config.getI("socket.port"), 9);
        socket.start();
    }

    private Long firstOrderTime;

    public void update() {
        // Get new orders from socket and process them
        socket.getNewPackets().stream().forEach(this::processNewOrder);

        if (orders.stream().filter(o -> !o.isCompleted()).count() == 0 && firstOrderTime != null) {
            System.out.println("TESTBENCH: TOTAL TIME FOR ORDERS = " + ((double) (Main.time() - firstOrderTime) / 1000.0) + "s");
            firstOrderTime = null;
        }
    }

    public Set<Order> getPendingOrders() {
        return orders.stream().filter(o -> o.canBeExecuted()).collect(toSet());
    }

    private void processNewOrder(byte[] packet) {
        String orderString = new String(packet, 0, packet.length);

        if (orderString.charAt(0) != ':') {
            return;
        }

        try {
            int orderId = Integer.parseInt(orderString.substring(2, 5));
            int value1 = Integer.parseInt(orderString.substring(5, 6));
            int value2 = Integer.parseInt(orderString.substring(6, 7));
            int quantity = Integer.parseInt(orderString.substring(7, 9));

            Order order;
            switch (orderString.charAt(1)) {
                case 'T':
                    order = new MachiningOrder(orderId, quantity, Block.Type.getType(value1), Block.Type.getType(value2));
                    break;
                case 'M':
                    order = new AssemblyOrder(orderId, quantity, Block.Type.getType(value1), Block.Type.getType(value2));
                    break;
                case 'U':
                    order = new UnloadOrder(orderId, quantity, Block.Type.getType(value1), value2);
                    break;
                default:
                    //System.out.println("Invalid order type: '" + orderString.charAt(1) + "'");
                    return;
            }

            if (orders.isEmpty()) {
                firstOrderTime = Main.time();
            }

            orders.add(order);
        }
        catch (NumberFormatException ex) {
            //System.out.println("Invalid order string: '" + orderString + "'");
        }
        //System.out.println("Valid order received: '" + orderString + "'");
    }

    public Order getOrderWithID(int id) {
        return orders.stream().filter(o -> o.id == id).findFirst().orElse(null);
    }

    public String orderInfo(String id) {
        try {
            Order order = getOrderWithID(Integer.parseInt(id));
            if (order != null) {
                return "Info for order O" + id + ":\n" + order.toString();
            }

            return "No order with id " + id;
        }
        catch (NumberFormatException ex) {
            return "Invalid order id '" + id + "'";
        }
    }

    @Override
    public String toString() {
        if (orders.isEmpty()) {
            return "No orders have been received";
        }

        StringBuilder sb = new StringBuilder();

        new ArrayList<>(orders)
                .stream()
                .sorted((o1, o2) -> o1.receivedBefore(o2) ? -1 : 1)
                .forEach((order) -> {
                    sb.append(" ").append(order.getSmallStateString()).append(" ")
                            .append("O").append(order.id).append(" ")
                            .append(order.orderTypeString()).append(" ")
                            .append("\n");
                });

        return sb.toString();
    }
}
