package com.github.feddericokz.gptassistant.ui.components.tool_window.request_info;

import com.github.feddericokz.gptassistant.ui.components.tool_window.ToolWindowContent;
import com.github.feddericokz.gptassistant.ui.components.tool_window.ToolWindowFactory;

import javax.swing.*;

public interface RequestInfoContentAware {

    void updateContent(ToolWindowContent content, String update);

    default void updateToolWindowContent(String update) {
        ToolWindowContent content = ToolWindowFactory.getToolWindowContent();
        if (content != null) {
            SwingUtilities.invokeLater(() -> updateContent(content, update));
        }
    }

}
