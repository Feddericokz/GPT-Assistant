package com.github.feddericokz.gptassistant.actions;

import com.github.feddericokz.gptassistant.actions.handlers.*;
import com.github.feddericokz.gptassistant.actions.handlers.imports.ImportsResponseHandler;
import com.github.feddericokz.gptassistant.common.Logger;
import com.github.feddericokz.gptassistant.context.ContextFinder;
import com.github.feddericokz.gptassistant.ui.components.context_selector.CheckboxListItem;
import com.github.feddericokz.gptassistant.ui.components.context_selector.ContextFilesSelectorDialog;
import com.github.feddericokz.gptassistant.ui.components.tool_window.log.ToolWindowLogger;
import com.github.feddericokz.gptassistant.utils.exceptions.UserCancelledException;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.feddericokz.gptassistant.notifications.Notifications.getWarningNotification;
import static com.github.feddericokz.gptassistant.utils.ActionsUtils.getFileContentFromNames;
import static com.github.feddericokz.gptassistant.utils.ActionsUtils.getXmlTaggedMessagesForRequest;

public class ProcessSelectionAction extends AbstractAssistantAction {

    private static final Logger logger = ToolWindowLogger.getInstance(ProcessSelectionAction.class);

    private static final ExtensionPointName<ContextFinder> extensionPointName
            = ExtensionPointName.create("com.github.feddericokz.gptassistant.contextFinder");

    private final List<AssistantResponseHandler> handlers = new ArrayList<>();

    public ProcessSelectionAction() {
        handlers.add(new ReplaceSelectionResponseHandler());
        handlers.add(new ImportsResponseHandler());
        handlers.add(new FileCreationResponseHandler());
        handlers.add(new StepsResponseHandler());
        handlers.add(new UserRequestResponseHandler());
    }

    @Override
    public List<AssistantResponseHandler> getResponseHandlersList() {
        return handlers;
    }

    @Override
    public List<String> getMessagesForRequest(AnActionEvent e, String selection) throws UserCancelledException {
        logger.debug("getMessagesForRequest: Entry - processing request with selection: " + selection);
        // Need to get the name of the current class.
        String currentFileUrl = getCurrentFileUrl(e);
        String currentFileName = getCurrentFileName(e);
        logger.debug("getMessagesForRequest: Current class URL: " + currentFileUrl + ", class name: " + currentFileName);

        // Need to get the names of the context classes from selection.
        Map<String, String> selectionContextFilesUrlsMap = getSelectionContextFilesMap(e);
        logger.debug("getMessagesForRequest: Selection context files count: " + selectionContextFilesUrlsMap.size());

        // Get the names of the context classes of the current file.
        Map<String, String> fileContextFilesUrlsMap = getFileContextFilesMap(e);
        logger.debug("getMessagesForRequest: File context files count: " + fileContextFilesUrlsMap.size());

        Map<String, Map<String, String>> contextFilesMap = new HashMap<>();
        contextFilesMap.put("Current file:", Collections.singletonMap(currentFileName, currentFileUrl));
        contextFilesMap.put("Selection context files:", selectionContextFilesUrlsMap);
        contextFilesMap.put("File context files:", fileContextFilesUrlsMap);

        // Let the user choose which classes to send for context.
        List<String> contextFiles = getUserChosenContextFiles(contextFilesMap, e);
        logger.info("getMessagesForRequest: User selected " + contextFiles.size() + " context files");

        // Get the actual content of the classes.
        List<Map<String, String>> fileContents = getFileContentFromNames(contextFiles, e.getProject());
        logger.debug("getMessagesForRequest: Obtained content for selected context files");

        List<String> messages = getXmlTaggedMessagesForRequest(selection, fileContents, true);
        logger.info("getMessagesForRequest: Returning " + messages.size() + " messages for request");
        return messages;
    }

    private String getCurrentFileUrl(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        if (psiFile == null) {
            throw new IllegalStateException("psiFile is null");
        }
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            throw new IllegalStateException("Editor is null");
        }

