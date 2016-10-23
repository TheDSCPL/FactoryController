/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import control.order.Order;
import coms.*;
import control.order.*;
import java.util.*;
import main.*;

/**
 *
 * @author Alex
 */
public class OrderController {
    // TODO: write class

    public Set<Order> pendingOrders = new HashSet<>();
    public Set<Order> completedOrders = new HashSet<>();
    private final SocketInput socket;
    
    public OrderController() {
        socket = new SocketInput(Main.config.getI("socket.port"), 20);
        socket.start();
    }
    
    public void update() {
        
        // Get new orders from socket and process them
        socket.getNewPackets().stream().forEach(this::processNewOrder);
    }
    
    private void processNewOrder(byte[] packet) {
        System.out.println("processNewOrder: " + new String(packet, 0, packet.length));
        
        // TODO unsure about how to process the bytes - we have to confirm with our teacher
        //byte type = packet[0];
        //int orderN = Integer.parseString(new String(packet[1:3], StandardCharsets.UTF_8));
        //int s = packet[4];
        
        // Example:
        Order order = new MachiningOrder(1, 5, Block.Type.P1, Block.Type.P3);
        pendingOrders.add(order);
    }
    
    void completeOrder(Order o) {
        pendingOrders.remove(o);
        completedOrders.add(o);
    }
}
