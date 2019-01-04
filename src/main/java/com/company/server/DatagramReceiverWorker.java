package com.company.server;

import com.company.logging.Level;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Date;

public class DatagramReceiverWorker implements Stoppable, Runnable {

    private static final String THREAD_NAME_DATAGRAM_RECEIVER = "DatagramReceiverWorker";
    private final Object stateLock = new Object();
    private Thread listenerThread;
    private ServerInternal server;
    private ThreadState threadState = ThreadState.UNINITIALIZED;

    private boolean serverInCorrectState() {
        return server.getState() == State.LISTENING || server.getState() == State.STARTING_LISTENING;
    }

    private void printSocketClosedMessage() {
        if (Level.DEBUG == server.getLogLevel()) {
            System.out.format("[%s] DatagramReceiver:Socket Closed\n", Configuration.SIMPLE_DATE_FORMAT.format(new Date()));
        }
    }

    private void printTimeoutMessage() {
        if (Level.DEBUG == server.getLogLevel()) {
            System.out.format("[%s] DatagramReceiver:Expected SocketTimeout after %d ms\n", Configuration.SIMPLE_DATE_FORMAT.format(new Date()), server.getTimeout());
        }
    }

    void startThread(ServerInternal server) {
        this.server = server;
        if (ThreadState.RUNNING != threadState) {
            setState(ThreadState.INITIALIZING);
            listenerThread = new Thread(this, THREAD_NAME_DATAGRAM_RECEIVER);
            setState(ThreadState.INITIALIZED);
            listenerThread.start();
            setState(ThreadState.RUNNING);
        }
    }

    private void stopThread() {
        setState(ThreadState.IRQ_FOR_STOP);
    }

    private boolean checkStopState() {
        synchronized (stateLock) {
            if (ThreadState.IRQ_FOR_STOP == threadState) {
                setState(ThreadState.STOPPING);
                return true;
            }
            return false;
        }
    }

    private void setState(ThreadState newState) {
        synchronized (stateLock) {
            if (Level.DEBUG == server.getLogLevel()) {
                System.out.format("[%s] DatagramReceiver:%s --> %s\n", Configuration.SIMPLE_DATE_FORMAT.format(new Date()), threadState, newState);
            }
            threadState = newState;

            printState();
        }
    }

    private void printState() {
        if (Level.DEBUG == server.getLogLevel()) {
            System.out.format("[%s] DatagramReceiver:%s\n", Configuration.SIMPLE_DATE_FORMAT.format(new Date()), threadState);
        }
    }

    @Override
    public void stop() {
        stopThread();
    }

    @Override
    public boolean isStopped() {
        synchronized (stateLock) {
            return ThreadState.STOPPED == threadState;
        }
    }

    @Override
    public void run() {
        while (!checkStopState()) {
//            if (checkStopState()) break;
            if (server.isSocketOpen() && serverInCorrectState()) {
                byte[] receiveData = new byte[Configuration.READ_BUFFER_SIZE_BYTES];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
//                    if (checkStopState()) break;
                    server.receive(receivePacket);
//                    if (checkStopState()) break;
                    server.processPacket(receivePacket);
                } catch (SocketException e) {
                    printSocketClosedMessage();
                } catch (SocketTimeoutException e) {
//                    printTimeoutMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        setState(ThreadState.STOPPED);
    }

    @Override
    public String toString() {
        return String.format("%s\n", THREAD_NAME_DATAGRAM_RECEIVER);
    }
}
