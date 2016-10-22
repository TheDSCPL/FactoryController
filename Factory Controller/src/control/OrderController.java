/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import coms.*;
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
        if (socket.hasNewLines()) {
            List<String> lines = socket.getNewLines();
            System.out.println("Got new lines: " + lines);
        }
    }
}
