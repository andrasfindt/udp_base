package com.company.server;

import com.company.logging.Level;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class Configuration {
    public static final int WRITE_BUFFER_SIZE_BYTES = 1024;
    public static final Level DEFAULT_LOG_LEVEL = Level.INFO;
    public static final String CLIENT_EXIT_RESPONSE = "baaai";
    public static final String CLIENT_EXIT_REQUEST = "q";
    public static final String CLIENT_STRESS_TEST_REQUEST = "stress";
    public static final String CLIENT_SIDE_EXIT = "exit";
    public static final String MONITOR_REQUEST = "monit";
    public static final String DEFAULT_RESPONSE = "ACK";
    public static final int DEFAULT_THREAD_COUNT = 4;
    static final int READ_BUFFER_SIZE_BYTES = 1024;
    static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final int ONE_SECOND_IN_MILLIS = 1000;
    private static final int FIVE_SECONDS_IN_MILLIS = 5 * ONE_SECOND_IN_MILLIS;
    private static final int DEFAULT_TIMEOUT = FIVE_SECONDS_IN_MILLIS;
    private static final int DEFAULT_PORT = 10301;
    private int timeout = DEFAULT_TIMEOUT;
    private int port = DEFAULT_PORT;
    private Level logLevel = DEFAULT_LOG_LEVEL;
    private int threadCount = DEFAULT_THREAD_COUNT;
    private Properties props;

    public Configuration(String[] arguments) {
//        List<String> strings = Arrays.asList(arguments);
        boolean useSettingsFile = false;
        String settingsFile = null;
        for (int i = 0; i < arguments.length; i++) {
            switch (arguments[i]) {
                case Argument.TIMEOUT_ABBR:
                case Argument.TIMEOUT:
                    timeout = Integer.parseInt(arguments[++i]);
                    break;
                case Argument.PORT_ABBR:
                case Argument.PORT:
                    port = Integer.parseInt(arguments[++i]);
                    break;
                case Argument.THREAD_COUNT_ABBR:
                case Argument.THREAD_COUNT:
                    threadCount = Integer.parseInt(arguments[++i]);
                    break;
                case Argument.VERBOSE_ABBR:
                case Argument.VERBOSE:
                    logLevel = Level.DEBUG;
                    break;
                case Argument.SETTING_FILE_ABBR:
                case Argument.SETTING_FILE:
                    useSettingsFile = true;
                    settingsFile = arguments[++i];
                    break;
            }
            if (useSettingsFile) {
                break;
            }
        }
        if (useSettingsFile) {
            parseSettingsFile(settingsFile);
        }
    }

    private void parseSettingsFile(String settingsFile) {
        try {
            InputStream is = new FileInputStream(settingsFile);
            props = new Properties();
            props.load(is);
            is.close();

            timeout = Integer.valueOf(props.getProperty("timeout", String.valueOf(DEFAULT_TIMEOUT)));
            port = Integer.valueOf(props.getProperty("port", String.valueOf(DEFAULT_PORT)));
            logLevel = Level.valueOf(props.getProperty("loglevel", DEFAULT_LOG_LEVEL.name()));
            threadCount = Integer.valueOf(props.getProperty("threadcount", String.valueOf(DEFAULT_THREAD_COUNT)));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public int getPort() {
        return port;
    }

    public int getTimeout() {
        return timeout;
    }

    Level getLogLevel() {
        return logLevel;
    }

    int getThreadCount() {
        return threadCount;
    }

    public class Argument {
        static final String TIMEOUT_ABBR = "-t";
        static final String TIMEOUT = "--timeout";
        static final String PORT_ABBR = "-p";
        static final String PORT = "--port";
        static final String VERBOSE_ABBR = "-v";
        static final String VERBOSE = "--verbose";
        static final String THREAD_COUNT_ABBR = "-T";
        static final String THREAD_COUNT = "--thread-count";
        static final String SETTING_FILE_ABBR = "-s";
        static final String SETTING_FILE = "--settings-file";
    }
}
