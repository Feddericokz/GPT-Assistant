package com.github.feddericokz.gptassistant.behaviors;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;

public abstract class AbstractFollowUpHandler implements FollowUpHandler {

    protected ChatCompletionChoice completionChoice;

    @Override
    public void handleResponse(AnActionEvent event) {
        if (completionChoice != null) {
            this.handleResponse(event, completionChoice);
        } else {
            // TODO Log that it wasn't executed.
        }
    }

    @Override
    public void storeResponse(ChatCompletionChoice chatCompletionChoice) {
        this.completionChoice = chatCompletionChoice;
    }
}
