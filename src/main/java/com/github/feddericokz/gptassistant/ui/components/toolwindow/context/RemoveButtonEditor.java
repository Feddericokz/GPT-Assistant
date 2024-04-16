package com.github.feddericokz.gptassistant.ui.components.toolwindow.context;

import com.github.feddericokz.gptassistant.configuration.PluginSettings;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// Custom editor for handling button click events
class RemoveButtonEditor extends DefaultCellEditor {
    protected JButton button;
    private final String label = "Remove";
    private boolean isPushed;
    private int rowNumber;
    private final JTable table;

    public RemoveButtonEditor(JCheckBox checkBox, JTable table) {
        super(checkBox);
        this.table = table;
        button = new JButton(label);
        button.addActionListener(e -> fireEditingStopped());
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        if (isSelected) {
            button.setForeground(table.getSelectionForeground());
            button.setBackground(table.getSelectionBackground());
        } else {
            button.setForeground(table.getForeground());
            button.setBackground(table.getBackground());
        }
        button.setText(label);
        isPushed = true;
        rowNumber = row;
        return button;
    }

    public Object getCellEditorValue() {
        if (isPushed) {
            // When the button is pushed, we need to remove the row from the table.
            String path = (String) table.getModel().getValueAt(rowNumber, 1);
            ((DefaultTableModel) table.getModel()).removeRow(rowNumber);
            PluginSettings.getInstance().removeContextItem(path);
        }
        isPushed = false;
        return label;
    }

    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }

    protected void fireEditingStopped() {
        super.fireEditingStopped();
    }
}
