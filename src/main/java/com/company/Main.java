package com.company;

import com.company.server.Configuration;
import com.company.server.StoppableServer;
import com.company.server.PackageProcessor;
import com.company.server.UDPServer;
import com.company.util.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Main {

    public static void main(String[] args) throws IOException {
        Configuration configuration = new Configuration(args);
        StoppableServer server = UDPServer.bindServer(configuration);
        server.startServer();
        System.out.println("server started: " + server);
        while (!server.isStopped()) {
            String s = Utils.getUserInput(System.in);
            if (!Utils.isNullOrBlank(s)) {
                if (Configuration.CLIENT_EXIT_REQUEST.equalsIgnoreCase(s)) {
                    server.stop();
                }
                if (Configuration.CLIENT_STRESS_TEST_REQUEST.equalsIgnoreCase(s)) {
                    for (int i = 0; i < 500; i++) {
                        InetAddress ipAddress = InetAddress.getByName("localhost");
                        String poop = Configuration.CLIENT_STRESS_TEST_REQUEST;
                        DatagramPacket sendPacket = new DatagramPacket(poop.getBytes(), poop.getBytes().length, ipAddress, configuration.getPort());
                        ((PackageProcessor) server).processPacket(sendPacket);
                    }
                }
            }
        }
        System.out.println("bye");
    }
}
