package coms;

import java.net.*;
import java.util.*;

public class SocketInput extends Thread {

    public final int packetSize;
    public final int port;
    private final List<byte[]> packets = new ArrayList<>();

    public SocketInput(int port, int packetSize) {
        this.port = port;
        this.packetSize = packetSize;
        setDaemon(true);
    }
    
    public synchronized List<byte[]> getNewPackets() {
        List<byte[]> copy = new ArrayList(packets);
        packets.clear();
        return copy;
    }

    @Override
    public void run() {
        try {
            try (DatagramSocket socket = new DatagramSocket(port)) {
                byte[] buffer = new byte[packetSize];
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);

                //System.out.println("Listening to UDP packages on " + port);
                while (true) {
                    socket.receive(incoming);
                    addPacket(incoming.getData());
                }
            }
        }
        catch (Exception ex) {
            throw new Error("Error ocurred while listening on " + port + ": " + ex);
        }
    }

    private synchronized void addPacket(byte[] packet) {
        packets.add(packet);
    }
}
