package com.github.feddericokz.gptassistant.actions;

import com.github.feddericokz.gptassistant.common.Logger;
import com.github.feddericokz.gptassistant.configuration.PluginSettings;
import com.github.feddericokz.gptassistant.context.ContextItem;
import com.github.feddericokz.gptassistant.ui.components.tool_window.log.ToolWindowLogger;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import static com.github.feddericokz.gptassistant.context.ContextItemType.*;
import static com.github.feddericokz.gptassistant.notifications.Notifications.getWarningNotification;

public class AddToContextAction extends AnAction {

    private static final Logger logger = ToolWindowLogger.getInstance(AddToContextAction.class);

    PluginSettings settings = PluginSettings.getInstance();

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
        if (element == null) throw new IllegalStateException("Element should not be null.");
        String classOrPackageName = null;

        if (element instanceof PsiClass) {
            classOrPackageName = element.getContainingFile().getVirtualFile().getUrl();
            settings.addContextItem(new ContextItem(CLASS, classOrPackageName));
        } else if (element instanceof PsiDirectory) {
            classOrPackageName = ((PsiDirectory) element).getVirtualFile().getUrl();
            settings.addContextItem(new ContextItem(DIRECTORY, classOrPackageName));
        } else if (element instanceof PsiFile) {
            classOrPackageName = ((PsiFile) element).getVirtualFile().getUrl();
            settings.addContextItem(new ContextItem(FILE, classOrPackageName));
        }

        // New check for null to handle non-class/package elements gracefully
        if (classOrPackageName == null) {
            logger.warning("The selected element is neither a class, file nor a directory. Selected: " + element);
            Notifications.Bus.notify(
                    getWarningNotification(
                            "Unsupported Element Selected",
                            "The selected element is neither a class, file nor a directory. Please select a valid element."
                    ), e.getProject());
        } else {
            logger.info("Added " + classOrPackageName + " to current assistant context.");
        }
    }

}
