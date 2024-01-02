package com.github.feddericokz.gptassistant.actions.code;

import com.intellij.psi.PsiFile;

public interface ImportLanguageHandler {
    void addImport(PsiFile file, String importIdentifier);
}
