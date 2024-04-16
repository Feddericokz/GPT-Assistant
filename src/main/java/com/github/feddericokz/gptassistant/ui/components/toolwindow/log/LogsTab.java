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

    public void logMessage(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

}
