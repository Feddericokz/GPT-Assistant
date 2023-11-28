package com.github.feddericokz.gptassistant.utils;

import com.intellij.psi.PsiFile;

interface LanguageHandler {
    void addImport(PsiFile file, String importIdentifier);
}
