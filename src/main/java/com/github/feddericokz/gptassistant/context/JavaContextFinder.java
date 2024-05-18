package com.github.feddericokz.gptassistant.context;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class JavaContextFinder implements ContextFinder {

    Logger logger = Logger.getInstance(JavaContextFinder.class);

    private Set<PsiClass> processElement(PsiElement element) {
        Set<PsiClass> foundClasses = new HashSet<>();
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
                        foundClasses.addAll(processElement(resolved));
                    }
                }
            } catch (Throwable e) {
                logger.error("Error while scanning class recursively to get context classes.", e);
            }
        }
        return foundClasses;
    }

    private Set<PsiClass> findClasses(PsiElement[] elements) {
        return Arrays.stream(elements)
                     .flatMap(element -> processElement(element).stream())
                     .collect(Collectors.toSet());
    }

    @Override
    public Map<String, String> getContext(PsiElement[] psiElements) {
        Set<PsiClass> psiClasses = this.findClasses(psiElements);

        return psiClasses.stream()
                .collect(Collectors.toMap(
                        PsiClass::getQualifiedName,
                        psiClass -> psiClass.getContainingFile().getVirtualFile().getUrl()
                ));
    }

    @Override
    public String getCurrentFileName(PsiFile psiFile) {
        if (psiFile instanceof PsiJavaFile javaFile) {
            String packageName = javaFile.getPackageName();
            // Assuming the file represents a single public class or interface
            String className = javaFile.getClasses()[0].getName();
            return packageName.isEmpty() ? className : packageName + "." + className;
        }
        return null;
    }

    @Override
    public String getLanguageIdentifierString() {
        return "Java";
    }
}
