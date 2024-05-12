package com.github.feddericokz.gptassistant.ui.components.configuration;

import com.github.feddericokz.gptassistant.configuration.PluginSettings;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class SettingsComponent {

    private final JPanel mainPanel;
    private final JBTextField apiKeyTextField;
    private final JBCheckBox enableReformatProcessedCodeCheckBox;
    private final JBTextField tokenThresholdTextField;
    private final JBTextField openIARequestTimeoutSeconds;
    private final JBTextField retrieveRunIntervalMillis;

    public SettingsComponent(PluginSettings settings) {
        // We need a field for the API Key
        apiKeyTextField = new JBTextField();
        apiKeyTextField.setColumns(44);

        // We need a checkbox to ask if we should process code.
        enableReformatProcessedCodeCheckBox = new JBCheckBox();

        tokenThresholdTextField = new JBTextField();
        tokenThresholdTextField.setColumns(6);

        openIARequestTimeoutSeconds = new JBTextField();
        openIARequestTimeoutSeconds.setColumns(6);

        retrieveRunIntervalMillis = new JBTextField();
        retrieveRunIntervalMillis.setColumns(6);

        // And show the assistants we have.
        JPanel assistantsPanel = new AssistantsPanel(settings);

        // I want a nice label that is a little bit separated from the rest.
        JBLabel assistantsLabel = new JBLabel("Assistants");
        assistantsLabel.setBorder(JBUI.Borders.empty(10));

        mainPanel = FormBuilder.createFormBuilder()
                .addComponent(getLabeledPanel("OpenAI API Key:", apiKeyTextField))
                .addComponent(getLabeledPanel("Reformat processed code:", enableReformatProcessedCodeCheckBox))
                .addComponent(getLabeledPanel("Token Threshold", tokenThresholdTextField))
                .addComponent(getLabeledPanel("OpenIA Timeout (seconds)", openIARequestTimeoutSeconds))
                .addComponent(getLabeledPanel("Retrieve Run Interval (milliseconds)", retrieveRunIntervalMillis))
                .addSeparator(10)
                .addLabeledComponent(assistantsLabel, assistantsPanel, 1, true)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    private JComponent getLabeledPanel(String label, JComponent component) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JBLabel(label));
        panel.add(component);
        return panel;
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

    public String getTokenThreshold() {
        return tokenThresholdTextField.getText();
    }

    public void setTokenThreshold(@NotNull String newText) {
        tokenThresholdTextField.setText(newText);
    }

    public String getOpenIARequestTimeoutSeconds() {
        return openIARequestTimeoutSeconds.getText();
    }

    public void setOpenIARequestTimeoutSeconds(@NotNull String newText) {
        openIARequestTimeoutSeconds.setText(newText);
    }

    public String getRetrieveRunIntervalMillis() {
        return retrieveRunIntervalMillis.getText();
    }

    public void setRetrieveRunIntervalMillis(@NotNull String newInterval) {
        retrieveRunIntervalMillis.setText(newInterval);
    }

}
