package com.github.feddericokz.gptassistant.ui.components;

import com.github.feddericokz.gptassistant.configuration.PluginSettings;
import com.github.feddericokz.gptassistant.context.ContextItem;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextArea;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;

import static java.awt.FlowLayout.LEFT;


public class ToolWindowContent extends JPanel {

    private final JBTextArea logArea;
    private final JComponent contextArea;
    private final JTabbedPane tabbedPane;

    private final PluginSettings settings = PluginSettings.getInstance();
    private final HashMap<ContextItem, JComponent> contextComponentsMap = new HashMap<>();

    public ToolWindowContent(Project project) {
        // Initialize components
        logArea = createLogComponent();
        JBScrollPane logScrollPane = new JBScrollPane(logArea);

        contextArea = createContextComponent();
        JBScrollPane contextScrollPane = new JBScrollPane(contextArea);

        // Initialize tabbed pane and add tabs
        tabbedPane = new JBTabbedPane();
        tabbedPane.addTab("Logs", logScrollPane);
        tabbedPane.addTab("Context", contextScrollPane);

        // Configure panel layout and add the tabbed pane
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);

        // Set the callback to update the component.
        settings.setUpdateContextItemsDisplayFunction(this::addToContextComponentsList);
    }

    private JBTextArea createLogComponent() {
        // Creates and configures the log component
        JBTextArea logArea = new JBTextArea();
        logArea.setEditable(false); // Make the log area read-only
        return logArea;
    }



    private JComponent createContextComponent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Using BoxLayout for vertical layout

        List<ContextItem> contextItems = settings.getContextItems();

        contextItems.stream()
                .map(this::getContextItemComponent)
                .forEach(panel::add);

        return panel;
    }


    public void addToContextComponentsList(ContextItem item) {
        // Now saving the component in a map after adding it to `contextArea` for future reference.
        JComponent component = getContextItemComponent(item);
        contextArea.add(component);
        contextComponentsMap.put(item, component);
    }

    public static class ContextItemComponent extends JPanel {
        public ContextItemComponent(ContextItem item, ActionListener actionListener) {
            //this.item = item;
            this.setLayout(new FlowLayout(LEFT));

            JLabel contextItemLabel = new JLabel(item.itemType() + ": " + item.contexPath());
            this.add(contextItemLabel);

            JButton removeButton = new JButton("Remove");
            removeButton.addActionListener(actionListener);
            this.add(removeButton);
        }
    }

    @NotNull
    private ContextItemComponent getContextItemComponent(ContextItem item) {
        return new ContextItemComponent(item, e -> {
            // Remove from context.
            PluginSettings.getInstance().getContextItems().remove(item);
            // Remove from list.
            JComponent removeComponent = contextComponentsMap.get(item);
            contextArea.remove(removeComponent);
            contextComponentsMap.remove(item);
        });
    }

    public void appendLogMessage(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength()); // Auto-scroll to the bottom
    }
}
