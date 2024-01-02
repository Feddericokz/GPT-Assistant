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

    public static Notification getMissingAssistantNotification(Project project) {
        Notification notification =  NotificationGroupManager.getInstance()
                .getNotificationGroup("GPTAssistantNotificationGroup")
                .createNotification(
                        "Select an assistant.",
                        "You need to select an assistant in order to process selection.",
                        NotificationType.WARNING);
        notification.addAction(new AssistantSettingsNotificationAction(project));
        return notification;
    }

    public static Notification getNotification(String title, String errorMessage, NotificationType type) {
        return NotificationGroupManager.getInstance()
                .getNotificationGroup("GPTAssistantNotificationGroup")
                .createNotification(title, errorMessage, type);
    }

    public static Notification getErrorNotification(String title, String errorMessage) {
        return getNotification(title, errorMessage, NotificationType.ERROR);
    }

    public static Notification getWarningNotification(String title, String errorMessage) {
        return getNotification(title, errorMessage, NotificationType.WARNING);
    }

}
