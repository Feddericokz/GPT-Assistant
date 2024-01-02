package com.github.feddericokz.gptassistant.ui.components;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ContextClassesSelectorDialog extends DialogWrapper {
    private final List<CollapsiblePanel> collapsiblePanels;

    public ContextClassesSelectorDialog(List<List<CheckboxListItem>> contextItemsList, List<String> titles) {
        super(true);
        setTitle("Context Selector");

        collapsiblePanels = new ArrayList<>();
        for (int i = 0; i < contextItemsList.size(); i++) {
            collapsiblePanels.add(new CollapsiblePanel(titles.get(i), new CheckboxList(contextItemsList.get(i)), false));
        }

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        for (CollapsiblePanel collapsiblePanel : collapsiblePanels) {
            panel.add(collapsiblePanel);
        }

        return panel;
    }

    public List<List<String>> getSelectedValues() {
        return collapsiblePanels.stream()
                .map(CollapsiblePanel::getComponent)
                .map(list -> {
                    ListModel<CheckboxListItem> model = ((CheckboxList) list).getModel();
                    List<String> selectedItems = new ArrayList<>();
                    for (int i = 0; i < model.getSize(); i++) {
                        CheckboxListItem item = model.getElementAt(i);
                        if (item.isSelected()) {
                            selectedItems.add(item.toString());
                        }
                    }
                    return selectedItems;
                })
                .collect(Collectors.toList());
    }

}