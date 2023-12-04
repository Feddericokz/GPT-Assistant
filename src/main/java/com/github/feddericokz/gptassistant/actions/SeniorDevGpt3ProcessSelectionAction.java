package com.github.feddericokz.gptassistant.actions;

import com.github.feddericokz.gptassistant.Constants;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.github.feddericokz.gptassistant.notifications.Notifications.getMissingModelNotification;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class SeniorDevGpt3ProcessSelectionAction extends SeniorDevProcessSelectionAction {

    @Override
    public String getModelToUse() {
        return Constants.GPT3;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (!isBlank(settings.getGpt3Model())) {
            super.actionPerformed(e);
        } else {
            Project project = e.getRequiredData(CommonDataKeys.PROJECT);
            // If its blank we do nothing and let the user know it needs to be configured.
            Notifications.Bus.notify(getMissingModelNotification(project, getModelToUse()), project);
        }
    }

}
