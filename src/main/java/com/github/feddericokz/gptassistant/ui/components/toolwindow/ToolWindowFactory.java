package com.github.feddericokz.gptassistant.ui.components.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.ContentFactory;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class ToolWindowFactory implements com.intellij.openapi.wm.ToolWindowFactory {

    @Getter
    private static ToolWindowContent toolWindowContent = null;

    @Override
    public void createToolWindowContent(@NotNull Project project, ToolWindow toolWindow) {
        toolWindowContent = new ToolWindowContent();

        ContentFactory contentFactory = ContentFactory.getInstance();

        toolWindow.getContentManager().addContent(
                contentFactory.createContent(toolWindowContent, "", false)
        );
    }

}

