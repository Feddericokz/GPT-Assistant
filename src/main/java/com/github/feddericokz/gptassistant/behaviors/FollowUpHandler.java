package com.github.feddericokz.gptassistant.behaviors;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;

import java.util.List;

public interface FollowUpHandler {

    void handleResponse(AnActionEvent event, List<String> assistantResponse);

}
