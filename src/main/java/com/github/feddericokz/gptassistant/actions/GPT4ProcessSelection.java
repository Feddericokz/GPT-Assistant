package com.github.feddericokz.gptassistant.actions;

import com.github.feddericokz.gptassistant.notifications.GPTAssistantNotifications;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class GPT4ProcessSelection extends GPTProcessSelection {
    public GPT4ProcessSelection() {
        super();
    }

    @Override
    public String getModelVersion() {
        return "GPT4";
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (!isBlank(settingsService.getGpt4Model())) {
            super.actionPerformed(e);
        } else {
            Project project = e.getRequiredData(CommonDataKeys.PROJECT);
            // If its blank we do nothing and let the user know it needs to be configured.
            Notifications.Bus.notify(GPTAssistantNotifications.getMissingGPT4ModelNotification(project), project);
        }
    }
}
