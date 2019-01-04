package com.company.server;

import java.net.DatagramPacket;

public interface PackageProcessor {

    void processPacket(DatagramPacket datagramPacket);
}
