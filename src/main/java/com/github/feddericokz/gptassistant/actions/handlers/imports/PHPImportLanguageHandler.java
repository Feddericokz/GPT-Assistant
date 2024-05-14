package com.github.feddericokz.gptassistant.actions.handlers.imports;

import com.intellij.psi.PsiFile;

public class PHPImportLanguageHandler implements ImportLanguageHandler {

    @Override
    public void addImport(PsiFile file, String importIdentifier) {
        // TODO
    }

    @Override
    public String getLanguageIdentifierString() {
        return "PHP";
    }

}
