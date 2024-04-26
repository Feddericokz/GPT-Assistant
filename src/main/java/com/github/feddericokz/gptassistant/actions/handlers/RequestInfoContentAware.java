package com.github.feddericokz.gptassistant.actions.handlers;

import com.github.feddericokz.gptassistant.ui.components.toolwindow.ToolWindowContent;
import com.github.feddericokz.gptassistant.ui.components.toolwindow.ToolWindowFactory;

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
