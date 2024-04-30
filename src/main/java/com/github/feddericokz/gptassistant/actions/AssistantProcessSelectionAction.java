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
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.feddericokz.gptassistant.notifications.Notifications.getWarningNotification;

public class AssistantProcessSelectionAction extends AbstractProcessSelectionAction {

    private static final Logger logger = ToolWindowLogger.getInstance(AssistantProcessSelectionAction.class);

    List<AssistantResponseHandler> handlers = new ArrayList<>();

    public AssistantProcessSelectionAction() {
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

        // Need to get the names of the context classes from selection.
        List<String> selectionContextFilesUrls = getSelectionContextFiles(e);

        // Get the names of the context classes of the current file.
        List<String> fileContextFilesUrls = getFileContextFiles(e);

        Map<String, List<String>> contextFilesMap = new HashMap<>();
        contextFilesMap.put("Current file:", Collections.singletonList(currentFileUrl));
        contextFilesMap.put("Selection context files:", selectionContextFilesUrls);
        contextFilesMap.put("File context files:", fileContextFilesUrls);

        // TODO The context selector now display the urls for all these files, should be more user friendly.Dec
        // Let the user choose which classes to send for context.
        List<String> contextFiles = getUserChosenContextFiles(contextFilesMap, e);

        // Get the actual content of the classes.
        List<AbstractMap.SimpleImmutableEntry<String,String>> fileContents = getFileContentFromNames(contextFiles, e.getProject());

        return getXmlTaggedMessagesForRequest(selection, fileContents);
    }

    private List<AbstractMap.SimpleImmutableEntry<String,String>> getFileContentFromNames(List<String> fileNames, Project project) {

        return fileNames.stream()
                .map(fileUrl -> {
                    VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(fileUrl);
                    if (virtualFile != null) {
                        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                        if (psiFile != null) {
                            LanguageFileType fileType = (LanguageFileType) FileTypeManager.getInstance().getFileTypeByFile(psiFile.getVirtualFile());
                            String language = fileType.getLanguage().getID();
                            String fileContent = psiFile.getText();
                            return new AbstractMap.SimpleImmutableEntry<>(language, fileContent);
                        }
                    } else {
                        logger.warning("Unable to create virtualFile for path: " + fileUrl);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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

    public static List<String> getUserChosenContextFiles(Map<String, List<String>> contextFilesMap, AnActionEvent e) throws UserCancelledException {
        List<List<CheckboxListItem>> itemsLists = new ArrayList<>();
        List<String> titles = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : contextFilesMap.entrySet()) {
            itemsLists.add(entry.getValue().stream().map(CheckboxListItem::new).collect(Collectors.toList()));
            titles.add(entry.getKey());
        }

        Project project = e.getRequiredData(CommonDataKeys.PROJECT);

        ContextFilesSelectorDialog dialog = new ContextFilesSelectorDialog(itemsLists, titles, project);
        dialog.show();

        if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            List<List<String>> selectedValues = dialog.getSelectedValues();
            return selectedValues.stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } else {
            throw new UserCancelledException();
        }
    }

    private List<String> getSelectionContextFiles(AnActionEvent e) {
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
                .map(psiClass -> psiClass.getContainingFile().getVirtualFile().getUrl())
                .collect(Collectors.toList());
    }

    private List<String> getFileContextFiles(AnActionEvent e) {
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
                .map(psiClass -> psiClass.getContainingFile().getVirtualFile().getUrl())
                .collect(Collectors.toList());
    }

    private List<String> getXmlTaggedMessagesForRequest(String selection, List<AbstractMap.SimpleImmutableEntry<String,String>> fileContents) {

        List<String> contextMessages = fileContents.stream()
                .map(fileContent -> xmlTagText(fileContent.getValue(), "context",
                        Collections.singletonMap("language", fileContent.getKey())))
                .toList();

        List<String> requestMessages
                = new ArrayList<>(Collections.singletonList(xmlTagText(selection, "prompt",
                Collections.singletonMap("isSelection", "true"))));

        requestMessages.addAll(contextMessages);

        return requestMessages;
    }

    private String xmlTagText(String text, String tag, Map<String, String> attributes) {
        // Construct the opening tag with attributes.
        String openingTag = "<" + tag + attributes.entrySet()
                .stream()
                .map(entry -> " " + entry.getKey() + "=\"" + entry.getValue() + "\"")
                .collect(Collectors.joining("")) + ">";
        String closingTag = "</" + tag + ">";
        // Return formatted string with tags and text.
        return openingTag + System.lineSeparator() + text + System.lineSeparator() + closingTag;
    }

}
