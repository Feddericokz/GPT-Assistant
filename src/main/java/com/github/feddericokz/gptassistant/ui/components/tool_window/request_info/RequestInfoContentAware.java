package com.github.feddericokz.gptassistant.ui.components.tool_window.request_info;

import com.github.feddericokz.gptassistant.ui.components.tool_window.ToolWindowContent;
import com.github.feddericokz.gptassistant.ui.components.tool_window.ToolWindowFactory;

import javax.swing.*;

public interface RequestInfoContentAware {

    void updateContent(ToolWindowContent content, String update);

    default void updateToolWindowContent(String update) {
        ToolWindowContent content = ToolWindowFactory.getToolWindowContent();
        if (content != null) {
            SwingUtilities.invokeLater(internalUpdateContent(content, update));
        }
    }

    /*
     * What an ugly hack, verification fails with error:
     *
     * Method com.github.feddericokz.gptassistant.ui.components.tool_window.request_info.RequestInfoContentAware.updateToolWindowContent(String update) : void contains an *invokeinterface* instruction referencing a private method com.github.feddericokz.gptassistant.ui.components.tool_window.request_info.RequestInfoContentAware.lambda$updateToolWindowContent$0(ToolWindowContent content, String update) : void. This can lead to **IncompatibleClassChangeError** exception at runtime.
     */
    default Runnable internalUpdateContent(ToolWindowContent content, String update) {
        return new Runnable() {
            @Override
            public void run() {
                updateContent(content, update);
            }
        };
    }

}

