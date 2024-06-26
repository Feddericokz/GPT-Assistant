package com.github.feddericokz.gptassistant.common;

public interface Logger {
    void log(String message, String logLevel);

    void info(String message);

    void debug(String message);

    void warning(String message);

    void error(String message);

    void log(String message, String logLevel, Throwable exception);

    void warning(String message, Throwable exception);

    void error(String message, Throwable exception);
}
