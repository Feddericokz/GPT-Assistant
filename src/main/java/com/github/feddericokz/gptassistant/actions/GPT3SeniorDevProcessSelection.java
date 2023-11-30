package com.github.feddericokz.gptassistant.actions;

import com.github.feddericokz.gptassistant.notifications.GPTAssistantNotifications;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class GPT3SeniorDevProcessSelection extends GPTProcessSelection {

    public static final String GPT3_SENIOR_DEV_MODEL_VERSION = "GPT3-SeniorDev";

    public GPT3SeniorDevProcessSelection() {
        super();
    }

    @Override
    public String getModelVersion() {
        return GPT3_SENIOR_DEV_MODEL_VERSION;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (!isBlank(settings.getGpt3Model())) {
            super.actionPerformed(e);
        } else {
            Project project = e.getRequiredData(CommonDataKeys.PROJECT);
            // If its blank we do nothing and let the user know it needs to be configured.
            Notifications.Bus.notify(GPTAssistantNotifications.getMissingGPT3ModelNotification(project), project);
        }
    }
}
