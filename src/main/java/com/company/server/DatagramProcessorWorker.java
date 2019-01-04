package com.company.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class DatagramProcessorWorker implements Runnable {
    private DatagramPacket packet;
    private ServerInternal server;

    DatagramProcessorWorker(DatagramPacket packet, ServerInternal server) {
        this.packet = packet;
        this.server = server;
    }

    @Override
    public void run() {
        String command = new String(packet.getData()).trim();
        processCommand(command);
    }

    private void processCommand(String command) {
        System.out.println("Rx:" + command);
        if (Configuration.CLIENT_EXIT_REQUEST.equalsIgnoreCase(command)) {
            sendReply(packet, Configuration.CLIENT_EXIT_RESPONSE);
            new Thread(() -> server.stop()).start();
            return;
        }
        if (Configuration.MONITOR_REQUEST.equalsIgnoreCase(command)) {
            sendReply(packet, MonitorCache.getLatest());
            return;
        }
        if (Configuration.CLIENT_STRESS_TEST_REQUEST.equalsIgnoreCase(command)) {
            System.out.println(Configuration.CLIENT_STRESS_TEST_REQUEST);
            return;
        }
        sendReply(packet, Configuration.DEFAULT_RESPONSE);
    }

    void sendReply(DatagramPacket packet, String response) {
        new Thread(() -> {
            InetAddress ipAddress = packet.getAddress();
            int port = packet.getPort();
            DatagramPacket sendPacket = new DatagramPacket(response.getBytes(), response.getBytes().length, ipAddress, port);
            try {
                server.send(sendPacket);
                System.out.println("Tx:" + response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public String toString() {
        return super.toString() + " " + new String(packet.getData()).trim();
    }
}
