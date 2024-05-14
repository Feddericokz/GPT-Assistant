package com.github.feddericokz.gptassistant.actions.handlers.imports;

import com.github.feddericokz.gptassistant.actions.handlers.AssistantResponseHandler;
import com.github.feddericokz.gptassistant.ui.components.tool_window.ToolWindowContent;
import com.github.feddericokz.gptassistant.ui.components.tool_window.request_info.RequestInfoContentAware;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.feddericokz.gptassistant.actions.handlers.AssistantResponseHandler.getXmlTagContentFromResponse;

public class ImportsResponseHandler implements AssistantResponseHandler, RequestInfoContentAware {

    private static final ExtensionPointName<ImportLanguageHandler> extensionPointName
            = ExtensionPointName.create("com.github.feddericokz.gptassistant.importLanguageHandler");

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
        String imports = getXmlTagContentFromResponse(assistantResponse, "imports").trim();
        return Arrays.stream(imports.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
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
        for (ImportLanguageHandler importLanguageHandler : extensionPointName.getExtensionList()) {
            if (importLanguageHandler.getLanguageIdentifierString().equals(language.getDisplayName())) {
                return importLanguageHandler;
            }
        }

        // ... if not handler was found.
        return null;
    }
}
