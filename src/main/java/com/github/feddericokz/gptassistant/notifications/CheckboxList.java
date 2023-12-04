package com.github.feddericokz.gptassistant.notifications;

import com.intellij.ui.components.JBList;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

class CheckboxList extends JBList<CheckboxListItem> {
    public CheckboxList(List<CheckboxListItem> items) {
        super(new DefaultListModel<>());
        DefaultListModel<CheckboxListItem> model = (DefaultListModel<CheckboxListItem>) getModel();
        items.forEach(model::addElement);

        setCellRenderer(new CheckboxListRenderer());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = locationToIndex(e.getPoint());
                if (index != -1) {
                    CheckboxListItem item = getModel().getElementAt(index);
                    item.setSelected(!item.isSelected());
                    repaint();
                }
            }
        });
    }
}