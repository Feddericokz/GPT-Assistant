package com.github.feddericokz.gptassistant.actions.handlers;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FileCreationResponseHandler implements AssistantResponseHandler {
    @Override
    public void handleResponse(@NotNull AnActionEvent e, @NotNull List<String> assistantResponse) {
        // TODO, Implement logic to create file from response.
    }
}
