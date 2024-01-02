package com.github.feddericokz.gptassistant.configuration;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SettingsComponent {

    private final JPanel mainPanel;

    private final JBTextField apiKeyTextField;
    private final JBCheckBox enableReformatProcessedCodeCheckBox;
    private final JPanel assistantsPanel;

    public SettingsComponent() {

        // TODO Need to refactor this to look good.

        apiKeyTextField = new JBTextField();
        apiKeyTextField.setColumns(44);

        enableReformatProcessedCodeCheckBox = new JBCheckBox();

        assistantsPanel = createAssistantsPanel();

        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("OpenAI API Key:"), apiKeyTextField, 1, false)
                .addLabeledComponent(new JBLabel("Reformat processed code:"), enableReformatProcessedCodeCheckBox, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    private JPanel createAssistantsPanel() {
        JPanel mainPanel = new JPanel();

        JPanel row = new JPanel();

        return mainPanel;
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return apiKeyTextField;
    }

    @NotNull
    public String getApiKey() {
        return apiKeyTextField.getText();
    }

    public void setApiKey(@NotNull String newText) {
        apiKeyTextField.setText(newText);
    }

    public boolean getEnableReformatProcessedCode() {
        return enableReformatProcessedCodeCheckBox.isSelected();
    }

    public void setEnableReformatProcessedCode(boolean selected) {
        enableReformatProcessedCodeCheckBox.setSelected(selected);
    }

}
