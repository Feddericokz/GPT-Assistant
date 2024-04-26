package com.github.feddericokz.gptassistant.ui.components.tool_window;

import com.github.feddericokz.gptassistant.ui.components.tool_window.context.ContextTab;
import com.github.feddericokz.gptassistant.ui.components.tool_window.log.LogsTab;
import com.github.feddericokz.gptassistant.ui.components.tool_window.request_info.RequestInfoTab;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;


@Getter
public class ToolWindowContent extends JPanel {

    private final LogsTab logTab;
    private final ContextTab contextTab;
    private final RequestInfoTab requestInfoTab;

    public ToolWindowContent() {
        // Initialize components
        logTab = new LogsTab();
        JBScrollPane logScrollPane = new JBScrollPane(logTab);

        contextTab = new ContextTab();
        JBScrollPane contextScrollPane = new JBScrollPane(contextTab);

        requestInfoTab = new RequestInfoTab();
        JBScrollPane requestInfoScrollPane = new JBScrollPane(requestInfoTab);

        // Initialize tabbed pane and add tabs
        JTabbedPane tabbedPane = new JBTabbedPane();
        tabbedPane.addTab("Logs", logScrollPane);
        tabbedPane.addTab("Context", contextScrollPane);
        tabbedPane.addTab("Request Info", requestInfoScrollPane);

        // Configure panel layout and add the tabbed pane
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
    }

}
