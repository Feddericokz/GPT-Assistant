package com.github.feddericokz.gptassistant.actions.handlers;

import com.github.feddericokz.gptassistant.configuration.PluginSettings;
import com.github.feddericokz.gptassistant.ui.components.tool_window.ToolWindowContent;
import com.github.feddericokz.gptassistant.ui.components.tool_window.request_info.RequestInfoContentAware;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;

import static com.github.feddericokz.gptassistant.utils.ActionsUtils.sanitizeCode;

public class ReplaceSelectionResponseHandler implements AssistantResponseHandler, RequestInfoContentAware {

    @Override
    public void handleResponse(@NotNull AnActionEvent e, @NotNull List<String> assistantResponse) throws InterruptedException, InvocationTargetException {
        updateSelection(e, getUpdateSelection(assistantResponse));
        updateToolWindowContent(AssistantResponseHandler.getXmlTagFromResponse(assistantResponse, "code-replacement"));
        reformatCodeIfEnabled(e);
    }

    @Override
    public void updateContent(ToolWindowContent content, String update) {
        content.getRequestInfoTab().updateCodeReplacement(update);
    }

    private String getUpdateSelection(List<String> assistantResponse) {
        String xmlTag = AssistantResponseHandler.getXmlTagFromResponse(assistantResponse, "code-replacement");

        // Parsing it this way is simpler, had issues with Java code inside a xml tag.

        // Find the start of the <code-replacement> tag
        int start = xmlTag.indexOf("<code-replacement>");
        if (start == -1) return ""; // Return empty if tag not found

        // Adjust start to get content after the tag
        start += "<code-replacement>".length();

        // Find the end of the </code-replacement> tag
        int end = xmlTag.indexOf("</code-replacement>", start);
        if (end == -1) return ""; // Return empty if closing tag not found

        // Extract the content between the tags
        return xmlTag.substring(start, end).trim();
    }

    private void reformatCodeIfEnabled(AnActionEvent e) throws InterruptedException, InvocationTargetException {
        if (isEnableReformatSelectedCode()) {
            reformatSelection(e);
        }
    }

    public boolean isEnableReformatSelectedCode() {
        return PluginSettings.getInstance().getEnableReformatProcessedCode();
    }

    private static void reformatSelection(AnActionEvent e) throws InterruptedException, InvocationTargetException {
        // Get needed objects to work with.
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        Document document = editor.getDocument();
        Project project = Objects.requireNonNull(editor.getProject());
        SelectionModel selection = editor.getSelectionModel();
        PsiFile file = Objects.requireNonNull(PsiDocumentManager.getInstance(project).getPsiFile(document));

        ApplicationManager.getApplication().invokeLater(() -> {
            // Ensure the document is committed
            PsiDocumentManager.getInstance(project).commitDocument(document);
        }, ModalityState.defaultModalityState());

        // Define the range to reformat
        TextRange rangeToReformat = new TextRange(selection.getSelectionStart(), selection.getSelectionEnd());

        // Get the CodeStyleManager instance
        CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);

        ApplicationManager.getApplication().invokeLater(() -> {
            // Reformat the specified range of the document. Wrap the document change in a WriteCommandAction
            WriteCommandAction.runWriteCommandAction(project, () ->
                    codeStyleManager.reformatText(file, rangeToReformat.getStartOffset(), rangeToReformat.getEndOffset())
            );
        }, ModalityState.defaultModalityState());
    }

    public static void updateSelection(AnActionEvent e, String updateSelection) throws InterruptedException, InvocationTargetException {
        if (updateSelection != null) {
            String sanitizedSelection = sanitizeCode(updateSelection);
            // Get needed objects to work with.
            Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
            SelectionModel selection = editor.getSelectionModel();
            Document document = editor.getDocument();

            ApplicationManager.getApplication().invokeLater(() -> {
                // Wrap the document change in a WriteCommandAction
                WriteCommandAction.runWriteCommandAction(e.getProject(), () ->
                        document.replaceString(selection.getSelectionStart(), selection.getSelectionEnd(), sanitizedSelection)
                );
            }, ModalityState.defaultModalityState());
        }
    }



}
