/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coms;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author Alex
 */
public class SocketInput {    
    public final int packetSize;
    public final int port;
    private final List<byte[]> packets = new ArrayList<>();
        
    public SocketInput(int port, int packetSize) {
        this.port = port;
        this.packetSize = packetSize;
    }
    
    public void start() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                threadMethod();
            }
        };
        
        thread.setDaemon(true);
        thread.start();
    }
       
    public List<byte[]> getNewPackets() {
        synchronized(packets) {
            List<byte[]> copy = new ArrayList(packets);
            packets.clear();
            return copy;
        }
    }
    
    private void threadMethod() {
        try {
            try (DatagramSocket socket = new DatagramSocket(port))
            {
                byte[] buffer = new byte[packetSize];
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                
                System.out.println("Listening to UDP packages on " + port);

                while (true) {
                    socket.receive(incoming);
                    addPacket(incoming.getData());
                    //System.out.println(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + new String(incoming.getData(), 0, incoming.getLength()));
                }
            }            
        }
        catch (Exception ex) {
            throw new Error("Error ocurred while listening on " + port + ": " + ex);
        }
    }
    private void addPacket(byte[] packet) {
        synchronized(packets) {
            packets.add(packet);
        }
    }
    
}
