package com.github.feddericokz.gptassistant.actions;

import com.github.feddericokz.gptassistant.configuration.GPTAssistantPluginService;
import com.github.feddericokz.gptassistant.notifications.GPTAssistantNotifications;
import com.github.feddericokz.gptassistant.utils.ImportUtils;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.isBlank;

public abstract class GPTProcessSelection extends AnAction {

    private static final String BEHAVIOR_SYSTEM_PROMPT = "Act as a senior software developer specialized in [SPECIFY PROGRAMMING LANGUAGE]. Your task is to interpret embedded comments in provided code snippets and modify or create code accordingly. Output only the revised or newly written code without any explanations or additional commentary. Assume that each comment in the code snippet is an instruction for a modification or a feature to be implemented. Do not include import statements.";
    private static final String IMPORTS_USER_PROMPT = "List the fully qualified class names of the classes I need to import to make this code work. Don't output anything else, just a comma separated list."; // TODO: This may only work for Java.

    private static boolean enableReformatSelectedCode = true; // TODO: Should be configurable.

    protected final GPTAssistantPluginService settingsService;

    protected GPTProcessSelection() {
        this.settingsService = GPTAssistantPluginService.getInstance();
    }

    private static String getSystemPrompt(String language) {
        return BEHAVIOR_SYSTEM_PROMPT.replace("[SPECIFY PROGRAMMING LANGUAGE]", language); // TODO: SYSTEM_PROMPT Should be done configurable.
    }

    public abstract String getModelVersion();

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        // Verify it there's an API key
        if (!isBlank(settingsService.getApiKey())) {
            // Get selected text.
            String selection = getSelectedText(e);

            // Make the call to OpenIA.
            List<ChatCompletionChoice> response = getResponseFromOpenIA(selection, e);

            // TODO By this time I could check if class is present in Class Loader.

            // Extract code.
            String codeFromGPT = getUpdatedSelection(response);

            // Update selected code.
            updateSelection(e, codeFromGPT);

            // Get import statements.
            List<String> importStatements = getImportStatements(response);

            // Update imports if needed.
            if (!importStatements.isEmpty()) {
                importStatements.forEach(importStatement -> {
                    ImportUtils.addImportStatement(e, importStatement);
                });
            }

            // Reformat code if enabled.
            reformatCodeIfEnabled(e);
        } else {
            Project project = e.getRequiredData(CommonDataKeys.PROJECT);
            // If its blank we do nothing and let the user know it needs to be configured.
            Notifications.Bus.notify(GPTAssistantNotifications.getMissingApiKeyNotification(project), project);
        }
    }

    private static void reformatCodeIfEnabled(AnActionEvent e) {
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);

        if (enableReformatSelectedCode) {
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

    private List<String> getImportStatements(List<ChatCompletionChoice> response) {
        if (response.size() > 1) {
            String content = response.get(1).getMessage().getContent();

            // Naive approach to determine if we don't need imports.
            if (!content.startsWith("No")) {
                // Splitting the string into an array using the newline character
                String[] items = content.split(","); // TODO: Need to test with more than one element in the list.
                // Creating an ArrayList from the array
                return new ArrayList<>(Arrays.asList(items));
            }
        }
        return Collections.emptyList();
    }

    private String getUpdatedSelection(List<ChatCompletionChoice> response) {
        // First response should be updated code.
        return response.get(0).getMessage().getContent();
    }

    private static void updateSelection(AnActionEvent e, String codeFromGPT) {
        // Get needed objects to work with.
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        SelectionModel selection = editor.getSelectionModel();
        Document document = editor.getDocument();

        // Wrap the document change in a WriteCommandAction
        WriteCommandAction.runWriteCommandAction(e.getProject(), () ->
                document.replaceString(selection.getSelectionStart(), selection.getSelectionEnd(), codeFromGPT)
        );
    }

    private List<ChatCompletionChoice> getResponseFromOpenIA(String selection, AnActionEvent event) {
        // First get the GPT model to use.
        String gptModel = getGPTModel(); // TODO GPT model could be empty here, need to either not do anything or use a default one.

        // Get the service we'll use to make the request.
        OpenAiService service = new OpenAiService(settingsService.getApiKey()); // TODO This could be created just once.

        List<ChatMessage> messages = new ArrayList<>();

        String language = discoverLanguage(event); // TODO Need to handle case where language is not known.

        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), getSystemPrompt(language)));

        messages.add(new ChatMessage(ChatMessageRole.USER.value(), selection));

        ChatCompletionRequest completionRequest = getChatCompletionRequestUsingModel(messages, gptModel);

        List<ChatCompletionChoice> choices = service.createChatCompletion(completionRequest).getChoices();

        // Update conversation with GPT response.
        messages.add(choices.get(0).getMessage());

        // Ask for import statements that need to be added.
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), IMPORTS_USER_PROMPT));

        completionRequest = getChatCompletionRequestUsingModel(messages, gptModel); // TODO, Should have config to make second one use less expensive GPT model.

        choices.addAll(service.createChatCompletion(completionRequest).getChoices());

        return choices;
    }

    private String getGPTModel() {
        return switch (getModelVersion()) {
            case "GPT3" -> settingsService.getGpt3Model();
            case "GPT4" -> settingsService.getGpt4Model();
            default ->
                // Handle the case where the model version is not recognized
                // You could return a default model, or handle the error as appropriate
                    ""; // Or handle this case as needed
        };
    }


    private String discoverLanguage(AnActionEvent e) {
        // Get the PsiFile for the current context
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        if (psiFile != null) {
            // Get the language of the file
            return psiFile.getLanguage().getID();
        }

        return "";
    }

    private List<ChatCompletionChoice> createChatCompletion(OpenAiService service, ChatCompletionRequest request, int retries) {
        int attempts = 0;
        while (true) {
            try {
                // Attempt to call the method
                return service.createChatCompletion(request).getChoices();
            } catch (RuntimeException e) { // Hmm, I'd swear I've seen this throw a SocketTimeoutException.
                attempts++;
                if (attempts >= retries) {
                    // Exceeded max retries, rethrow the exception
                    throw e;
                }
                // Optionally add a delay here if you want to wait between retries
            }
        }
    }

    private ChatCompletionRequest getChatCompletionRequestUsingModel(List<ChatMessage> messages, String model) {
        return ChatCompletionRequest.builder()
                .messages(messages)
                .model(model)
                .build();
    }

    private static String getSelectedText(AnActionEvent e) {
        // Get the current editor
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        // Get the selection model of the editor
        SelectionModel selectionModel = editor.getSelectionModel();
        // Get the selected text
        return selectionModel.getSelectedText();
    }

}
