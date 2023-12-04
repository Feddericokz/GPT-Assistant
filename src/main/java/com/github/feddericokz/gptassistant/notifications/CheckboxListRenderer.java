package com.github.feddericokz.gptassistant.notifications;

import javax.swing.*;
import java.awt.*;

class CheckboxListRenderer extends JCheckBox implements ListCellRenderer<CheckboxListItem> {
    @Override
    public Component getListCellRendererComponent(JList<? extends CheckboxListItem> list, CheckboxListItem value, int index, boolean isSelected, boolean cellHasFocus) {
        setSelected(value.isSelected());
        setText(value.toString());
        return this;
    }
}
