/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import coms.*;
import control.order.*;
import java.util.*;
import static java.util.stream.Collectors.*;
import main.*;

/**
 *
 * @author Alex
 */
public class OrderController {

    /**
     * All orders waiting to be executing, currently executing
     * and that have finished executing
     */
    public Set<Order> orders = new HashSet<>();

    private final SocketInput socket;

    public OrderController() {
        socket = new SocketInput(Main.config.getI("socket.port"), 9);
        socket.start();
    }

    public void update() {
        // Get new orders from socket and process them
        socket.getNewPackets().stream().forEach(this::processNewOrder);
    }
    
    public Set<Order> getPendingOrders() {
        return orders.stream().filter(o -> o.isPending()).collect(toSet());
    }

    private void processNewOrder(byte[] packet) { // TODO: do error handling
        String orderString = new String(packet, 0, packet.length);

        if (orderString.charAt(0) != ':') {
            return;
        }

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
            default: return;
        }

        orders.add(order);
    }
}
