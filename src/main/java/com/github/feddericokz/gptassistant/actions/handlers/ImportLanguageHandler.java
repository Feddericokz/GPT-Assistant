package com.github.feddericokz.gptassistant.actions.handlers;

import com.intellij.psi.PsiFile;

public interface ImportLanguageHandler {
    void addImport(PsiFile file, String importIdentifier);
}
