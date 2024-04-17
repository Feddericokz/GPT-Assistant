package com.github.feddericokz.gptassistant.ui.components.context.selector;

import com.intellij.ui.components.JBCheckBox;

import javax.swing.*;
import java.awt.*;

class CheckboxListRenderer extends JBCheckBox implements ListCellRenderer<CheckboxListItem> {

    @Override
    public Component getListCellRendererComponent(JList<? extends CheckboxListItem> list, CheckboxListItem value, int index, boolean isSelected, boolean cellHasFocus) {
        setSelected(value.isSelected());
        setText(value.toString());
        return this;
    }

}
