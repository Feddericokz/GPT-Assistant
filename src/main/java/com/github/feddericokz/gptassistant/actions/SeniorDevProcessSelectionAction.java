package com.github.feddericokz.gptassistant.actions;

import com.github.feddericokz.gptassistant.RecursiveClassFinder;
import com.github.feddericokz.gptassistant.behaviors.SeniorDevBehaviorPattern;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.feddericokz.gptassistant.ActionEventUtils.getFileLanguage;

public abstract class SeniorDevProcessSelectionAction extends ProcessSelectionAction {

    public SeniorDevProcessSelectionAction() {
        super(new SeniorDevBehaviorPattern());
    }

    @Override
    public List<ChatMessage> getMessagesForRequest(AnActionEvent e, String selection) {

        String language = getFileLanguage(e);

        // Get all the files we want to send for context.
        List<String> contextClasses = new ArrayList<>(List.of(getCurrentClass(e)));
        contextClasses.addAll(classesFromSelection(e));

        // TODO User should be able to choose which classes are sent as context.
        // TODO Also user should be able to choose classes found in the current class.

        return getMessagesForRequest(language, selection, contextClasses);
    }

    public List<String> classesFromSelection(AnActionEvent e) {

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

        // TODO Should refactor this to be inside of RecursiveClassFinder.
        // Try to resolve classes from field references.
        Arrays.stream(psiElements).forEach(psiElement -> {
            PsiReference[] references = psiElement.getReferences();
            Arrays.stream(references).forEach(psiReference -> {
                if (psiReference.resolve() instanceof PsiField psiField) {
                    PsiType fieldType = psiField.getType();
                    PsiClass typeClass = PsiUtil.resolveClassInType(fieldType);
                    if (typeClass != null) {
                        psiClasses.add(typeClass);
                    }
                }
            });
        });

        return psiClasses.stream()
                .map(PsiElement::getText)
                .collect(Collectors.toList());
    }

    private String getCurrentClass(AnActionEvent e) {
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

        // Access the Document object
        Document document = editor.getDocument();

        // Now you can work with the document or psiFile
        // For example, getting the entire text of the file
        return document.getText();
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
