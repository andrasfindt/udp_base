package com.company.client;

import com.company.server.Configuration;
import com.company.util.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class UDPClient {

    private static DatagramSocket clientSocket;

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration(args);
        clientSocket = new DatagramSocket();
        clientSocket.setSoTimeout(configuration.getTimeout());
        while (true) {
            try {
                InetAddress ipAddress = InetAddress.getByName("localhost");
                int port = configuration.getPort();
                String sentence = Utils.getUserInput(System.in);
                if (!Utils.isNullOrBlank(sentence)) {
                    if (isKillCommand(sentence)) {
                        System.out.println("client side kill");
                        break;
                    }
                    if (isStressTestCommand(sentence)) {
                        doStressTest(ipAddress, port);
                        continue;
                    }
                    byte[] sendData;
                    byte[] receiveData = new byte[Configuration.WRITE_BUFFER_SIZE_BYTES];
                    sendData = sentence.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
                    clientSocket.send(sendPacket);
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    clientSocket.receive(receivePacket);
                    String modifiedSentence = new String(receivePacket.getData()).trim();
                    if (checkKillCondition(modifiedSentence)) {
                        System.out.println("server side kill");
                        break;
                    }
                    System.out.println("FROM SERVER:" + modifiedSentence);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        clientSocket.close();
        System.out.println("bye");
    }

    private static void doStressTest(InetAddress ipAddress, int port) throws IOException {
        for (int i = 0; i < 500; i++) {
            String request = Configuration.CLIENT_STRESS_TEST_REQUEST;
            DatagramPacket sendPacket = new DatagramPacket(request.getBytes(), request.getBytes().length, ipAddress, port);
            clientSocket.send(sendPacket);
        }

    }

    private static boolean isStressTestCommand(String sentence) {
        return Configuration.CLIENT_STRESS_TEST_REQUEST.equalsIgnoreCase(sentence);
    }

    private static boolean isKillCommand(String sentence) {
        return Configuration.CLIENT_SIDE_EXIT.equalsIgnoreCase(sentence);
    }

    private static boolean checkKillCondition(String sentence) {
        return Configuration.CLIENT_EXIT_RESPONSE.equalsIgnoreCase(sentence);
    }
}
