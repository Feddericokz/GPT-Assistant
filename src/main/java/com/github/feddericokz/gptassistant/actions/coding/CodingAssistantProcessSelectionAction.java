package com.github.feddericokz.gptassistant.actions.coding;

import com.github.feddericokz.gptassistant.actions.ProcessSelectionAction;
import com.github.feddericokz.gptassistant.actions.UserCancelledException;
import com.github.feddericokz.gptassistant.ui.components.CheckboxListItem;
import com.github.feddericokz.gptassistant.ui.components.ContextClassesSelectorDialog;
import com.github.feddericokz.gptassistant.utils.ActionEventUtils;
import com.github.feddericokz.gptassistant.utils.RecursiveClassFinder;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.feddericokz.gptassistant.notifications.Notifications.getWarningNotification;

public class CodingAssistantProcessSelectionAction extends ProcessSelectionAction {

    public CodingAssistantProcessSelectionAction() {
    }

    @Override
    public void doWorkWithResponse(@NotNull AnActionEvent e, List<String> assistantResponse) {
        logger.log("Updating selection...", "INFO");
        ActionEventUtils.updateSelection(e, getUpdateSelection(assistantResponse));

        logger.log("Performing follow up operations...", "INFO");
        performFollowUpOperations(e, assistantResponse);

        reformatCodeIfEnabled(e);
    }

    private void reformatCodeIfEnabled(AnActionEvent e) {
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);

        if (isEnableReformatSelectedCode()) {
            logger.log("Re-formatting code...", "INFO");
            reformatSelection(editor);
        }
    }

    private static void reformatSelection(Editor editor) {
        // Get needed objects to work with.
        Document document = editor.getDocument();
        Project project = Objects.requireNonNull(editor.getProject());
        SelectionModel selection = editor.getSelectionModel();
        PsiFile file = Objects.requireNonNull(PsiDocumentManager.getInstance(project).getPsiFile(document));

        // Ensure the document is committed
        PsiDocumentManager.getInstance(project)
                .commitDocument(document);

        // Define the range to reformat
        TextRange rangeToReformat = new TextRange(selection.getSelectionStart(), selection.getSelectionEnd());

        // Get the CodeStyleManager instance
        CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);

        // Reformat the specified range of the document. Wrap the document change in a WriteCommandAction
        WriteCommandAction.runWriteCommandAction(project, () ->
                codeStyleManager.reformatText(file, rangeToReformat.getStartOffset(), rangeToReformat.getEndOffset())
        );
    }

    protected void performFollowUpOperations(AnActionEvent e, List<String> assistantResponse) {
        List<String> importStatements = getImportsList(assistantResponse);

        // TODO If there's no import statements, we can probably recover by making another request.

        // Update imports if needed.
        if (!importStatements.isEmpty()) {
            importStatements.forEach(importStatement -> {
                ImportUtils.addImportStatement(e, importStatement);
            });
        }
    }

    private static List<String> getImportsList(List<String> assistantResponse) {
        String imports = assistantResponse.stream()
                .filter(response -> response.contains("<imports>"))
                .map(response -> response.substring(response.indexOf("<imports>") + "<imports>".length(), response.indexOf("</imports>")))
                .findFirst()
                .orElse("");
        return Arrays.asList(imports.split(","));
    }

    private String getUpdateSelection(List<String> assistantResponse) {
        for (String response : assistantResponse) {
            if (response.contains("<response>")) {
                int start = response.indexOf("<response>") + "<response>".length();
                int end = response.indexOf("</response>");
                return response.substring(start, end);
            }
        }
        return null;
    }

    @Override
    public List<String> getMessagesForRequest(AnActionEvent e, String selection) throws UserCancelledException {
        // Need to get the name of the current class.
        String currentClassName = getCurrentClassName(e);

        // Need to get the names of the context classes from selection.
        List<String> selectionContextClasses = getSelectionContextClasses(e);

        // Get the names of the context classes of the current file.
        List<String> fileContextClasses = getFileContextClasses(e);

        Map<String, List<String>> contextClassesMap = new HashMap<>();
        contextClassesMap.put("Current class:", Collections.singletonList(currentClassName));
        contextClassesMap.put("Selection context classes:", selectionContextClasses);
        contextClassesMap.put("File context classes:", fileContextClasses);

        // Let the user choose which classes to send for context.
        List<String> contextClasses = getUserChoosenContextClasses(contextClassesMap, e);

        // Get the actual content of the classes.
        List<String> classContents = getClassContentFromNames(contextClasses, e.getProject());

        return getXmlTaggedMessagesForRequest(selection, classContents);
    }

    private List<String> getClassContentFromNames (List<String> classNames, Project project){
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);

        return classNames.stream()
                .map(className -> javaPsiFacade.findClass(className, scope))
                .filter(Objects::nonNull)
                .map(PsiClass::getContainingFile)
                .filter(Objects::nonNull)
                .map(PsiFile::getText)
                .collect(Collectors.toList());
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

    public static List<String> getUserChoosenContextClasses(Map<String, List<String>> contextClassesMap, AnActionEvent e) throws UserCancelledException {
        List<List<CheckboxListItem>> itemsLists = new ArrayList<>();
        List<String> titles = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : contextClassesMap.entrySet()) {
            itemsLists.add(entry.getValue().stream().map(CheckboxListItem::new).collect(Collectors.toList()));
            titles.add(entry.getKey());
        }

        Project project = e.getRequiredData(CommonDataKeys.PROJECT);

        ContextClassesSelectorDialog dialog = new ContextClassesSelectorDialog(itemsLists, titles, project);
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

    private List<String> getSelectionContextClasses(AnActionEvent e) {
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
                .map(PsiClass::getQualifiedName)
                .collect(Collectors.toList());
    }


    private List<String> getFileContextClasses(AnActionEvent e) {
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
                .map(PsiClass::getQualifiedName)
                .collect(Collectors.toList());
    }


    private List<String> getXmlTaggedMessagesForRequest(String selection, List<String> currentFileContent) {
        List<String> contextMessages = currentFileContent.stream()
                .map(fileContent -> xmlTagText(fileContent, "context"))
                .toList();

        List<String> requestMessages
                = new ArrayList<>(Collections.singletonList(xmlTagText(selection, "selection")));

        requestMessages.addAll(contextMessages);

        return requestMessages;
    }

    private String xmlTagText(String text, String tag) {
        return String.format("<%s>%n%s%n<%1$s/>", tag, text);
    }

}
