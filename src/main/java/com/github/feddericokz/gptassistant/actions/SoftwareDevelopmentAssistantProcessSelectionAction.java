package com.github.feddericokz.gptassistant.actions;

import com.github.feddericokz.gptassistant.utils.RecursiveClassFinder;
import com.github.feddericokz.gptassistant.behaviors.SoftwareDevelopmentAssistant;
import com.github.feddericokz.gptassistant.ui.components.CheckboxListItem;
import com.github.feddericokz.gptassistant.ui.components.ContextClassesSelectorDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.*;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.theokanning.openai.assistants.Assistant;
import com.theokanning.openai.assistants.AssistantRequest;

import java.util.*;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.feddericokz.gptassistant.utils.ActionEventUtils.getFileLanguage;

public abstract class SoftwareDevelopmentAssistantProcessSelectionAction extends ProcessSelectionAction {

    public SoftwareDevelopmentAssistantProcessSelectionAction() {
        super(new SoftwareDevelopmentAssistant());
    }

    @Override
    public void createAssistantIfNotExists() {
        if (settings.getAssistantId() == null || settings.getAssistantId().isBlank()) { // TODO I don't like this.

            logger.log("Creating a new assistant as none currently exists.", "INFO");

            AssistantRequest assistantRequest = new AssistantRequest();
            assistantRequest.setName("GPT Software development assistant.");
            assistantRequest.setDescription("A GPT powered coding assistant that understands your code.");
            assistantRequest.setInstructions(assistantBehavior.getSystemPrompt());
            assistantRequest.setModel(getGPTModel()); // TODO This will create the assistant with the first model it uses.. Bug.

            Assistant assistant = getOpenAiService().createAssistant(assistantRequest);

            settings.setAssistantId(assistant.getId());
            logger.log("New assistant created with ID: " + assistant.getId(), "DEBUG");
        } else {
            logger.log("Assistant already exists with ID: " + settings.getAssistantId(), "DEBUG");
        }
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
        List<String> contextClasses = getUserChoosenContextClasses(contextClassesMap);

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

    public static List<String> getUserChoosenContextClasses(Map<String, List<String>> contextClassesMap) throws UserCancelledException {
        List<List<CheckboxListItem>> itemsLists = new ArrayList<>();
        List<String> titles = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : contextClassesMap.entrySet()) {
            itemsLists.add(entry.getValue().stream().map(CheckboxListItem::new).collect(Collectors.toList()));
            titles.add(entry.getKey());
        }

        ContextClassesSelectorDialog dialog = new ContextClassesSelectorDialog(itemsLists, titles);
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
            // TODO throw message that something must be selected.
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

    @Override
    protected void performFollowUpRequests(AnActionEvent e, List<String> assistantResponse) {
        //
    }

    @Override
    protected void performFollowUpOperations(AnActionEvent e, List<String> assistantResponse) {
        assistantBehavior.getFollowUpHandlers().forEach((followUpHandler -> {
            followUpHandler.handleResponse(e, assistantResponse);
        }));
    }

}
