package com.github.feddericokz.gptassistant.ui.components.toolwindow.log;

import com.github.feddericokz.gptassistant.common.Logger;
import com.github.feddericokz.gptassistant.ui.components.toolwindow.ToolWindowContent;
import com.github.feddericokz.gptassistant.ui.components.toolwindow.ToolWindowFactory;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ToolWindowLogger implements Logger {

    private final Class<?> clazz;

    private ToolWindowLogger(Class<?> clazz) {
        this.clazz = clazz;
    }

    public static ToolWindowLogger getInstance(Class<?> clazz) {
        return new ToolWindowLogger(clazz);
    }

    public void log(String message, String logLevel) {
        ToolWindowContent content = ToolWindowFactory.getToolWindowContent();
        if (content != null) {
            String formattedMessage = formatLogMessage(message, logLevel);
            SwingUtilities.invokeLater(() -> content.getLogTab().logMessage(formattedMessage, logLevel));
        }
    }

    private String formatLogMessage(String message, String logLevel) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return "[" + logLevel + "] " + currentTime + " | " + clazz.getName() + " | " + message;
    }

    public void info(String message) {
        log(message, "INFO");
    }

    public void debug(String message) {
        log(message, "DEBUG");
    }

    public void warning(String message) {
        log(message, "WARNING");
    }

    public void error(String message) {
        log(message, "ERROR");
    }

    public void log(String message, String logLevel, Exception exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();

        ToolWindowContent content = ToolWindowFactory.getToolWindowContent();
        if (content != null) {
            String formattedMessage = formatLogMessage(message + "\n" + exceptionAsString, logLevel);
            SwingUtilities.invokeLater(() -> content.getLogTab().logMessage(formattedMessage, logLevel));
        }
    }

    public void warning(String message, Exception exception) {
        log(message, "WARNING", exception);
    }

    public void error(String message, Exception exception) {
        log(message, "ERROR", exception);
    }


}

