package com.company.server;

import com.company.logging.Level;

import java.net.DatagramPacket;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DatagramProcessor implements Stoppable {
    private final Object stateLock = new Object();
    private int threadCount;
    private ServerInternal server;
    private ThreadPoolExecutor executorPool;
    private MonitorThread monitor;
    private ThreadState threadState = ThreadState.UNINITIALIZED;


    DatagramProcessor(int threadCount) {
        this.threadCount = threadCount;
    }

    void processDatagram(DatagramPacket packet) {
        executorPool.execute(new DatagramProcessorWorker(packet, server));
    }

    private void setState(ThreadState newState) {
        synchronized (stateLock) {
            if (Level.DEBUG == server.getLogLevel()) {
                System.out.format("[%s] DatagramProcessor:%s --> %s\n", Configuration.SIMPLE_DATE_FORMAT.format(new Date()), threadState, newState);
            }
            threadState = newState;
            printState();
        }
    }

    private void printState() {
        if (Level.DEBUG == server.getLogLevel()) {
            System.out.format("[%s] DatagramProcessor:%s\n", Configuration.SIMPLE_DATE_FORMAT.format(new Date()), threadState);
        }
    }

    @Override
    public void stop() {
        setState(ThreadState.STOPPING);
        monitor.stop();
        executorPool.shutdown();
        while (!executorPool.isTerminated()) {
        }
        setState(ThreadState.STOPPED);
    }

    @Override
    public boolean isStopped() {
        boolean threadStopped = ThreadState.STOPPED == threadState;
        boolean terminated = executorPool.isTerminated();
        boolean monitorStopped = monitor.isStopped();
        return threadStopped && terminated && monitorStopped;
    }

    void startExecutor(ServerInternal server) {
        this.server = server;
        if (ThreadState.RUNNING != threadState) {
            setState(ThreadState.INITIALIZING);
            CommandCacheFullExecutionHandler rejectionHandler = new CommandCacheFullExecutionHandler();
            ThreadFactory threadFactory = Executors.defaultThreadFactory();
            setState(ThreadState.INITIALIZED);
            executorPool = new ThreadPoolExecutor((int) Math.ceil(threadCount / 2.0), threadCount, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(threadCount * 2), threadFactory, rejectionHandler);
            executorPool.allowCoreThreadTimeOut(false);
            monitor = new MonitorThread(executorPool, 5);
            Thread monitorThread = new Thread(monitor, "MonitorThread");
            monitorThread.start();
            setState(ThreadState.RUNNING);
        }
    }
}
