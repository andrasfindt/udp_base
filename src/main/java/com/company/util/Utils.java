package com.company.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class Utils {

    public static String getUserInput(InputStream in) throws IOException {
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(in));
        if (inFromUser.ready()) {
            return inFromUser.readLine();
        }
        return null;
    }

    public static boolean isNullOrBlank(String value) {
        return null == value || "".equals(value.trim());
    }

    public static String mapToString(Map<String, Integer> rejectedThreads) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Integer> entry: rejectedThreads.entrySet()) {
            stringBuilder.append(entry.getKey()).append(" --> has failed ").append(entry.getValue()).append(" times\n");
        }
        return stringBuilder.toString().trim();
    }
}
