package com.github.feddericokz.gptassistant.behaviors;

import com.github.feddericokz.gptassistant.configuration.Prompts;
import com.github.feddericokz.gptassistant.utils.ImportUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;

import java.util.*;
import java.util.List;

public class SoftwareDevelopmentAssistant extends AssistantBehavior {

    public SoftwareDevelopmentAssistant() {
        this.setSystemPrompt(getConfiguredSystemPrompt());
        this.setFollowUpHandlers(getConfiguredFollowUpPrompts());
    }

    private static String getConfiguredSystemPrompt() {
        // TODO Make configurable.
        return Prompts.SOFTWARE_DEVELOPMENT_ASSISTANT_PROMPT;
    }

    private static List<FollowUpHandler> getConfiguredFollowUpPrompts() {
        return Collections.singletonList(new ImportsFollowUpHandler());
    }

    public static class ImportsFollowUpHandler implements FollowUpHandler {

        @Override
        public void handleResponse(AnActionEvent event, List<String> assistanResponse) {
            List<String> importStatements = getImportsList(assistanResponse);

            // TODO If there's no import statements, we can probably recover by making another request.

            // Update imports if needed.
            if (!importStatements.isEmpty()) {
                importStatements.forEach(importStatement -> {
                    ImportUtils.addImportStatement(event, importStatement);
                });
            }
        }

        private static List<String> getImportsList(List<String> assistantResponse) {
            String imports = assistantResponse.stream()
                    .filter(response -> response.contains("<imports>"))
                    .map(response -> response.substring(response.indexOf("<imports>") + "<imports>".length(), response.indexOf("</imports>")))
                    .findFirst()
                    .orElse("");
            return Arrays.asList(imports.split(","));
        }
    }
}
