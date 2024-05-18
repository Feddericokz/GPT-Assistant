package com.github.feddericokz.gptassistant.actions.handlers.imports;

import com.github.feddericokz.gptassistant.common.LanguageSpecificHandler;
import com.intellij.psi.PsiFile;

public interface ImportLanguageHandler extends LanguageSpecificHandler {
    void addImport(PsiFile file, String importIdentifier);
}
