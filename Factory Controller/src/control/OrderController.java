/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import coms.*;
import java.nio.charset.*;
import java.util.*;
import main.*;

/**
 *
 * @author Alex
 */
public class OrderController {
    // TODO: write class

    public Order[] orders;
    private SocketInput socket;
    
    public OrderController() {
        socket = new SocketInput(Main.config.getS("socket.host"), Main.config.getI("socket.port"));
        socket.start();
    }
    
    public void update() {
        
        // Get new orders from socket
        if (socket.hasNewLines()) {
            List<String> lines = socket.getNewLines();
            for (String line : lines) {
                processNewOrder(line);
            }
        }
    }
    
    private void processNewOrder(String line) {
        byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
        
        byte type = bytes[0];
        int orderN = Integer.parseString(new String(bytes[1:3], StandardCharsets.UTF_8));
        int type = bytes[4];
    }
}
