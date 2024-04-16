package com.github.feddericokz.gptassistant.ui.components.toolwindow;

import com.github.feddericokz.gptassistant.configuration.PluginSettings;
import com.github.feddericokz.gptassistant.ui.components.toolwindow.context.ContextTab;
import com.github.feddericokz.gptassistant.ui.components.toolwindow.log.LogsTab;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextArea;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;


public class ToolWindowContent extends JPanel {

    @Getter
    private final LogsTab logTab;
    @Getter
    private final ContextTab contextTab;
    private final JTabbedPane tabbedPane;

    public ToolWindowContent(Project project) {
        // Initialize components
        logTab = new LogsTab();
        JBScrollPane logScrollPane = new JBScrollPane(logTab);

        contextTab = new ContextTab();
        JBScrollPane contextScrollPane = new JBScrollPane(contextTab);

        // Initialize tabbed pane and add tabs
        tabbedPane = new JBTabbedPane();
        tabbedPane.addTab("Logs", logScrollPane);
        tabbedPane.addTab("Context", contextScrollPane);

        // Configure panel layout and add the tabbed pane
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
    }

}
