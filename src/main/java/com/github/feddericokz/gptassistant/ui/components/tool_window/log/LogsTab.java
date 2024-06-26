package com.github.feddericokz.gptassistant.ui.components.tool_window.log;

import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class LogsTab extends JPanel {

    private JTextPane logPane;
    private StyledDocument logDoc;

    public LogsTab() {
        setLayout(new BorderLayout());
        createLogPane();
    }

    private void createLogPane() {
        logPane = new JTextPane();
        logPane.setEditable(false);
        logDoc = logPane.getStyledDocument();
        add(new JBScrollPane(logPane), BorderLayout.CENTER);
    }

    public void logMessage(String message, String level) {
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        switch (level.toUpperCase()) {
            case "ERROR":
                StyleConstants.setForeground(attributeSet, new JBColor(new Color(255, 105, 97), new Color(255, 105, 97))); // Pastel Red
                break;
            case "WARN":
                StyleConstants.setForeground(attributeSet, new JBColor(new Color(255, 179, 71), new Color(255, 179, 71))); // Pastel Orange
                break;
            case "INFO":
                StyleConstants.setForeground(attributeSet, new JBColor(new Color(119, 221, 119), new Color(119, 221, 119))); // Pastel Green
                break;
            case "DEBUG":
                StyleConstants.setForeground(attributeSet, new JBColor(new Color(162, 181, 205), new Color(162, 181, 205))); // Pastel Blue
                break;
            default:
                StyleConstants.setForeground(attributeSet, new JBColor(Gray._221, Gray._221)); // Pastel Grey

        }
        try {
            logDoc.insertString(logDoc.getLength(), message + "\n", attributeSet);
        } catch (Exception e) {
            throw new RuntimeException("Error while trying to log into the logs assistant tab.", e);
        }
        logPane.setCaretPosition(logDoc.getLength());
    }

}
