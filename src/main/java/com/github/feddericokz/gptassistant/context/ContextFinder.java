package com.github.feddericokz.gptassistant.context;

import com.github.feddericokz.gptassistant.common.LanguageSpecificHandler;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.Map;

public interface ContextFinder extends LanguageSpecificHandler {
    Map<String, String> getContext(PsiElement[] psiElements);
    String getCurrentFileName(PsiFile psiFile);
}
