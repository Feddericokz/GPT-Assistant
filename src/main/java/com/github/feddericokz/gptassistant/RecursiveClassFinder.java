package com.github.feddericokz.gptassistant;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

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
                if (resolved != null) {
                    // Avoid re-processing elements already found
                    if (resolved instanceof PsiClass && !foundClasses.contains(resolved)) {
                        foundClasses.add((PsiClass) resolved);
                    } else {
                        processElement(resolved);
                    }
                }
            }
        }
    }

}
