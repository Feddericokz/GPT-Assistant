package com.github.feddericokz.gptassistant.actions;

import com.github.feddericokz.gptassistant.configuration.OpenAIServiceCache;
import com.github.feddericokz.gptassistant.configuration.PluginSettings;
import com.github.feddericokz.gptassistant.ui.components.ToolWindowLogger;
import com.github.feddericokz.gptassistant.utils.Logger;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.theokanning.openai.messages.Message;
import com.theokanning.openai.messages.MessageRequest;
import com.theokanning.openai.runs.CreateThreadAndRunRequest;
import com.theokanning.openai.runs.Run;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.threads.ThreadRequest;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.feddericokz.gptassistant.notifications.Notifications.getMissingApiKeyNotification;
import static com.github.feddericokz.gptassistant.notifications.Notifications.getMissingAssistantNotification;
import static com.github.feddericokz.gptassistant.utils.ActionEventUtils.getSelectedText;
import static org.apache.commons.lang3.StringUtils.isBlank;

public abstract class ProcessSelectionAction extends AnAction {

    protected final PluginSettings settings;
    protected final Logger logger;

    //private OpenAiService openAiService;

    public ProcessSelectionAction() {
        this.settings = PluginSettings.getInstance();
        this.logger = new ToolWindowLogger(); // For now just create a ToolWindowLogger
    }

    protected OpenAiService getOpenAiService() {
        return OpenAIServiceCache.getInstance().getService();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (!isBlank(settings.getApiKey())) {

            if (isAssistantSelected(e)) {

                String selection = getSelectedText(e);
                logger.log("Selection acquired: " + selection, "DEBUG");

                try {
                    List<String> stringMessages = getMessagesForRequest(e, selection);
                    logger.log("Initial messages obtained for the request.", "INFO");

                    Run assistantRun = createAssistantThreadAndRun(stringMessages);

                    List<String> assistantResponse = waitUntilRunCompletesAndGetAssistantResponse(assistantRun);
                    logger.log("Request made to OpenAI and responses received.", "INFO");

                    // Now I need to hand this off to the process selection implementation.

                    doWorkWithResponse(e, assistantResponse);
                } catch (UserCancelledException ex) {
                    logger.log("User cancelled the action.", "INFO");
                }
            } else {
                logger.log("There's no selected assistant, not processing. " +
                        "Please select an assistant in the settings panel.", "INFO");
            }
        } else {
            Project project = e.getRequiredData(CommonDataKeys.PROJECT);
            Notifications.Bus.notify(getMissingApiKeyNotification(project), project);
        }
    }

    public abstract void doWorkWithResponse(@NotNull AnActionEvent e, List<String> assistantResponse);

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
                .assistantId(settings.getSelectedAssistant().getId())
                .thread(threadRequest)
                .build();

        Run run = getOpenAiService().createThreadAndRun(createThreadAndRunRequest);
        logger.log("Run created with id: " + run.getId(), "INFO");

        return run;
    }

    public boolean isAssistantSelected(@NotNull AnActionEvent e) {
        if (settings.getSelectedAssistant() == null) {
            Project project = e.getRequiredData(CommonDataKeys.PROJECT);
            Notifications.Bus.notify(getMissingAssistantNotification(project), project);
            return false;
        }
        return true;
    }

    public boolean isEnableReformatSelectedCode() {
        return settings.getEnableReformatProcessedCode();
    }

    public abstract List<String> getMessagesForRequest(AnActionEvent e, String selection) throws UserCancelledException;

}
