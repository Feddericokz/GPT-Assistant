package com.github.feddericokz.gptassistant.ui.components.toolwindow.log;

import com.github.feddericokz.gptassistant.common.Logger;
import com.github.feddericokz.gptassistant.ui.components.toolwindow.ToolWindowContent;
import com.github.feddericokz.gptassistant.ui.components.toolwindow.ToolWindowFactory;

import javax.swing.*;

public class ToolWindowLogger implements Logger {

    public void log(String message, String logLevel) {
        ToolWindowContent content = ToolWindowFactory.getToolWindowContent();
        if (content != null) {
            SwingUtilities.invokeLater(() -> content.getLogTab().logMessage(formatLogMessage(message, logLevel)));
        }
    }

    private String formatLogMessage(String message, String logLevel) {
        return "[" + logLevel + "] " + message;
    }

}

