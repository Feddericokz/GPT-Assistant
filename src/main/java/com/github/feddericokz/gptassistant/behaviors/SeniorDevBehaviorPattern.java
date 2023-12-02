package com.github.feddericokz.gptassistant.behaviors;

import com.github.feddericokz.gptassistant.configuration.Prompts;
import com.github.feddericokz.gptassistant.utils.ImportUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;

import java.util.*;

public class SeniorDevBehaviorPattern extends BehaviorPattern {

    public SeniorDevBehaviorPattern() {
        this.setSystemPrompt(getConfiguredSystemPrompt());
        this.setFollowUpPrompts(getConfiguredFollowUpPrompts());
    }

    private static Prompt getConfiguredSystemPrompt() {
        // TODO Make configurable.
        return new Prompt("SENIOR_DEV_SYSTEM_PROMPT", Prompts.SENIOR_DEV_SYSTEM_PROMPT);
    }

    private static Map<Prompt, FollowUpHandler> getConfiguredFollowUpPrompts() {
        // TODO Make imports prompt configurable.
        return Collections.singletonMap(new Prompt("SENIOR_DEV_IMPORTS_FOLLOW_UP_PROMPT", Prompts.SENIOR_DEV_FOLLOW_UP_IMPORTS), new ImportsFollowUpHandler());
    }

    public static class ImportsFollowUpHandler extends AbstractFollowUpHandler {

        @Override
        public void handleResponse(AnActionEvent event, ChatCompletionChoice chatCompletionChoice) {
            List<String> importStatements = getImportsList(chatCompletionChoice);

            // Update imports if needed.
            if (!importStatements.isEmpty()) {
                importStatements.forEach(importStatement -> {
                    ImportUtils.addImportStatement(event, importStatement);
                });
            }
        }

        private static List<String> getImportsList(ChatCompletionChoice chatCompletionChoice) {
            String content = chatCompletionChoice.getMessage().getContent();

            // Naive approach to determine if we don't need imports.
            if (!content.startsWith("No")) {
                // Splitting the string into an array using the newline character
                String[] items = content.split(","); // TODO: Need to test with more than one element in the list.
                // Creating an ArrayList from the array
                return new ArrayList<>(Arrays.asList(items));
            }

            return Collections.emptyList();
        }
    }
}
