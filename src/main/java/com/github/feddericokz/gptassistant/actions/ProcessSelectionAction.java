package com.github.feddericokz.gptassistant.actions;

import com.github.feddericokz.gptassistant.behaviors.AssistantBehavior;
import com.github.feddericokz.gptassistant.configuration.PluginSettings;
import com.github.feddericokz.gptassistant.ui.components.ToolWindowLogger;
import com.github.feddericokz.gptassistant.utils.ActionEventUtils;
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
import com.theokanning.openai.messages.Message;
import com.theokanning.openai.messages.MessageRequest;
import com.theokanning.openai.runs.CreateThreadAndRunRequest;
import com.theokanning.openai.runs.Run;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.threads.ThreadRequest;
import org.jetbrains.annotations.NotNull;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.github.feddericokz.gptassistant.Constants.GPT3;
import static com.github.feddericokz.gptassistant.Constants.GPT4;
import static com.github.feddericokz.gptassistant.notifications.Notifications.getMissingApiKeyNotification;
import static com.github.feddericokz.gptassistant.utils.ActionEventUtils.getSelectedText;
import static org.apache.commons.lang3.StringUtils.isBlank;

public abstract class ProcessSelectionAction  extends AnAction {

    protected final PluginSettings settings;
    protected final AssistantBehavior assistantBehavior;
    protected final Logger logger;

    private OpenAiService openAiService;

    public ProcessSelectionAction(AssistantBehavior behaviorPattern) {
        this.settings = PluginSettings.getInstance();
        this.assistantBehavior = behaviorPattern;
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

            createAssistantIfNotExists();

            String selection = getSelectedText(e);
            logger.log("Selection acquired: " + selection, "DEBUG");

            try {
                List<String> stringMessages = getMessagesForRequest(e, selection);
                logger.log("Initial messages obtained for the request.", "INFO");

                Run assistantRun = createAssistantThreadAndRun(stringMessages);

                List<String> assistantResponse = waitUntilRunCompletesAndGetAssistantResponse(assistantRun);
                logger.log("Request made to OpenAI and responses received.", "INFO");

                logger.log("Performing follow up requests if needed.", "INFO");
                performFollowUpRequests(e, assistantResponse);

                ActionEventUtils.updateSelection(e, getUpdateSelection(assistantResponse));
                logger.log("Selection updated.", "INFO");

                logger.log("Performing follow up operations.", "INFO");
                performFollowUpOperations(e, assistantResponse);

                reformatCodeIfEnabled(e);
            } catch (UserCancelledException ex) {
                logger.log("User cancelled the action.", "INFO");
            }
        } else {
            Project project = e.getRequiredData(CommonDataKeys.PROJECT);
            Notifications.Bus.notify(getMissingApiKeyNotification(project), project);
        }
    }

    private String getUpdateSelection(List<String> assistantResponse) {
        for (String response : assistantResponse) {
            if (response.contains("<response>")) {
                int start = response.indexOf("<response>") + "<response>".length();
                int end = response.indexOf("</response>");
                return response.substring(start, end);
            }
        }
        return null;
    }

    private List<String> waitUntilRunCompletesAndGetAssistantResponse(Run assistantRun) {
        Run run;
        while (true) {
            run = getOpenAiService().retrieveRun(assistantRun.getThreadId(), assistantRun.getId());
            if ("completed".equals(run.getStatus()) || "failed".equals(run.getStatus())) {
                break;
            }
            try {
                java.lang.Thread.sleep(1000);
            } catch (InterruptedException e) {
                java.lang.Thread.currentThread().interrupt();
                throw new RuntimeException("The thread waiting for the run to complete was interrupted", e);
            }
        }

        if ("failed".equals(run.getStatus())) {
            throw new RuntimeException("The run failed to complete. Last error: " + run.getLastError());
        }

        List<Message> messageList = getOpenAiService().listMessages(run.getThreadId()).getData();

        return messageList
                .stream()
                .filter(m -> "assistant".equals(m.getRole()))
                // We're expecting a single content per message.
                .map(message -> message.getContent().get(0).getText().getValue())
                .collect(Collectors.toList());
    }

    private Run createAssistantThreadAndRun(List<String> stringMessages) {
        if (stringMessages == null || stringMessages.isEmpty()) {
            throw new IllegalArgumentException("stringMessages is null or empty");
        }

        List<MessageRequest> messageRequestList = stringMessages.stream()
                .map(msg -> MessageRequest.builder().content(msg).build())
                .collect(Collectors.toList());

        ThreadRequest threadRequest = ThreadRequest.builder()
                .messages(messageRequestList)
                .build();

        CreateThreadAndRunRequest createThreadAndRunRequest = CreateThreadAndRunRequest.builder()
                .assistantId(settings.getAssistantId())
                .thread(threadRequest)
                .build();

        Run run = getOpenAiService().createThreadAndRun(createThreadAndRunRequest);
        logger.log("Run created with id: " + run.getId(), "INFO");

        return run;
    }

    public abstract void createAssistantIfNotExists();

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
                        java.lang.Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        java.lang.Thread.currentThread().interrupt();
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

    protected abstract void performFollowUpRequests(AnActionEvent e, List<String> assistantResponse);

    protected abstract void performFollowUpOperations(AnActionEvent e, List<String> assistantResponse);

    public abstract List<String> getMessagesForRequest(AnActionEvent e, String selection) throws UserCancelledException;

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

}
