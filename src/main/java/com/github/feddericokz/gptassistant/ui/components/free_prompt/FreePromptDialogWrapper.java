package com.github.feddericokz.gptassistant.ui.components.free_prompt;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class FreePromptDialogWrapper extends DialogWrapper {

    private FreePromptPanel freePromptPanel;

    public FreePromptDialogWrapper(boolean canBeParent) {
        super(canBeParent);
        setTitle("Free Prompt");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        freePromptPanel = new FreePromptPanel();
        return freePromptPanel;
    }

    public FreePromptPanel getMainPanel() {
        return freePromptPanel;
    }

}
