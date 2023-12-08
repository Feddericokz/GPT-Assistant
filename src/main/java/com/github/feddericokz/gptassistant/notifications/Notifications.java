package com.github.feddericokz.gptassistant.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

public class Notifications {

    public static Notification getMissingApiKeyNotification(Project project) {
        Notification notification =  NotificationGroupManager.getInstance()
                .getNotificationGroup("GPTAssistantNotificationGroup")
                .createNotification(
                        "OpenAI API Key missing.",
                        "Please configure the API key in the plugin settings.",
                        NotificationType.ERROR);
        notification.addAction(new AssistantSettingsNotificationAction(project));
        return notification;
    }

    // TODO might not need this anymore, if we replace specific actions with configurable assistants per model.
    public static Notification getMissingModelNotification(Project project, String modelVersion) {
        String title = "Missing " + modelVersion + " model setting.";
        String content = "Please configure the exact " + modelVersion + " model to use in settings.";
        Notification notification = NotificationGroupManager.getInstance()
                .getNotificationGroup("GPTAssistantNotificationGroup")
                .createNotification(title, content, NotificationType.ERROR);
        notification.addAction(new AssistantSettingsNotificationAction(project));
        return notification;
    }

}
