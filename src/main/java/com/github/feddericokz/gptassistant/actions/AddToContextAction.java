package com.github.feddericokz.gptassistant.actions;

import com.github.feddericokz.gptassistant.common.Logger;
import com.github.feddericokz.gptassistant.configuration.PluginSettings;
import com.github.feddericokz.gptassistant.context.ContextItem;
import com.github.feddericokz.gptassistant.ui.components.tool_window.log.ToolWindowLogger;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import static com.github.feddericokz.gptassistant.context.ContextItemType.DIRECTORY;
import static com.github.feddericokz.gptassistant.context.ContextItemType.FILE;

public class AddToContextAction extends AnAction {

    private static final Logger logger = ToolWindowLogger.getInstance(AddToContextAction.class);

    PluginSettings settings = PluginSettings.getInstance();

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        //Retrieve an array of selected virtual files
        VirtualFile[] virtualFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (virtualFiles == null || virtualFiles.length == 0) {
            logger.error("File selection is null or empty.");
            throw new IllegalStateException("Files should not be null or empty.");
        }

        logger.info(virtualFiles.length + " files selected.");
        for (VirtualFile file : virtualFiles) {
            String url = file.getUrl();
            // Determine the type for context addition
            if (file.isDirectory()) {
                logger.info("Directory detected, adding context: " + url);
                settings.addContextItem(new ContextItem(DIRECTORY, url));
            } else {
                logger.info("File detected, adding context: " + url);
                settings.addContextItem(new ContextItem(FILE, url));
            }
        }
    }

}
