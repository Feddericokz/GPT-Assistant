package com.github.feddericokz.gptassistant.utils;

import com.github.feddericokz.gptassistant.common.Logger;
import com.github.feddericokz.gptassistant.ui.components.tool_window.log.ToolWindowLogger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

import java.util.*;
import java.util.stream.Collectors;

public class ActionsUtils {

    private static final Logger logger = ToolWindowLogger.getInstance(ActionsUtils.class);

    public static List<Map<String,String>> getFileContentFromNames(List<String> fileNames, Project project) {

        return fileNames.stream()
                .map(fileUrl -> {
                    logger.debug("fileUrl: " + fileUrl);
                    if (fileUrl != null) {
                        VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(fileUrl);
                        if (virtualFile != null) {
                            PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                            if (psiFile != null) {
                                String fileContent = psiFile.getText();
                                FileType fileType = FileTypeManager.getInstance().getFileTypeByFile(psiFile.getVirtualFile());
                                Map<String, String> returnMap = new HashMap<>();
                                if (fileType instanceof LanguageFileType) {
                                    LanguageFileType languageFileType = (LanguageFileType) FileTypeManager.getInstance().getFileTypeByFile(psiFile.getVirtualFile());
                                    String language = languageFileType.getLanguage().getID();

                                    returnMap.put("language", language);
                                } else {
                                    returnMap.put("language", "unknown");
                                }
                                returnMap.put("content", fileContent);
                                returnMap.put("fileUrl", fileUrl);
                                return returnMap;
                            }
                        } else {
                            logger.warning("Unable to create virtualFile for path: " + fileUrl);
                        }
                    }
                    logger.debug("Returning null..");
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static List<String> getXmlTaggedMessagesForRequest(String userPrompt, List<Map<String,String>> fileContents, boolean isSelection) {

        List<String> contextMessages = fileContents.stream()
                .map(fileContentMap -> {
                    Map<String, String> attributes = new HashMap<>();
                    attributes.put("language", fileContentMap.get("language"));
                    attributes.put("fileUrl", fileContentMap.get("fileUrl"));
                    return xmlTagText(fileContentMap.get("content"), "context", attributes);
                })
                .toList();

        List<String> requestMessages
                = new ArrayList<>(Collections.singletonList(xmlTagText(userPrompt, "prompt",
                Collections.singletonMap("isSelection", String.valueOf(isSelection)))));

        requestMessages.addAll(contextMessages);

        return requestMessages;
    }

    public static String xmlTagText(String text, String tag, Map<String, String> attributes) {
        // Construct the opening tag with attributes.
        String openingTag = "<" + tag + attributes.entrySet()
                .stream()
                .map(entry -> " " + entry.getKey() + "=\"" + entry.getValue() + "\"")
                .collect(Collectors.joining("")) + ">";
        String closingTag = "</" + tag + ">";
        // Return formatted string with tags and text.
        return openingTag + System.lineSeparator() + text + System.lineSeparator() + closingTag;
    }

    public static String sanitizeCode(String updateSelection) {
        return updateSelection.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&");
    }

}
