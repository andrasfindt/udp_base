package com.company.server;

import com.company.util.Utils;

import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;

public class MonitorThread implements Runnable, Stoppable {
    private ThreadPoolExecutor executor;
    private int seconds;
    private boolean run = true;

    public MonitorThread(ThreadPoolExecutor executor, int delay) {
        this.executor = executor;
        this.seconds = delay;
    }

    @Override
    public void run() {
        while (run) {
            String latest = String.format("[%s] Monitor:[%d/%d(%d)] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s, rejected: %d\n",
                    Configuration.SIMPLE_DATE_FORMAT.format(new Date()),
                    this.executor.getPoolSize(),
                    this.executor.getCorePoolSize(),
                    this.executor.getMaximumPoolSize(),
                    this.executor.getActiveCount(),
                    this.executor.getCompletedTaskCount(),
                    this.executor.getTaskCount(),
                    this.executor.isShutdown(),
                    this.executor.isTerminated(),
                    MonitorCache.rejectedCount);
            System.out.println(latest + "\n" + Utils.mapToString(MonitorCache.rejectedThreads));
            MonitorCache.setLatest(latest);
            try {
                Thread.sleep(seconds * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() {
        this.run = false;
    }

    @Override
    public boolean isStopped() {
        return !run;
    }
}

