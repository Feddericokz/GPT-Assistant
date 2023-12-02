package com.github.feddericokz.gptassistant.behaviors;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;

public interface FollowUpHandler {

    void handleResponse(AnActionEvent event, ChatCompletionChoice chatCompletionChoice);

    void handleResponse(AnActionEvent event);

    void storeResponse(ChatCompletionChoice chatCompletionChoice);

}
