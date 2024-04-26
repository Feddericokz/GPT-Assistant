package com.github.feddericokz.gptassistant.ui.components.toolwindow.log;

import com.intellij.ui.components.JBTextArea;

import javax.swing.*;
import java.awt.*;

public class LogsTab extends JPanel {

    private JBTextArea logArea;

    public LogsTab() {
        setLayout(new BorderLayout());
        createLogArea();
    }

    private void createLogArea() {
        logArea = new JBTextArea();
        logArea.setEditable(false);
        add(logArea, BorderLayout.CENTER);
    }

    public void logMessage(String message, String level) {
        switch (level.toUpperCase()) {
            case "ERROR":
                logArea.append("\u001B[31m" + message + "\u001B[0m\n"); // Red
                break;
            case "WARN":
                logArea.append("\u001B[33m" + message + "\u001B[0m\n"); // Yellow
                break;
            case "INFO":
                logArea.append("\u001B[32m" + message + "\u001B[0m\n"); // Green
                break;
            case "DEBUG":
                logArea.append("\u001B[34m" + message + "\u001B[0m\n"); // Blue
                break;
            default:
                logArea.append(message + "\n"); // Default color
        }
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

}
