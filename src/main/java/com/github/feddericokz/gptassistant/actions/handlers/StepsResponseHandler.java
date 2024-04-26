package com.github.feddericokz.gptassistant.actions.handlers;

import com.github.feddericokz.gptassistant.ui.components.toolwindow.ToolWindowContent;
import com.github.feddericokz.gptassistant.ui.components.toolwindow.ToolWindowFactory;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class StepsResponseHandler implements AssistantResponseHandler, RequestInfoContentAware {

    @Override
    public void handleResponse(@NotNull AnActionEvent e, @NotNull List<String> assistantResponse) {
        updateToolWindowContent(AssistantResponseHandler.getXmlTagFromResponse(assistantResponse, "steps"));
    }

    @Override
    public void updateContent(ToolWindowContent content, String update) {
        content.getRequestInfoTab().updateSteps(update);
    }

}
