package com.github.feddericokz.gptassistant.ui.components.free_prompt;

import javax.swing.*;
import java.awt.*;

public class FreePromptPanel extends JPanel {

    JCheckBox useGlobalContextCheckbox = new JCheckBox("Use Global Context");
    JTextArea userPromptInput = new JTextArea();

    public FreePromptPanel() {
        super();
        // Layout setup
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Checkbox for global context selection
        JPanel useGlobalContextPanel = new JPanel();
        useGlobalContextPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        useGlobalContextPanel.add(useGlobalContextCheckbox);
        add(useGlobalContextPanel);

        add(new JSeparator());

        // Label for the text box
        JPanel promptLabelPanel = new JPanel();
        promptLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel promptLabel = new JLabel("Enter your prompt:");
        promptLabelPanel.add(promptLabel);
        add(promptLabelPanel);

        // Text box for user to enter prompt
        // TODO Should find a way to set this based on something dynamic,
        //  as it will look different for different monitor sizes.
        userPromptInput.setColumns(100);
        userPromptInput.setRows(10);
        add(userPromptInput);
    }

    public boolean isUseGlobalContext() {
        return useGlobalContextCheckbox.isSelected();
    }

    public String getUserPrompt() {
        return userPromptInput.getText();
    }

}
