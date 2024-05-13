package com.github.feddericokz.gptassistant.actions;

import com.github.feddericokz.gptassistant.actions.handlers.*;
import com.github.feddericokz.gptassistant.common.Logger;
import com.github.feddericokz.gptassistant.ui.components.free_prompt.FreePromptDialogWrapper;
import com.github.feddericokz.gptassistant.ui.components.tool_window.log.ToolWindowLogger;
import com.github.feddericokz.gptassistant.utils.ContextUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.github.feddericokz.gptassistant.utils.ActionsUtils.getFileContentFromNames;
import static com.github.feddericokz.gptassistant.utils.ActionsUtils.getXmlTaggedMessagesForRequest;

public class FreePromptAction extends AnAction {

    private static final Logger logger = ToolWindowLogger.getInstance(FreePromptAction.class);

    private final List<AssistantResponseHandler> handlers = new ArrayList<>();

    public FreePromptAction() {
        handlers.add(new ReplaceSelectionResponseHandler());
        // Imports response handler does not run on FreePrompt for now since it only makes sense when replacing a selection.
        //  In the future if we improve it to also contain where the import should be placed it could be enabled.
        //handlers.add(new ImportsResponseHandler());
        handlers.add(new FileCreationResponseHandler());
        handlers.add(new StepsResponseHandler());
        handlers.add(new UserRequestResponseHandler());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        FreePromptDialogWrapper dialog = new FreePromptDialogWrapper(true);
        if (dialog.showAndGet()) {
            String userPrompt = dialog.getMainPanel().getUserPrompt();
            boolean useGlobalContext = dialog.getMainPanel().isUseGlobalContext();

            AbstractAssistantAction assistantAction = new AbstractAssistantAction() {
                @Override
                protected List<AssistantResponseHandler> getResponseHandlersList() {
                    return handlers;
                }

                @Override
                protected List<String> getMessagesForRequest(AnActionEvent e, String userPrompt) {
                    return FreePromptAction.getMessagesForRequest(e, useGlobalContext, userPrompt);
                }

                @Override
                protected String getUserPrompt(@NotNull AnActionEvent e) {
                    return userPrompt;
                }
            };

            assistantAction.actionPerformed(e);
        } else {
            logger.warning("Action was cancelled by the user.");
        }
    }

    private static List<String> getMessagesForRequest(AnActionEvent e, boolean useGlobalContext, String userPrompt) {
        List<String> messagesForRequest;
        if (useGlobalContext) {
            List<String> globalContextFiles = ContextUtils.getGlobalContextValues();
            List<Map<String, String>> fileContents = getFileContentFromNames(globalContextFiles, e.getProject());
            messagesForRequest = getXmlTaggedMessagesForRequest(userPrompt, fileContents, false);
        } else {
            messagesForRequest = getXmlTaggedMessagesForRequest(userPrompt, Collections.emptyList(), false);
        }
        return messagesForRequest;
    }

}
