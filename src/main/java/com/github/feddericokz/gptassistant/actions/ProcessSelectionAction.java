package com.github.feddericokz.gptassistant.actions;

import com.github.feddericokz.gptassistant.actions.handlers.*;
import com.github.feddericokz.gptassistant.common.Logger;
import com.github.feddericokz.gptassistant.ui.components.context_selector.CheckboxListItem;
import com.github.feddericokz.gptassistant.ui.components.context_selector.ContextFilesSelectorDialog;
import com.github.feddericokz.gptassistant.ui.components.tool_window.log.ToolWindowLogger;
import com.github.feddericokz.gptassistant.utils.RecursiveClassFinder;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.feddericokz.gptassistant.notifications.Notifications.getWarningNotification;
import static com.github.feddericokz.gptassistant.utils.ActionsUtils.getFileContentFromNames;
import static com.github.feddericokz.gptassistant.utils.ActionsUtils.getXmlTaggedMessagesForRequest;

public class ProcessSelectionAction extends AbstractAssistantAction {

    // TODO Could use some logs in these methods not that I've moved stuff around.
    private static final Logger logger = ToolWindowLogger.getInstance(ProcessSelectionAction.class);

    List<AssistantResponseHandler> handlers = new ArrayList<>();

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
        // Need to get the name of the current class.
        String currentFileUrl = getCurrentClassUrl(e);
        String currentFileName = getCurrentClassName(e);

        // Need to get the names of the context classes from selection.
        Map<String, String> selectionContextFilesUrlsMap = getSelectionContextFilesMap(e);

        // Get the names of the context classes of the current file.
        Map<String, String> fileContextFilesUrlsMap = getFileContextFilesMap(e);

        Map<String, Map<String, String>> contextFilesMap = new HashMap<>();
        contextFilesMap.put("Current file:", Collections.singletonMap(currentFileName, currentFileUrl));
        contextFilesMap.put("Selection context files:", selectionContextFilesUrlsMap);
        contextFilesMap.put("File context files:", fileContextFilesUrlsMap);

        // Let the user choose which classes to send for context.
        List<String> contextFiles = getUserChosenContextFiles(contextFilesMap, e);

        // Get the actual content of the classes.
        List<Map<String,String>> fileContents = getFileContentFromNames(contextFiles, e.getProject());

        return getXmlTaggedMessagesForRequest(selection, fileContents, true);
    }

    private String getCurrentClassUrl(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        if (psiFile == null) {
            throw new IllegalStateException("psiFile is null");
        }
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            throw new IllegalStateException("Editor is null");
        }
        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAt = psiFile.findElementAt(offset);
        PsiClass containingClass = PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
        return containingClass != null ? containingClass.getContainingFile().getVirtualFile().getUrl() : null;
    }

    private String getCurrentClassName(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        if (psiFile == null) {
            throw new IllegalStateException("psiFile is null");
        }
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            throw new IllegalStateException("Editor is null");
        }
        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAt = psiFile.findElementAt(offset);
        PsiClass containingClass = PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
        return containingClass != null ? containingClass.getQualifiedName() : null;
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

        Project project = e.getRequiredData(CommonDataKeys.PROJECT);

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
                        // TODO Explain this.
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

        // Resolve classes from psiElements.
        Set<PsiClass> psiClasses = new RecursiveClassFinder().findClasses(psiElements);

        return psiClasses.stream()
                .collect(Collectors.toMap(
                        psiClass -> psiClass.getQualifiedName(),
                        psiClass -> psiClass.getContainingFile().getVirtualFile().getUrl()
                ));
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
        Set<PsiClass> psiClasses = new RecursiveClassFinder().findClasses(psiElements);

        return psiClasses.stream()
                .collect(Collectors.toMap(
                        psiClass -> psiClass.getQualifiedName(),
                        psiClass -> psiClass.getContainingFile().getVirtualFile().getUrl()
                ));
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
