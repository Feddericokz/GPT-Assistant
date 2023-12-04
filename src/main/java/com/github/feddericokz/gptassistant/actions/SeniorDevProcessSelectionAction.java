package com.github.feddericokz.gptassistant.actions;

import com.github.feddericokz.gptassistant.RecursiveClassFinder;
import com.github.feddericokz.gptassistant.behaviors.SeniorDevBehaviorPattern;
import com.github.feddericokz.gptassistant.notifications.ContextClassesSelectorDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.*;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import java.lang.IllegalStateException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.feddericokz.gptassistant.ActionEventUtils.getFileLanguage;

public abstract class SeniorDevProcessSelectionAction extends ProcessSelectionAction {

    public SeniorDevProcessSelectionAction() {
        super(new SeniorDevBehaviorPattern());
    }

    @Override
    public List<ChatMessage> getMessagesForRequest(AnActionEvent e, String selection) {

        String language = getFileLanguage(e);

        // Need to get the name of the current class.
        String currentClassName = getCurrentClassName(e);

        // Need to get the names of the context classes from selection.
        List<String> selectionContextClasses = getSelectionContextClasses(e);

        // TODO get the context classes from the file.

        // Let the user choose which classes to send for context.
        List<String> contextClasses = getUserChoosenContextClasses(currentClassName, selectionContextClasses);

        // Get the actual content of the classes.
        List<String> classContents = getClassContentFromNames(contextClasses, e.getProject());

        return getMessagesForRequest(language, selection, classContents);
    }

    List<String> getClassContentFromNames (List<String> classNames, Project project){
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

    public static List<String> getUserChoosenContextClasses(String currentClassName, List<String> contextClasses) {
        // TODO For now I just add it to the list so I can choose it, but they should be shown differently in the dialog.
        contextClasses.add(currentClassName);

        ContextClassesSelectorDialog contextSelector = new ContextClassesSelectorDialog(contextClasses);

        contextSelector.show();

        if (contextSelector.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            return contextSelector.getSelectedValues();
        } else {
            // Handle cancellation
            return Collections.emptyList(); // or an appropriate response
        }
    }

    public List<String> getSelectionContextClasses(AnActionEvent e) {

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

    private List<ChatMessage> getMessagesForRequest(String language, String selection, List<String> currentFileContent) {
        List<ChatMessage> contextMessages = currentFileContent.stream()
                .map(fileContent ->
                        new ChatMessage(ChatMessageRole.USER.value(), xmlTagText(fileContent, "context")))
                .toList();
        List<ChatMessage> requestMessages = new ArrayList<>(Arrays.asList(
                new ChatMessage(ChatMessageRole.SYSTEM.value(), getSystemPrompt(language)),
                new ChatMessage(ChatMessageRole.USER.value(), xmlTagText(selection, "selection"))
        ));
        requestMessages.addAll(contextMessages);
        return requestMessages;
    }

    private String xmlTagText(String text, String tag) {
        return String.format("<%s>%n%s%n<%1$s/>", tag, text);
    }

    private String getSystemPrompt(String language) {
        return behaviorPattern.getSystemPrompt().getPromptString().replace("[PROGRAMMING LANGUAGE]", language);
    }

    @Override
    protected void performFollowUpOperations(AnActionEvent e, List<ChatCompletionChoice> choices) {
        behaviorPattern.getFollowUpPrompts().forEach(((prompt, followUpHandler) -> {
            followUpHandler.handleResponse(e);
        }));
    }

    @Override
    public Collection<? extends ChatCompletionChoice> performFolowUpRequests(AnActionEvent e, List<ChatMessage> messages) {

        List<ChatCompletionChoice> completionChoices = new ArrayList<>();

        this.behaviorPattern.getFollowUpPrompts().forEach((prompt, followUpHandler) -> {

            // Create message with follow-up prompt.
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), prompt.getPromptString()));

            // TODO, Should have config to make follow up one use less expensive GPT model.
            ChatCompletionRequest completionRequest = getChatCompletionRequestUsingModel(messages, getGPTModel());

            ChatCompletionChoice completionChoice = makeRequestAndGetResponse(completionRequest);

            // Will be storing responses on handlers to process them later.
            followUpHandler.storeResponse(completionChoice);

            // This method returns a list of choices, building it to keep the method contract.
            completionChoices.add(completionChoice);

            // Add GPT response to messages to keep context.
            messages.add(completionChoice.getMessage());

        });

        return completionChoices;
    }

    private ChatCompletionChoice makeRequestAndGetResponse(ChatCompletionRequest completionRequest) {
        return getOpenAiService().createChatCompletion(completionRequest).getChoices().get(0);
    }
}
