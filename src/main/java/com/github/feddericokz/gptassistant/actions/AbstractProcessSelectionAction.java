package com.github.feddericokz.gptassistant.actions;

import com.github.feddericokz.gptassistant.actions.handlers.AssistantResponseHandler;
import com.github.feddericokz.gptassistant.configuration.PluginSettings;
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

    protected final PluginSettings settings = PluginSettings.getInstance();

    protected abstract List<String> getMessagesForRequest(AnActionEvent e, String selection) throws UserCancelledException;

    public abstract List<AssistantResponseHandler> getResponseHandlersList();

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (!isBlank(settings.getApiKey())) {
            String selection = getSelectedText(e);
            try {
                List<String> stringMessages = getMessagesForRequest(e, selection);
                Run assistantRun = createAssistantThreadAndRun(stringMessages);
                List<String> assistantResponse = waitUntilRunCompletesAndGetAssistantResponse(assistantRun);

                // Now I need to hand this off to handlers to do their thing.
                getResponseHandlersList().forEach(assistantResponseHandler -> {
                    assistantResponseHandler.handleResponse(e, assistantResponse);
                });
            } catch (UserCancelledException ex) {
                // TODO Notify the user some other way, maybe a dialog, that it was cancelled.
            } catch (AssistantNotSelectedException ex) {
                Project project = e.getRequiredData(CommonDataKeys.PROJECT);
                Notifications.Bus.notify(getMissingAssistantNotification(project), project);
            }
        } else {
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
