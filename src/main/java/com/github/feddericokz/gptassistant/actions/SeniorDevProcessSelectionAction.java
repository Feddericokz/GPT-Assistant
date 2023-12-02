package com.github.feddericokz.gptassistant.actions;

import com.github.feddericokz.gptassistant.behaviors.SeniorDevBehaviorPattern;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.github.feddericokz.gptassistant.ActionEventUtils.getFileLanguage;

public abstract class SeniorDevProcessSelectionAction extends ProcessSelectionAction {

    public SeniorDevProcessSelectionAction() {
        super(new SeniorDevBehaviorPattern());
    }

    @Override
    public List<ChatMessage> getMessagesForRequest(AnActionEvent e, String selection) {

        // Get the language of the file.
        String language = getFileLanguage(e);

        // Return formatter prompt.
        return getMessagesForRequest(language, selection);
    }

    private List<ChatMessage> getMessagesForRequest(String language, String selection) {
        return new ArrayList<>(Arrays.asList(
                new ChatMessage(ChatMessageRole.SYSTEM.value(), getSystemPrompt(language)),
                new ChatMessage(ChatMessageRole.USER.value(), selection)
        ));
    }

    private String getSystemPrompt(String language) {
        return behaviorPattern.getSystemPrompt().getPromptString().replace("[PROGRAMMING LANGUAGE]", language);
    }

    @Override
    protected void performFollowUpOperations(AnActionEvent e, List<ChatCompletionChoice> choices) {
        behaviorPattern.getFollowUpPrompts().forEach(((prompt, followUpHandler) -> {
            followUpHandler.handleResponse(e);
        }));
    }

    @Override
    public Collection<? extends ChatCompletionChoice> performFolowUpRequests(AnActionEvent e, List<ChatMessage> messages) {

        List<ChatCompletionChoice> completionChoices = new ArrayList<>();

        this.behaviorPattern.getFollowUpPrompts().forEach((prompt, followUpHandler) -> {

            // Create message with follow-up prompt.
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), prompt.getPromptString()));

            // TODO, Should have config to make follow up one use less expensive GPT model.
            ChatCompletionRequest completionRequest = getChatCompletionRequestUsingModel(messages, getGPTModel());

            ChatCompletionChoice completionChoice = makeRequestAndGetResponse(completionRequest);

            // Will be storing responses on handlers to process them later.
            followUpHandler.storeResponse(completionChoice);

            // This method returns a list of choices, building it to keep the method contract.
            completionChoices.add(completionChoice);

            // Add GPT response to messages to keep context.
            messages.add(completionChoice.getMessage());

        });

        return completionChoices;
    }

    private ChatCompletionChoice makeRequestAndGetResponse(ChatCompletionRequest completionRequest) {
        return getOpenAiService().createChatCompletion(completionRequest).getChoices().get(0);
    }
}
