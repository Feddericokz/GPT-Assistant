package com.github.feddericokz.gptassistant.actions.handlers;

import com.github.feddericokz.gptassistant.ui.components.tool_window.ToolWindowContent;
import com.github.feddericokz.gptassistant.ui.components.tool_window.request_info.RequestInfoContentAware;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.github.feddericokz.gptassistant.utils.ActionsUtils.sanitizeCode;

public class FileCreationResponseHandler implements AssistantResponseHandler, RequestInfoContentAware {

    @Override
    public void handleResponse(@NotNull AnActionEvent e, @NotNull List<String> assistantResponse) {
        // Extract the <file-creation> content and path attribute from the assistant response
        List<String> fileCreationTagContentList = AssistantResponseHandler.getXmlTagContentListFromResponse(assistantResponse, "file-creation");

        StringBuilder toolWindowContentStringBuilder = new StringBuilder();

        fileCreationTagContentList.forEach(fileCreationTagContent -> {
            toolWindowContentStringBuilder.append(fileCreationTagContent).append("\n");
            if (!fileCreationTagContent.isEmpty()) {
                String filePath = AssistantResponseHandler.getXmlAttribute(fileCreationTagContent, "file-creation", "path");

                // Verify if the content and path are extracted successfully
                if (filePath != null) {
                    // Define the path where the file needs to be created
                    Path path = Paths.get(filePath);

                    // Run the action to create and write to the file
                    WriteCommandAction.runWriteCommandAction(e.getProject(), () -> {
                        try {
                            Path parentPath = path.getParent();
                            Path createFilePath;

                            // Retrieve the project's base directory from the AnActionEvent object
                            VirtualFile baseDir = e.getProject().getBaseDir();
                            Path sourcesRootPath = Paths.get(baseDir.getPath());

                            if (parentPath != null) {
                                parentPath = sourcesRootPath.resolve(parentPath);
                                Path createdDir = Files.createDirectories(parentPath);

                                if (Files.exists(createdDir)) {
                                    System.out.println("Directory exists or has been successfully created.");
                                } else {
                                    System.out.println("Failed to create the directory.");
                                }

                                createFilePath = parentPath.resolve(path.getFileName());
                            } else {
                                // Adjust file creation to occur in the sources root if the parent directory was null
                                createFilePath = sourcesRootPath.resolve(path.getFileName());
                            }

                            // Write the content into the file, excluding the tag itself to get the pure content
                            String fileContent = fileCreationTagContent.replaceFirst("<[^>]*>", "")
                                    .replaceAll("</file-creation>", "");

                            Files.writeString(createFilePath, sanitizeCode(fileContent));

                            // Refresh the filesystem to ensure the file is displayed in the project structure
                            VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);
                        } catch (IOException ex) {
                            logger.error("Error while processing file-creation response.", ex);
                        }
                    });
                }
            } else {
                logger.info("No files to create.");
            }
        });

        updateToolWindowContent(toolWindowContentStringBuilder.toString());
    }

    @Override
    public void updateContent(ToolWindowContent content, String update) {
        content.getRequestInfoTab().updateFileCreation(update);
    }

}
