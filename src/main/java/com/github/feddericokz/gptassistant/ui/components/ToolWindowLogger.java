package com.github.feddericokz.gptassistant.ui.components;

import com.github.feddericokz.gptassistant.common.Logger;

import javax.swing.*;

public class ToolWindowLogger implements Logger {

    public void log(String message, String logLevel) {
        ToolWindowContent content = ToolWindowFactory.getToolWindowContent();
        if (content != null) {
            SwingUtilities.invokeLater(() -> content.appendLogMessage(formatLogMessage(message, logLevel)));
        }
    }

    private String formatLogMessage(String message, String logLevel) {
        return "[" + logLevel + "] " + message;
    }

}

