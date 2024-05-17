package com.github.feddericokz.gptassistant.ui.components.tool_window.context;

import com.github.feddericokz.gptassistant.configuration.PluginSettings;
import com.github.feddericokz.gptassistant.context.ContextItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ContextTab extends JPanel {

    private final JTable contextTable;

    public ContextTab() {
        List<ContextItem> contextItems = PluginSettings.getInstance().getContextItems();

        // Extending column names to include a "Remove" button column for each row
        String[] columnNames = {"Item Type", "Context Path", "Remove"};

        // Refactoring loop to use foreach for better readability and performance
        Object[][] data = new Object[contextItems.size()][3];
        int i = 0;
        for (ContextItem item : contextItems) {
            data[i][0] = item.itemType().toString();
            data[i][1] = item.contextPath();
            // Initializing placeholder for the "Remove" button, actual button will be added in cell rendering
            data[i][2] = "Remove";
            i++;
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make all cells except for the "Remove" button column non-editable
                return column == 2;
            }

            @Override
            public Class getColumnClass(int columnIndex) {
                // Ensuring the "Remove" button is rendered as a button
                return columnIndex == 2 ? JButton.class : String.class;
            }
        };

        contextTable = new JTable(tableModel) {
            @Override
            public void setValueAt(Object aValue, int row, int column) {
                // If it's an action from the remove button, we don't want to set anything here.
                if (aValue instanceof String && !aValue.equals("Remove")) {
                    super.setValueAt(aValue, row, column);
                }
            }
        };

        // Adding a custom renderer and editor for the "Remove" button
        contextTable.getColumn("Remove").setCellRenderer(new RemoveButtonRenderer());
        contextTable.getColumn("Remove").setCellEditor(new RemoveButtonEditor(new JCheckBox(), contextTable));

        // Mess with heights and widths
        contextTable.setRowHeight(30);
        contextTable.getColumn("Remove").setMaxWidth(500);
        contextTable.getColumn("Remove").setMinWidth(200);
        contextTable.getColumn("Item Type").setMaxWidth(500);
        contextTable.getColumn("Item Type").setMinWidth(200);

        // Add table to this panel.
        setLayout(new BorderLayout());
        add(contextTable, BorderLayout.CENTER);

        // Set the callback to update the component.
        PluginSettings.getInstance().setUpdateContextItemsDisplayFunction(this::addContextItemToTable);
    }

    public void addContextItemToTable(ContextItem item) {
        // To ensure model updates are done on the EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            DefaultTableModel model = (DefaultTableModel) contextTable.getModel(); // Casting to DefaultTableModel to use addRow method
            model.addRow(new Object[]{item.itemType().toString(), item.contextPath()}); // Adding new item to the table
        });
    }

}
