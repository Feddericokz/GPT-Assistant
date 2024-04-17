package com.github.feddericokz.gptassistant.actions.handlers;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface AssistantResponseHandler {
    void handleResponse(@NotNull AnActionEvent e, @NotNull List<String> assistantResponse);

    @NotNull
    static String getXmlTagContentFromResponse(List<String> assistantResponse, String xmlOpeningTag, String xmlClosingTag) {
        return assistantResponse.stream()
                .filter(response -> response.contains(xmlOpeningTag))
                .map(response -> response.substring(response.indexOf(xmlOpeningTag) + xmlOpeningTag.length(), response.indexOf(xmlClosingTag)))
                .findFirst()
                .orElse("");
    }
}
