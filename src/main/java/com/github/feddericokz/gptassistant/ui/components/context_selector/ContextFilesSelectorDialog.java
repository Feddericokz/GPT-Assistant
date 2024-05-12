package com.github.feddericokz.gptassistant.ui.components.context_selector;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.SeparatorComponent;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.feddericokz.gptassistant.utils.ContextUtils.getGlobalContextValues;
import static java.awt.FlowLayout.LEFT;

public class ContextFilesSelectorDialog extends DialogWrapper {

    private final List<CollapsiblePanel> collapsiblePanels;
    private JBCheckBox useGlobalContextClassesCheckbox;


    public ContextFilesSelectorDialog(List<List<CheckboxListItem>> contextItemsList, List<String> titles) {
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

        panel.add(new JSeparator());

        useGlobalContextClassesCheckbox = new JBCheckBox("Use global context");
        JPanel globalContextClassesPanel = new JPanel();
        globalContextClassesPanel.setLayout(new FlowLayout(LEFT));
        globalContextClassesPanel.add(useGlobalContextClassesCheckbox);
        globalContextClassesPanel.add(new SeparatorComponent());
        panel.add(globalContextClassesPanel);

        return panel;
    }

    public List<List<String>> getSelectedValues() {
        List<List<String>> selectedValues = collapsiblePanels.stream()
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

        if (useGlobalContextClassesCheckbox.isSelected()) {
            List<String> globalContextValues = getGlobalContextValues();
            selectedValues.add(globalContextValues);
        }

        return selectedValues;
    }



}