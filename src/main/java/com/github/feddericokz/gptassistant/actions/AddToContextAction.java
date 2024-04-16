package com.github.feddericokz.gptassistant.actions;

import com.github.feddericokz.gptassistant.configuration.PluginSettings;
import com.github.feddericokz.gptassistant.context.ContextItem;
import com.github.feddericokz.gptassistant.ui.components.toolwindow.log.ToolWindowLogger;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import static com.github.feddericokz.gptassistant.context.ContextItemType.CLASS;
import static com.github.feddericokz.gptassistant.context.ContextItemType.DIRECTORY;

public class AddToContextAction extends AnAction {

    PluginSettings settings = PluginSettings.getInstance();

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ToolWindowLogger logger = new ToolWindowLogger();
        logger.log("Adding to context..", "INFO");

        PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
        String classOrPackageName = null;

        // Attempt to extract qualified name from either PsiClass or PsiPackage
        if (element instanceof PsiClass) {
            classOrPackageName = ((PsiClass) element).getQualifiedName();
            settings.addContextItem(new ContextItem(CLASS, classOrPackageName));
        } else if (element instanceof PsiDirectory) {
            classOrPackageName = ((PsiDirectory) element).getVirtualFile().getPath();
            settings.addContextItem(new ContextItem(DIRECTORY, classOrPackageName));
        }

        // New check for null to handle non-class/package elements gracefully
        if (classOrPackageName == null) {
            logger.log("Selected element is neither a class nor a directory.", "WARN");
        } else {
            // Log the name of the class or package acted upon
            logger.log("Action performed on: " + classOrPackageName, "INFO");
        }
    }

}
