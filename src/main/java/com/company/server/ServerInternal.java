package com.company.server;

import com.company.logging.Level;

import java.io.IOException;
import java.net.DatagramPacket;

interface ServerInternal extends Stoppable, PackageProcessor {
    void receive(DatagramPacket receivedPacket) throws IOException;

    void send(DatagramPacket receivedPacket) throws IOException;

    boolean isSocketOpen();

    void closeSocket();

    State getState();

    int getTimeout();

    Level getLogLevel();
}
