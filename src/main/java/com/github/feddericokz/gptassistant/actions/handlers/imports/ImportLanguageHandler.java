package com.github.feddericokz.gptassistant.actions.handlers.imports;

import com.intellij.psi.PsiFile;

public interface ImportLanguageHandler {
    void addImport(PsiFile file, String importIdentifier);

    String getLanguageIdentifierString();
}
