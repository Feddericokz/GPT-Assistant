package com.github.feddericokz.gptassistant.notifications;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.util.List;

public class ContextClassesSelectorDialog extends DialogWrapper {

    private JBList<String> list;

    public ContextClassesSelectorDialog(List<String> contextClasses) {
        super(true); // Use current window as parent
        setTitle("Context Selector");

        // Initialize list
        list = new JBList<>(contextClasses.toArray(new String[0]));
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        init(); // This is needed to initialize the dialog
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JBScrollPane(list), BorderLayout.CENTER);
        return panel;
    }

    public List<String> getSelectedValues() {
        return list.getSelectedValuesList();
    }
}