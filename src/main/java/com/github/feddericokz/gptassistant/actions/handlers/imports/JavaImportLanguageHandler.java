package com.github.feddericokz.gptassistant.actions.handlers.imports;

import com.github.feddericokz.gptassistant.common.Logger;
import com.github.feddericokz.gptassistant.ui.components.tool_window.log.ToolWindowLogger;
import com.intellij.notification.Notifications;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.Nullable;

import static com.github.feddericokz.gptassistant.notifications.Notifications.getWarningNotification;

public class JavaImportLanguageHandler implements ImportLanguageHandler {

    private static final Logger logger = ToolWindowLogger.getInstance(JavaImportLanguageHandler.class);

    @Override
    public void addImport(PsiFile file, String importIdentifier) {
        if (!(file instanceof PsiJavaFile javaFile)) {
            // Exit if file is not Java.
            return;
        }

        // Ensure the document we're trying to modify is committed.
        commitDocument(javaFile);

        // Check if the import statement already exists
        if (importStatementAlreadyExists(javaFile, importIdentifier)) {
            // Exit if we don't need to add anything else.
            return;
        }

        // Get the import statement we want to add.
        PsiImportStatement importStatement = getPsiImportStatement(importIdentifier, javaFile.getProject());
        if (importStatement == null) {
            Notifications.Bus.notify(getWarningNotification("Import class error.", String.format("It seems " +
                            "that GPT assistant tried to import a class that's not found in the project: %s. You'll " +
                            "need to add this class in order to use provided code.", importIdentifier)));
            return;
        }

        // Add the import statement to the file
        WriteCommandAction.runWriteCommandAction(javaFile.getProject(), () -> {
            PsiImportList importList = javaFile.getImportList();
            if (importList != null) {
                importList.add(importStatement);
            }
        });
    }

    @Override
    public String getLanguageIdentifierString() {
        return "Java";
    }

    @Nullable
    private static PsiImportStatement getPsiImportStatement(String importIdentifier, Project project) {
        // Assuming importIdentifier is a fully qualified class name
        PsiClass importClass = JavaPsiFacade.getInstance(project)
                .findClass(importIdentifier, GlobalSearchScope.allScope(project));
        if (importClass == null) {
            logger.error("Unable to find PsiClass for identifier: " + importIdentifier);
            return null;
        }
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
        return elementFactory.createImportStatement(importClass);
    }

    private static void commitDocument(PsiJavaFile file) {
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(file.getProject());
        Document document = documentManager.getDocument(file);
        // Ensure the document is committed
        if (document != null) {
            documentManager.commitDocument(document);
        }
    }

    private boolean importStatementAlreadyExists(PsiJavaFile javaFile, String importIdentifier) {
        PsiImportList importList = javaFile.getImportList();
        if (importList == null) {
            return false;
        }
        for (PsiImportStatementBase importStatement : importList.getAllImportStatements()) {
            String qualifiedName = importStatement.getImportReference() != null ? importStatement.getImportReference().getQualifiedName() : null;
            if (importIdentifier.equals(qualifiedName)) {
                return true;
            }
        }
        return false;
    }

}
