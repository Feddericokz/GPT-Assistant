package com.github.feddericokz.gptassistant.configuration;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AssistantConfiguration implements Configurable {

    private SettingsComponent settingsComponent;

    public AssistantConfiguration() {
        // ..
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "GPT Assistant Configuration.";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settingsComponent = new SettingsComponent();
        return settingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        PluginSettings settingsService = PluginSettings.getInstance();
        boolean modified = !settingsService.getApiKey().equals(settingsComponent.getApiKey());
        modified = modified || !settingsService.getGpt3Model().equals(settingsComponent.getGpt3Model());
        modified = modified || !settingsService.getGpt4Model().equals(settingsComponent.getGpt4Model());
        modified = modified || !settingsService.getEnableReformatProcessedCode() == settingsComponent.getEnableReformatProcessedCode();
        return modified;
    }

    @Override
    public void apply() throws ConfigurationException {
        PluginSettings settingsService = PluginSettings.getInstance();
        settingsService.setApiKey(settingsComponent.getApiKey());
        settingsService.setGpt3Model(settingsComponent.getGpt3Model());
        settingsService.setGpt4Model(settingsComponent.getGpt4Model());
        settingsService.setEnableReformatProcessedCode(settingsComponent.getEnableReformatProcessedCode());
    }

    @Override
    public void reset() {
        PluginSettings settingsService = PluginSettings.getInstance();
        settingsComponent.setApiKey(settingsService.getApiKey());
        settingsComponent.setGpt3Model(settingsService.getGpt3Model());
        settingsComponent.setGpt4Model(settingsService.getGpt4Model());
        settingsComponent.setEnableReformatProcessedCode(settingsService.getEnableReformatProcessedCode());
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }

}
