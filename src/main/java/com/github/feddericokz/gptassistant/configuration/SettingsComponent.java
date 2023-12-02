package com.github.feddericokz.gptassistant.configuration;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SettingsComponent {

    private final JPanel mainPanel;

    private JBTextField apiKeyTextField = new JBTextField();
    private JBTextField gpt3ModelTextField = new JBTextField();
    private JBTextField gpt4ModelTextField = new JBTextField();
    private JBCheckBox enableReformatProcessedCodeCheckBox = new JBCheckBox();


    public SettingsComponent() {

        // TODO Need to refactor this to look good.

        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("OpenAI API Key:"), apiKeyTextField, 1, false)
                .addLabeledComponent(new JBLabel("GPT3 model:"), gpt3ModelTextField, 1, false)
                .addLabeledComponent(new JBLabel("GPT4 model:"), gpt4ModelTextField, 1, false)
                .addLabeledComponent(new JBLabel("Reformat processed code:"), enableReformatProcessedCodeCheckBox, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
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

    @NotNull
    public String getGpt3Model() {
        return gpt3ModelTextField.getText();
    }

    public void setGpt3Model(@NotNull String newText) {
        gpt3ModelTextField.setText(newText);
    }

    @NotNull
    public String getGpt4Model() {
        return gpt4ModelTextField.getText();
    }

    public void setGpt4Model(@NotNull String newText) {
        gpt4ModelTextField.setText(newText);
    }

    public boolean getEnableReformatProcessedCode() {
        return enableReformatProcessedCodeCheckBox.isSelected();
    }

    public void setEnableReformatProcessedCode(boolean selected) {
        enableReformatProcessedCodeCheckBox.setSelected(selected);
    }

}
