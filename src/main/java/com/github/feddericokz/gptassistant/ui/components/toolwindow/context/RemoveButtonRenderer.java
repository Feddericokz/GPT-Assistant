package com.github.feddericokz.gptassistant.ui.components.toolwindow.context;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

// Custom renderer for the button in the table
class RemoveButtonRenderer extends JButton implements TableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        setText("Remove");
        return this;
    }
}
