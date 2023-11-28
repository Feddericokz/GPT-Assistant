package com.github.feddericokz.gptassistant.utils;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.*;

public class ImportUtils {

    public static void addImportStatement(Project project, Editor editor, String importIdentifier) {
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile == null) {
            return;
        }

        LanguageHandler handler = getLanguageHandler(psiFile.getLanguage());
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

    private static LanguageHandler getLanguageHandler(Language language) {
        // Return the appropriate handler based on the language
        if (language.is(JavaLanguage.INSTANCE)) {
            return new JavaLanguageHandler();
        }
        // TODO ... handlers for other languages
        return null;
    }

}
