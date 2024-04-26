package com.github.feddericokz.gptassistant.actions.handlers;

import com.github.feddericokz.gptassistant.ui.components.tool_window.ToolWindowContent;
import com.github.feddericokz.gptassistant.ui.components.tool_window.request_info.RequestInfoContentAware;
import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static com.github.feddericokz.gptassistant.actions.handlers.AssistantResponseHandler.getXmlTagContentFromResponse;

public class ImportsResponseHandler implements AssistantResponseHandler, RequestInfoContentAware {
    @Override
    public void handleResponse(@NotNull AnActionEvent e, @NotNull List<String> assistantResponse) {
        List<String> importStatements = getImportsList(assistantResponse);

        // Update imports if needed.
        if (!importStatements.isEmpty()) {
            importStatements.forEach(importStatement -> {
                addImportStatement(e, importStatement);
            });
        }

        updateToolWindowContent(AssistantResponseHandler.getXmlTagFromResponse(assistantResponse, "imports"));
    }

    @Override
    public void updateContent(ToolWindowContent content, String update) {
        content.getRequestInfoTab().updateImports(update);
    }

    private List<String> getImportsList(List<String> assistantResponse) {
        String imports = getXmlTagContentFromResponse(assistantResponse, "imports");
        return Arrays.asList(imports.split(","));
    }

    private static void addImportStatement(AnActionEvent actionEvent, String importIdentifier) {
        Editor editor = actionEvent.getRequiredData(CommonDataKeys.EDITOR);
        Project project = editor.getProject();
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());

        if (psiFile == null) {
            return;
        }

        ImportLanguageHandler handler = getLanguageHandler(psiFile.getLanguage());

        if (handler != null) {
            handler.addImport(psiFile, importIdentifier);
        }
    }

    private static ImportLanguageHandler getLanguageHandler(Language language) {
        // Return the appropriate handler based on the language
        if (language.is(JavaLanguage.INSTANCE)) {
            return new JavaImportLanguageHandler();
        }
        // ... handlers for other languages
        return null;
    }
}
