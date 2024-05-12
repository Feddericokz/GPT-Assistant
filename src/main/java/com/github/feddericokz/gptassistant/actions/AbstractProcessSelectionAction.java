package com.github.feddericokz.gptassistant.actions;

import com.github.feddericokz.gptassistant.actions.handlers.AssistantResponseHandler;
import com.github.feddericokz.gptassistant.common.Logger;
import com.github.feddericokz.gptassistant.configuration.PluginSettings;
import com.github.feddericokz.gptassistant.ui.components.tool_window.log.ToolWindowLogger;
import com.github.feddericokz.gptassistant.utils.AssistantNotSelectedException;
import com.github.feddericokz.gptassistant.utils.TokenCalculator;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.theokanning.openai.runs.Run;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

import static com.github.feddericokz.gptassistant.notifications.Notifications.*;
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

                new Thread(() -> {
                    try {
                        Run assistantRun = createAssistantThreadAndRun(stringMessages);
                        logger.debug("Assistant run created successfully.");

                        int tokenCount = calculateTokenCount(stringMessages);
                        int tokenThreshold = settings.getTokenThreshold();

                        if (tokenCount > tokenThreshold && tokenThreshold != 0) {
                            int answer = JOptionPane.showConfirmDialog(null,
                                    "About to send " + tokenCount + " tokens, do you want to proceed?",
                                    "Token Threshold Exceeded",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.WARNING_MESSAGE);

                            if (answer != JOptionPane.YES_OPTION) {
                                Notifications.Bus.notify(getInfoNotification("Operation cancelled.", "Operation cancelled by the user."));
                                logger.info("Operation cancelled by the user.");
                                return; // Stop further execution
                            }
                        }

                        Notifications.Bus.notify(getInfoNotification("Tokens.", tokenCount + " sent."));

                        List<String> assistantResponse = waitUntilRunCompletesAndGetAssistantResponse(assistantRun);
                        logger.debug("Assistant response received.");

                        // Now I need to hand this off to handlers to do their thing on the UI thread.
                        SwingUtilities.invokeLater(() -> {
                            getResponseHandlersList().forEach(assistantResponseHandler -> {
                                assistantResponseHandler.handleResponse(e, assistantResponse);
                            });
                            logger.info("Responses handled successfully.");
                        });
                    } catch (AssistantNotSelectedException ex) {
                        logger.error("No assistant selected for the project.", ex);
                        Project project = e.getRequiredData(CommonDataKeys.PROJECT);
                        Notifications.Bus.notify(getMissingAssistantNotification(project), project);
                    } catch (Exception ex) {
                        Notifications.Bus.notify(getErrorNotification("OpenAI API error.", ex.getMessage()));
                        logger.error("Error while processing selection", ex);
                    }
                }).start();

            } catch (UserCancelledException ex) {
                logger.warning("Action was cancelled by the user.", ex);
            } catch (Exception ex1) {
                logger.error("Error while processing request: " + ex1.getMessage(), ex1);
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

    private int calculateTokenCount(List<String>messages){
        TokenCalculator tokenCalculator = new TokenCalculator();
        int totalTokens = 0;
        for (String message : messages) {
            int tokens = tokenCalculator.calculateTokens(message);
            logger.debug("Calculated tokens for message: " + tokens);
            totalTokens += tokens;
        }
        logger.info("Total tokens for all messages: " + totalTokens);
        return totalTokens;
    }

}
