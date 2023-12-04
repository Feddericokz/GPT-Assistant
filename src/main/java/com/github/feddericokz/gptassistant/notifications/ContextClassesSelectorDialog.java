package com.github.feddericokz.gptassistant.notifications;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

public class ContextClassesSelectorDialog extends DialogWrapper {
    private CheckboxList list;

    public ContextClassesSelectorDialog(List<CheckboxListItem> contextItems) {
        super(true);
        setTitle("Context Selector");
        list = new CheckboxList(contextItems);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JBScrollPane(list), BorderLayout.CENTER);
        return panel;
    }

    public List<CheckboxListItem> getSelectedValues() {
        ListModel<CheckboxListItem> model = list.getModel();
        List<CheckboxListItem> selectedItems = new ArrayList<>();
        for (int i = 0; i < model.getSize(); i++) {
            CheckboxListItem item = model.getElementAt(i);
            if (item.isSelected()) {
                selectedItems.add(item);
            }
        }
        return selectedItems;
    }
}