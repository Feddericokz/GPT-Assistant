package com.github.feddericokz.gptassistant.actions;

import com.github.feddericokz.gptassistant.ui.components.ToolWindowLogger;
import com.github.feddericokz.gptassistant.utils.ActionEventUtils;
import com.github.feddericokz.gptassistant.behaviors.BehaviorPattern;
import com.github.feddericokz.gptassistant.configuration.PluginSettings;
import com.github.feddericokz.gptassistant.utils.Logger;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.jetbrains.annotations.NotNull;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.github.feddericokz.gptassistant.utils.ActionEventUtils.getFileLanguage;
import static com.github.feddericokz.gptassistant.utils.ActionEventUtils.getSelectedText;
import static com.github.feddericokz.gptassistant.Constants.GPT3;
import static com.github.feddericokz.gptassistant.Constants.GPT4;
import static com.github.feddericokz.gptassistant.notifications.Notifications.getMissingApiKeyNotification;
import static org.apache.commons.lang3.StringUtils.isBlank;

public abstract class ProcessSelectionAction  extends AnAction {

    protected final PluginSettings settings;
    protected final BehaviorPattern behaviorPattern;
    private final Logger logger;

    private OpenAiService openAiService;

    public ProcessSelectionAction(BehaviorPattern behaviorPattern) {
        this.settings = PluginSettings.getInstance();
        this.behaviorPattern = behaviorPattern;
        this.logger = new ToolWindowLogger(); // For now just create a ToolWindowLogger
    }

    protected OpenAiService getOpenAiService() {
        if (openAiService == null) {
            // TODO What if we change API key?
            // TODO Make timeout configurable.
            openAiService = new OpenAiService(settings.getApiKey(), Duration.ofMinutes(5));
        }
        return openAiService;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (!isBlank(settings.getApiKey())) {

            String selection = getSelectedText(e);
            logger.log("Selection acquired: " + selection, "DEBUG");

            try {
                List<ChatMessage> messages = getMessagesForRequest(e, selection);

                logger.log("Initial messages obtained for the request.", "INFO");

                ChatCompletionRequest completionRequest = getChatCompletionRequestUsingModel(messages, getGPTModel());
                List<ChatCompletionChoice> choices = makeOpenAiRequest(completionRequest);
                logger.log("Request made to OpenAI and responses received.", "INFO");

                ChatMessage gptResponse = getGptResponseFromChoices(choices);
                String updateSelection = sanitizeSelection(gptResponse.getContent(), e);
                logger.log("GPT-3 response received and prepared for update: " + updateSelection, "DEBUG");

                messages.add(gptResponse);

                choices.addAll(performFolowUpRequests(e, messages));
                logger.log("All follow-up requests performed.", "INFO");

                ActionEventUtils.updateSelection(e, updateSelection);
                logger.log("Editor selection updated.", "INFO");

                performFollowUpOperations(e, choices);
                logger.log("Follow-up operations performed.", "INFO");

                reformatCodeIfEnabled(e);
            } catch (UserCancelledException ex) {
                logger.log("User cancelled the action.", "INFO");
            }
        } else {
            Project project = e.getRequiredData(CommonDataKeys.PROJECT);

            Notifications.Bus.notify(getMissingApiKeyNotification(project), project);
            logger.log("API key missing, notification sent.", "WARN");
        }
    }

    private String sanitizeSelection(String content, AnActionEvent e) {
        String language = getFileLanguage(e).toLowerCase();
        String codeBlockTag = "```" + language;
        if (content.startsWith(codeBlockTag)) {
            logger.log("Response is being sanitized..", "DEBUG");
            content = content.substring(codeBlockTag.length());
            if (content.endsWith("```")) {
                content = content.substring(0, content.length() - 3);
            }
        }
        return content;
    }

    public List<ChatCompletionChoice> makeOpenAiRequest(ChatCompletionRequest completionRequest) {
        int maxRetries = getConfiguredMaxRetries();
        int retries = 0;

        while (true) {
            try {
                return getOpenAiService().createChatCompletion(completionRequest).getChoices();
            } catch (RuntimeException e) {
                if (e.getCause() instanceof SocketTimeoutException && retries < maxRetries) {
                    retries++;
                    logger.log("Socket timeout exception occurred. Retrying... Retry count: " + retries, "ERROR");
                    try {
                        logger.log("Waiting 1 second before retrying...", "INFO");
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Thread interrupted while waiting to retry", ex);
                    }
                } else {
                    throw e;
                }
            }
        }
    }

    private int getConfiguredMaxRetries() {
        return 3; // TODO Make this configurable.
    }

    private void reformatCodeIfEnabled(AnActionEvent e) {
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);

        if (isEnableReformatSelectedCode()) {
            reformatSelection(editor);
        }
    }

    public boolean isEnableReformatSelectedCode() {
        return settings.getEnableReformatProcessedCode();
    }

    private static void reformatSelection(Editor editor) {
        // Get needed objects to work with.
        Document document = editor.getDocument();
        Project project = Objects.requireNonNull(editor.getProject());
        SelectionModel selection = editor.getSelectionModel();
        PsiFile file = Objects.requireNonNull(PsiDocumentManager.getInstance(project).getPsiFile(document));

        // Ensure the document is committed
        PsiDocumentManager.getInstance(project)
                .commitDocument(document);

        // Define the range to reformat
        TextRange rangeToReformat = new TextRange(selection.getSelectionStart(), selection.getSelectionEnd());

        // Get the CodeStyleManager instance
        CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);

        // Reformat the specified range of the document. Wrap the document change in a WriteCommandAction
        WriteCommandAction.runWriteCommandAction(project, () ->
                codeStyleManager.reformatText(file, rangeToReformat.getStartOffset(), rangeToReformat.getEndOffset())
        );
    }

    protected abstract void performFollowUpOperations(AnActionEvent e, List<ChatCompletionChoice> choices);

    protected abstract Collection<? extends ChatCompletionChoice> performFolowUpRequests(AnActionEvent e, List<ChatMessage> messages);

    protected static ChatMessage getGptResponseFromChoices(List<ChatCompletionChoice> choices) {
        // We'll only have 1 choice I assume.
        return choices.get(0).getMessage();
    }

    public abstract List<ChatMessage> getMessagesForRequest(AnActionEvent e, String selection) throws UserCancelledException;

    public abstract String getModelToUse();

    protected String getGPTModel() {
        return switch (getModelToUse()) {
            case GPT3 -> settings.getGpt3Model();
            case GPT4 -> settings.getGpt4Model();
            default -> {
                throw new IllegalStateException("Shouldn't reach this statement, something is not implemented right.");
            }
        };
    }

    protected ChatCompletionRequest getChatCompletionRequestUsingModel(List<ChatMessage> messages, String model) {
        return ChatCompletionRequest.builder()
                .messages(messages)
                .model(model)
                .build();
    }

}
