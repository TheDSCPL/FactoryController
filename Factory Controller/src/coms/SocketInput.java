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
    public final String host;
    public final int port;
    private final List<String> lines = new ArrayList<>();
        
    public SocketInput(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    public void start() {
        Thread thread = new Thread() {
            public void run() {
                threadMethod();
            }
        };
        
        thread.start();
    }
    public boolean hasNewLines() {
        synchronized(lines) {
            return lines.size() > 0;
        }
    }    
    public List<String> getNewLines() {
        synchronized(lines) {
            List<String> ret = new ArrayList(lines);
            lines.clear();
            return ret;
        }
    }
    
    private void threadMethod() {
        try {
            try (Socket socket = new Socket(host, port); // TODO dunno if this is the correct way to implement a client for the UDP protocol, probably not
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())))
            {
                System.out.println("Connected to socket server on " + host + ":" + port);
                
                String line;
                while ((line = in.readLine()) != null) {
                    addLine(line);
                }
                
            }
            
            System.out.println("Socket server closed on " + host + ":" + port);
        }
        catch (Exception ex) {
            throw new Error("Could not connect to socket on " + host + ":" + port);
        }
    }
    private void addLine(String line) {
        synchronized(lines) {
            lines.add(line);
        }
    }
    
}
