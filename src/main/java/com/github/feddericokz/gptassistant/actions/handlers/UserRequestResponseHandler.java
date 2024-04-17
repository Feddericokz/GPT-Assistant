package com.github.feddericokz.gptassistant.actions.handlers;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UserRequestResponseHandler implements AssistantResponseHandler {
    @Override
    public void handleResponse(@NotNull AnActionEvent e, @NotNull List<String> assistantResponse) {
        // TODO Plan is to display this in the UI for the user to see.
    }
}
