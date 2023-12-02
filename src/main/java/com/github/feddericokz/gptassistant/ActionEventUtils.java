package com.github.feddericokz.gptassistant;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.psi.PsiFile;

public class ActionEventUtils {

    public static String getSelectedText(AnActionEvent e) {
        // Get the current editor
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        // Get the selection model of the editor
        SelectionModel selectionModel = editor.getSelectionModel();
        // Get the selected text
        return selectionModel.getSelectedText();
    }

    public static String getFileLanguage(AnActionEvent e) {
        // Get the PsiFile for the current context
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        if (psiFile != null) {
            // Get the language of the file
            return psiFile.getLanguage().getID();
        }

        throw new IllegalStateException("All the files should have a language even if it's text, you should not be seeing this message.");
    }

    public static void updateSelection(AnActionEvent e, String updateSelection) {
        // Get needed objects to work with.
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        SelectionModel selection = editor.getSelectionModel();
        Document document = editor.getDocument();

        // Wrap the document change in a WriteCommandAction
        WriteCommandAction.runWriteCommandAction(e.getProject(), () ->
                document.replaceString(selection.getSelectionStart(), selection.getSelectionEnd(), updateSelection)
        );
    }

}
