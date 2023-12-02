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

    public static Notification getMissingGPT4ModelNotification(Project project) {
        Notification notification =  NotificationGroupManager.getInstance()
                .getNotificationGroup("GPTAssistantNotificationGroup")
                .createNotification(
                        "Missing GPT4 model setting.",
                        "Please configure the exact GPT4 model to use in settings.",
                        NotificationType.ERROR);
        notification.addAction(new AssistantSettingsNotificationAction(project));
        return notification;
    }

    public static Notification getMissingGPT3ModelNotification(Project project) {
        Notification notification =  NotificationGroupManager.getInstance()
                .getNotificationGroup("GPTAssistantNotificationGroup")
                .createNotification(
                        "Missing GPT3 model setting.",
                        "Please configure the exact GPT3 model to use in settings.",
                        NotificationType.ERROR);
        notification.addAction(new AssistantSettingsNotificationAction(project));
        return notification;
    }

}
