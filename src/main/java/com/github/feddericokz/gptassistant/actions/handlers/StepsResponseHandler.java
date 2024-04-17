package com.github.feddericokz.gptassistant.actions.handlers;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StepsResponseHandler implements AssistantResponseHandler {
    @Override
    public void handleResponse(@NotNull AnActionEvent e, @NotNull List<String> assistantResponse) {
        // TODO Idea is to display these steps for the user to see.
    }
}
