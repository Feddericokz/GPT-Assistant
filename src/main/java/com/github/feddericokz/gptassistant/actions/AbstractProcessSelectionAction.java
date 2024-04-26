package com.github.feddericokz.gptassistant.actions;

import com.github.feddericokz.gptassistant.actions.handlers.AssistantResponseHandler;
import com.github.feddericokz.gptassistant.common.Logger;
import com.github.feddericokz.gptassistant.configuration.PluginSettings;
import com.github.feddericokz.gptassistant.ui.components.tool_window.log.ToolWindowLogger;
import com.github.feddericokz.gptassistant.utils.AssistantNotSelectedException;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.theokanning.openai.runs.Run;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.github.feddericokz.gptassistant.notifications.Notifications.getMissingApiKeyNotification;
import static com.github.feddericokz.gptassistant.notifications.Notifications.getMissingAssistantNotification;
import static com.github.feddericokz.gptassistant.utils.AssistantUtils.createAssistantThreadAndRun;
import static com.github.feddericokz.gptassistant.utils.AssistantUtils.waitUntilRunCompletesAndGetAssistantResponse;
import static org.apache.commons.lang3.StringUtils.isBlank;

public abstract class AbstractProcessSelectionAction extends AnAction {

    private final Logger logger = ToolWindowLogger.getInstance(AbstractProcessSelectionAction.class);

    protected final PluginSettings settings = PluginSettings.getInstance();

    protected abstract List<String> getMessagesForRequest(AnActionEvent e, String selection) throws UserCancelledException;

    public abstract List<AssistantResponseHandler> getResponseHandlersList();


    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        logger.info("Starting process selection action.");
        if (!isBlank(settings.getApiKey())) {
            String selection = getSelectedText(e);
            try {
                List<String> stringMessages = getMessagesForRequest(e, selection);
                Run assistantRun = createAssistantThreadAndRun(stringMessages);
                logger.debug("Assistant run created successfully.");

                List<String> assistantResponse = waitUntilRunCompletesAndGetAssistantResponse(assistantRun);
                logger.debug("Assistant response received.");

                // Now I need to hand this off to handlers to do their thing.
                getResponseHandlersList().forEach(assistantResponseHandler -> {
                    assistantResponseHandler.handleResponse(e, assistantResponse);
                });
                logger.info("Responses handled successfully.");
            } catch (UserCancelledException ex) {
                logger.error("Action was cancelled by the user.", ex);
                // TODO Notify the user some other way, maybe a dialog, that it was cancelled.
            } catch (AssistantNotSelectedException ex) {
                logger.error("No assistant selected for the project.", ex);
                Project project = e.getRequiredData(CommonDataKeys.PROJECT);
                Notifications.Bus.notify(getMissingAssistantNotification(project), project);
            }
        } else {
            logger.info("API Key is missing.");
            Project project = e.getRequiredData(CommonDataKeys.PROJECT);
            Notifications.Bus.notify(getMissingApiKeyNotification(project), project);
        }
    }


    private static String getSelectedText(AnActionEvent e) {
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        SelectionModel selectionModel = editor.getSelectionModel();
        return selectionModel.getSelectedText();
    }

}
