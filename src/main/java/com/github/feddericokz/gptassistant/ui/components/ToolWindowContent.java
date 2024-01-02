package com.github.feddericokz.gptassistant.ui.components;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;

import javax.swing.*;
import java.awt.*;

public class ToolWindowContent extends JPanel {

    private JBTextArea logArea;

    public ToolWindowContent(Project project) {
        // Set up the layout and add components
        setLayout(new BorderLayout());
        logArea = new JBTextArea();
        logArea.setEditable(false); // Make the log area read-only

        JBScrollPane scrollPane = new JBScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void appendLogMessage(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength()); // Auto-scroll to the bottom
    }

}
