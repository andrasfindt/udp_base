package com.company.server;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class CommandCacheFullExecutionHandler implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        System.out.println(r.toString() + " was rejected, retrying");
        MonitorCache.rejectedCount++;
        Integer integer = MonitorCache.rejectedThreads.getOrDefault(r.toString(), 0);
        MonitorCache.rejectedThreads.put(r.toString(), ++integer);
        executor.execute(r);
    }

}

