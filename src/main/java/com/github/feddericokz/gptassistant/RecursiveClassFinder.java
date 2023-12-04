package com.github.feddericokz.gptassistant;

import com.intellij.psi.*;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RecursiveClassFinder {

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
        }
    }


}
