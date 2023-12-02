package com.github.feddericokz.gptassistant.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class AssistantSettingsNotificationAction extends NotificationAction {

    private final Project project;

    public AssistantSettingsNotificationAction(Project project) {
        super("Configure now");
        this.project = project;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
        // Open the specific settings panel
        ShowSettingsUtil.getInstance().showSettingsDialog(project, "GPT Assistant Settings");
        // Expire the notification once it's clicked.
        notification.expire();
    }
}