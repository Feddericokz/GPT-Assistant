package com.github.feddericokz.gptassistant.ui.components;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class GPTAssistantToolWindowFactory implements ToolWindowFactory {

    @Getter
    private static ToolWindowContent toolWindowContent = null;

    @Override
    public void createToolWindowContent(@NotNull Project project, ToolWindow toolWindow) {
        toolWindowContent = new ToolWindowContent(project);

        ContentFactory contentFactory = ContentFactory.getInstance();
        toolWindow.getContentManager().addContent(
                contentFactory.createContent(toolWindowContent, "", false));
    }

}

