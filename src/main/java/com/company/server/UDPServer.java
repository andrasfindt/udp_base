package com.company.server;

import com.company.logging.Level;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Date;

public class UDPServer implements ServerInternal, StoppableServer, PackageProcessor {
    private static final UDPServer SERVER = new UDPServer();
    private final Object stateLock = new Object();
    private DatagramSocket serverSocket;
    private State serverState = State.UNINITIALIZED;
    private DatagramReceiverWorker datagramReceiverWorker;
    private Level logLevel = Configuration.DEFAULT_LOG_LEVEL;
    private int threadCount = Configuration.DEFAULT_THREAD_COUNT;
    private DatagramProcessor datagramProcessor;

    public static UDPServer bindServer(Configuration configuration) throws SocketException {
        try {
            SERVER.init(configuration.getPort(), configuration.getTimeout(), configuration.getLogLevel(), configuration.getThreadCount());
        } catch (SocketException e) {
            e.printStackTrace();
            SERVER.setState(State.ERROR);
            throw e;
        }
        return SERVER;
    }

    @Override
    public void startServer() {
        if (null == datagramReceiverWorker) {
            datagramProcessor = new DatagramProcessor(threadCount);
            datagramReceiverWorker = new DatagramReceiverWorker();
        }
        if (State.LISTENING != serverState) {
            setState(State.STARTING_LISTENING);
            datagramProcessor.startExecutor(this);
            datagramReceiverWorker.startThread(this);
            setState(State.LISTENING);
        }
    }

    @Override
    public boolean isSocketOpen() {
        return !serverSocket.isClosed();
    }

    @Override
    public void closeSocket() {
        serverSocket.close();
    }

    @Override
    public void receive(DatagramPacket receivedPacket) throws IOException {
        serverSocket.receive(receivedPacket);
    }

    @Override
    public void send(DatagramPacket sendPacket) throws IOException {
        serverSocket.send(sendPacket);
    }

    @Override
    public void processPacket(DatagramPacket receivedPacket) {
        datagramProcessor.processDatagram(receivedPacket);
    }

    @Override
    public State getState() {
        synchronized (stateLock) {
            return serverState;
        }
    }

    private void setState(State newState) {
        synchronized (stateLock) {
            if (Level.DEBUG == getLogLevel()) {
                System.out.format("[%s] UDPServer:%s --> %s\n", Configuration.SIMPLE_DATE_FORMAT.format(new Date()), serverState, newState);
            }
            serverState = newState;
            printState();
        }
    }

    @Override
    public int getTimeout() {
        return getSoTimeout();
    }

    @Override
    public Level getLogLevel() {
        return logLevel;
    }

    @Override
    public void printState() {

        if (Level.DEBUG == getLogLevel()) {
            System.out.format("[%s] UDPServer:%s\n", Configuration.SIMPLE_DATE_FORMAT.format(new Date()), serverState);
        }
    }

    private void init(int port, int timeout) throws SocketException {
        setState(State.INITIALIZING);
        serverSocket = new DatagramSocket(port);
        serverSocket.setSoTimeout(timeout);
        setState(State.INITIALIZED);
    }

    private void init(int port, int timeout, Level logLevel) throws SocketException {
        init(port, timeout);
        this.logLevel = logLevel;
    }

    private void init(int port, int timeout, Level logLevel, int threadCount) throws SocketException {
        init(port, timeout, logLevel);
        this.threadCount = threadCount;
    }

    @Override
    public String toString() {
        int localPort = SERVER.serverSocket.getLocalPort();
        int soTimeout = getSoTimeout();
        return String.format("UDPServer bound to port %d%s", localPort, timeoutToString(soTimeout));
    }

    private int getSoTimeout() {
        int soTimeout = -1;
        try {
            soTimeout = SERVER.serverSocket.getSoTimeout();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return soTimeout;
    }

    private String timeoutToString(int soTimeout) {
        return soTimeout == -1 || soTimeout == 0 ? "" : String.format(" with timeout of %d ms", soTimeout);
    }

    @Override
    public void stop() {
        setState(State.STOPPING);
        datagramReceiverWorker.stop();
        while (!datagramReceiverWorker.isStopped()) {//wait for stopped
        }
        datagramProcessor.stop();
        while (!datagramProcessor.isStopped()) {//wait for stopped
        }
        closeSocket();
        while (isSocketOpen()) {//wait for closed
        }
        setState(State.STOPPED);
    }

    @Override
    public boolean isStopped() {

        synchronized (stateLock) {
            boolean serverStopped = State.STOPPED == serverState;
            boolean serverThreadStopped = datagramReceiverWorker.isStopped();
            boolean processorThreadStopped = datagramProcessor.isStopped();
            return serverStopped && serverThreadStopped && processorThreadStopped;
        }
    }
}
