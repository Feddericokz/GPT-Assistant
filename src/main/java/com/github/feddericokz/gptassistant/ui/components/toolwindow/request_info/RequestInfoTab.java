package com.github.feddericokz.gptassistant.ui.components.toolwindow.request_info;


import com.intellij.ui.components.JBTabbedPane;

import javax.swing.*;
import java.awt.*;


public class RequestInfoTab extends JPanel {
    private JTabbedPane tabbedPane;
    private SwitchableTextDisplayPanel requestStepsPanel;
    private SwitchableTextDisplayPanel userRequestPanel;
    private SwitchableTextDisplayPanel importsPanel;
    private SwitchableTextDisplayPanel codeReplacementPanel;
    private SwitchableTextDisplayPanel fileCreationPanel;

    public RequestInfoTab() {
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        tabbedPane = new JBTabbedPane();

        // Initialize sub-parts
        requestStepsPanel = new SwitchableTextDisplayPanel();
        userRequestPanel = new SwitchableTextDisplayPanel();
        importsPanel = new SwitchableTextDisplayPanel();
        codeReplacementPanel = new SwitchableTextDisplayPanel();
        fileCreationPanel = new SwitchableTextDisplayPanel();

        // Adding panels to tabbed pane
        tabbedPane.addTab("Request Steps", null, requestStepsPanel, "Displays reasoning steps of the assistant.");
        tabbedPane.addTab("User Request", null, userRequestPanel, "Displays the user's request.");
        tabbedPane.addTab("Imports", null, importsPanel, "Displays imports that assistant wants to add.");
        tabbedPane.addTab("Code Replacement", null, codeReplacementPanel, "Displays what the assistant wants to replace the selection with.");
        tabbedPane.addTab("File Creation", null, fileCreationPanel, "Displays the content of a file that the assistant wants to create.");

        // Adding tabbedPane to RequestInfoTab JPanel
        add(tabbedPane, BorderLayout.CENTER);
    }

    // Method to add new tabs if necessary
    public void addTab(String title, Component component, String tip) {
        tabbedPane.addTab(title, null, component, tip);
    }

    public void updateSteps(String update) {
        requestStepsPanel.updateText(update);
    }

    public void updateUserRequest(String update) {
        userRequestPanel.updateText(update);
    }

    public void updateImports(String update) {
        importsPanel.updateText(update);
    }

    public void updateCodeReplacement(String update) {
        codeReplacementPanel.updateText(update);
    }

    public void updateFileCreation(String update) {
        fileCreationPanel.updateText(update);
    }

}


