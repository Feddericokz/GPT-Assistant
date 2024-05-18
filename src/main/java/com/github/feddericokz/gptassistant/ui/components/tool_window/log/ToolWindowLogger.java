package com.github.feddericokz.gptassistant.ui.components.tool_window.log;

import com.github.feddericokz.gptassistant.common.Logger;
import com.github.feddericokz.gptassistant.ui.components.tool_window.ToolWindowContent;
import com.github.feddericokz.gptassistant.ui.components.tool_window.ToolWindowFactory;
import com.intellij.openapi.application.ApplicationManager;

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

    @Override
    public void log(String message, String logLevel) {
        ToolWindowContent content = ToolWindowFactory.getToolWindowContent();
        if (content != null) {
            String formattedMessage = formatLogMessage(message, logLevel);
            ApplicationManager.getApplication().invokeLater(() -> content.getLogTab().logMessage(formattedMessage, logLevel));
        }
    }

    private String formatLogMessage(String message, String logLevel) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return "[" + logLevel + "] " + currentTime + " | " + clazz.getName() + " | " + message;
    }

    @Override
    public void info(String message) {
        log(message, "INFO");
    }

    @Override
    public void debug(String message) {
        log(message, "DEBUG");
    }

    @Override
    public void warning(String message) {
        log(message, "WARNING");
    }

    @Override
    public void error(String message) {
        log(message, "ERROR");
    }

    @Override
    public void log(String message, String logLevel, Throwable exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();

        ToolWindowContent content = ToolWindowFactory.getToolWindowContent();
        if (content != null) {
            String formattedMessage = formatLogMessage(message + "\n" + exceptionAsString, logLevel);
            ApplicationManager.getApplication().invokeLater(() -> content.getLogTab().logMessage(formattedMessage, logLevel));
        }
    }

    @Override
    public void warning(String message, Throwable exception) {
        log(message, "WARNING", exception);
    }

    @Override
    public void error(String message, Throwable exception) {
        log(message, "ERROR", exception);
    }

}

