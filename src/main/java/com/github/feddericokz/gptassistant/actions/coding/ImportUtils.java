package com.github.feddericokz.gptassistant.actions.coding;

import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;

public class ImportUtils {

    public static void addImportStatement(Project project, Editor editor, String importIdentifier) {
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile == null) {
            return;
        }

        ImportLanguageHandler handler = getLanguageHandler(psiFile.getLanguage());
        if (handler != null) {
            handler.addImport(psiFile, importIdentifier);
        }
    }

    public static void addImportStatement(AnActionEvent actionEvent, String importIdentifier) {
        // Get the current editor
        Editor editor = actionEvent.getRequiredData(CommonDataKeys.EDITOR);
        // Get current project.
        Project project = editor.getProject();
        // Call sibling method.
        addImportStatement(project, editor, importIdentifier);
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
