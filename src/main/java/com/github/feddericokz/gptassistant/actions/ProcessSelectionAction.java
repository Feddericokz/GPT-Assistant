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
        this.logger = new ToolWindowLogger(); // For just create a ToolWindowLogger
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
            // Get selected text.
            String selection = getSelectedText(e);

            // Get first set of messages.
            List<ChatMessage> messages = getMessagesForRequest(e, selection);

            // Create and make request to OpenIA
            ChatCompletionRequest completionRequest = getChatCompletionRequestUsingModel(messages, getGPTModel());
            List<ChatCompletionChoice> choices = makeOpenAiRequest(completionRequest);
            ChatMessage gptResponse = getGptResponseFromChoices(choices);
            String updateSelection = gptResponse.getContent();

            // Update conversation with GPT response.
            messages.add(gptResponse);

            // Make all follow-up requests before updating anything.
            choices.addAll(performFolowUpRequests(e, messages));

            // Update the selection.
            ActionEventUtils.updateSelection(e, updateSelection);

            // Do follow up operations if any.
            performFollowUpOperations(e, choices);

            // Reformat
            reformatCodeIfEnabled(e);
        } else {
            Project project = e.getRequiredData(CommonDataKeys.PROJECT);

            // If its blank we do nothing and let the user know it needs to be configured.
            Notifications.Bus.notify(getMissingApiKeyNotification(project), project);
        }
    }

    public List<ChatCompletionChoice> makeOpenAiRequest(ChatCompletionRequest completionRequest) {
        int maxRetries = getConfiguredMaxRetries(); // Assuming there's a method to get configured max retries
        int retries = 0;

        while (true) {
            try {
                return getOpenAiService().createChatCompletion(completionRequest).getChoices();
            } catch (RuntimeException e) {
                if (e.getCause() instanceof SocketTimeoutException && retries < maxRetries) {
                    retries++;
                    // TODO Log this and wait before retrying.
                } else {
                    throw e; // Re-throw if it's not a SocketTimeoutException or we've exceeded the number of retries
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

    public abstract List<ChatMessage> getMessagesForRequest(AnActionEvent e, String selection);

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
