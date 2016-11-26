package coms;

import java.net.*;
import java.util.*;

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
        synchronized (packets) {
            List<byte[]> copy = new ArrayList(packets);
            packets.clear();
            return copy;
        }
    }

    private void threadMethod() {
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

    private void addPacket(byte[] packet) {
        synchronized (packets) {
            packets.add(packet);
        }
    }
}
