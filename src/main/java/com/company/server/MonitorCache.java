package com.company.server;

import java.util.HashMap;
import java.util.Map;

class MonitorCache {
    static int rejectedCount = 0;
    static Map<String, Integer> rejectedThreads = new HashMap<>();
    private static String latest;

    static String getLatest() {
        return latest;
    }

    static void setLatest(String latest) {
        MonitorCache.latest = latest;
    }
}