        return psiFile.getContainingFile().getVirtualFile().getUrl();
    }

    private String getCurrentFileName(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        if (psiFile == null) {
            throw new IllegalStateException("psiFile is null");
        }
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            throw new IllegalStateException("Editor is null");
        }

        for (ContextFinder contextFinder : extensionPointName.getExtensionList()) {
            if (contextFinder.getLanguageIdentifierString().equals(psiFile.getLanguage().getDisplayName())) {
                String fileName = contextFinder.getCurrentFileName(psiFile);
                if (fileName != null && !fileName.isBlank()) {
                    return fileName;
                }
            }
        }

        return psiFile.getContainingFile().getVirtualFile().getName();
    }

    public static List<String> getUserChosenContextFiles(Map<String, Map<String, String>> contextFilesMap, AnActionEvent e) throws UserCancelledException {
        List<List<CheckboxListItem>> itemsLists = new ArrayList<>();
        List<String> titles = new ArrayList<>();

        for (Map.Entry<String, Map<String, String>> entry : contextFilesMap.entrySet()) {
            itemsLists.add(entry.getValue().keySet()
                    .stream()
                    .sorted(String::compareTo)
                    .toList()
                    .stream()
                    .map(CheckboxListItem::new)
                    .collect(Collectors.toList()));
            titles.add(entry.getKey());
        }

        ContextFilesSelectorDialog dialog = new ContextFilesSelectorDialog(itemsLists, titles);
        dialog.show();

        // Join the maps from contextFilesMap to create a single map for easier lookup.
        Map<String, String> selectionContextFilesUrlsMap = contextFilesMap.values().stream()
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existing, replacement) -> existing));

        if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            List<List<String>> selectedValues = dialog.getSelectedValues();
            return selectedValues.stream()
                    .flatMap(Collection::stream)
                    .map(key -> {
                        // If the key is not in this map, it means it is a file obtained from the global context.
                        //  It should already be a file url, so we just return the key.
                        String value = selectionContextFilesUrlsMap.get(key);
                        if (value != null) return value;
                        return key;
                    })
                    .collect(Collectors.toList());
        } else {
            throw new UserCancelledException();
        }
    }

    private Map<String, String> getSelectionContextFilesMap(AnActionEvent e) {
        // Get the current project
        Project project = e.getProject();
        if (project == null) {
            throw new IllegalStateException("Project is null");
        }

        // Get the current editor
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            throw new IllegalStateException("Editor is null");
        }
        // Get the selection model
        SelectionModel selectionModel = editor.getSelectionModel();

        // Check if there is a selection
        if (!selectionModel.hasSelection()) {
            Notifications.Bus.notify(getWarningNotification("Empty selection.",
                    "Something needs to be selected in order to do work."));
        }

        // Get the start and end points of the selection
        int startOffset = selectionModel.getSelectionStart();
        int endOffset = selectionModel.getSelectionEnd();

        // Access the PsiFile for the current document
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile == null) {
            throw new IllegalStateException("psiFile is null");
        }

        // Get the PSI elements within the selected range
        PsiElement[] psiElements = PsiTreeUtil.collectElements(psiFile, element ->
                element.getTextRange() != null &&
                        element.getTextRange().getStartOffset() >= startOffset &&
                        element.getTextRange().getEndOffset() <= endOffset
        );

        return resolveFromPsiElements(psiFile, psiElements);
    }

    private static Map<String, String> resolveFromPsiElements(PsiFile psiFile, PsiElement[] psiElements) {
        // Resolve classes from psiElements.
        for (ContextFinder contextFinder : extensionPointName.getExtensionList()) {
            if (contextFinder.getLanguageIdentifierString().equals(psiFile.getLanguage().getDisplayName())) {
                return contextFinder.getContext(psiElements);
            }
        }

        // TODO Let the user know that no handler was found.
        return Collections.emptyMap();
    }

    private Map<String, String> getFileContextFilesMap(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            throw new IllegalStateException("Project is null");
        }

        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            throw new IllegalStateException("Editor is null");
        }

        Document document = editor.getDocument();

        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (psiFile == null) {
            throw new IllegalStateException("psiFile is null");
        }

        PsiElement[] psiElements = PsiTreeUtil.collectElements(psiFile, element -> true);

        // Resolve classes from psiElements.
        return resolveFromPsiElements(psiFile, psiElements);
    }

    @Override
    protected String getUserPrompt(@NotNull AnActionEvent e) {
        return getSelectedText(e);
    }

    private static String getSelectedText(AnActionEvent e) {
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        SelectionModel selectionModel = editor.getSelectionModel();
        return selectionModel.getSelectedText();
    }

}
