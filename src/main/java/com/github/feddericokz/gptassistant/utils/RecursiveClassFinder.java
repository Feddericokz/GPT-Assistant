package com.github.feddericokz.gptassistant.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;

import java.util.HashSet;
import java.util.Set;


public class RecursiveClassFinder {

    Logger logger = Logger.getInstance(RecursiveClassFinder.class);

    private final Set<PsiClass> foundClasses = new HashSet<>();

    public Set<PsiClass> findClasses(PsiElement[] elements) {
        for (PsiElement element : elements) {
            processElement(element);
        }

        return foundClasses;
    }

    private void processElement(PsiElement element) {
        if (element instanceof PsiClass) {
            foundClasses.add((PsiClass) element);
        } else {
            try {
                for (PsiReference reference : element.getReferences()) {
                    PsiElement resolved = reference.resolve();
                    if (resolved instanceof PsiClass psiClass && !foundClasses.contains(psiClass)) {
                        foundClasses.add(psiClass);
                    } else if (resolved instanceof PsiField psiField) {
                        PsiType fieldType = psiField.getType();
                        PsiClass fieldTypeClass = PsiUtil.resolveClassInType(fieldType);
                        if (fieldTypeClass != null) {
                            foundClasses.add(fieldTypeClass);
                        }
                    } else if (resolved != null) {
                        processElement(resolved);
                    }
                }
            } catch (Throwable e) {
                logger.error("Error while scanning class recursively to get context classes.", e);
            }
        }
    }

}
