package com.github.feddericokz.gptassistant.ui.components.tool_window.log;

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
            // TODO Refactor to use JBColor?
            case "ERROR":
                StyleConstants.setForeground(attributeSet, new Color(255, 105, 97)); // Pastel Red
                break;
            case "WARN":
                StyleConstants.setForeground(attributeSet, new Color(255, 179, 71)); // Pastel Orange
                break;
            case "INFO":
                StyleConstants.setForeground(attributeSet, new Color(119, 221, 119)); // Pastel Green
                break;
            case "DEBUG":
                StyleConstants.setForeground(attributeSet, new Color(162, 181, 205)); // Pastel Blue
                break;
            default:
                StyleConstants.setForeground(attributeSet, new Color(221, 221, 221)); // Pastel Grey

        }
        try {
            logDoc.insertString(logDoc.getLength(), message + "\n", attributeSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logPane.setCaretPosition(logDoc.getLength());
    }

}
