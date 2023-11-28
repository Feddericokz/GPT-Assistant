package com.github.feddericokz.gptassistant.configuration;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class GPTAssistantConfiguration implements Configurable {

    private GPTAssistantSettingsComponent settingsComponent;

    public GPTAssistantConfiguration() {
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "GPT Assistant Configuration.";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settingsComponent = new GPTAssistantSettingsComponent();
        return settingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        GPTAssistantPluginService settingsService = GPTAssistantPluginService.getInstance();
        boolean modified = !settingsService.getApiKey().equals(settingsComponent.getApiKey());
        modified = modified || !settingsService.getGpt3Model().equals(settingsComponent.getGpt3Model());
        modified = modified || !settingsService.getGpt4Model().equals(settingsComponent.getGpt4Model());
        return modified;
    }

    @Override
    public void apply() throws ConfigurationException {
        GPTAssistantPluginService settingsService = GPTAssistantPluginService.getInstance();
        settingsService.setApiKey(settingsComponent.getApiKey());
        settingsService.setGpt3Model(settingsComponent.getGpt3Model());
        settingsService.setGpt4Model(settingsComponent.getGpt4Model());
    }

    @Override
    public void reset() {
        GPTAssistantPluginService settingsService = GPTAssistantPluginService.getInstance();
        settingsComponent.setApiKey(settingsService.getApiKey());
        settingsComponent.setGpt3Model(settingsService.getGpt3Model());
        settingsComponent.setGpt4Model(settingsService.getGpt4Model());

    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }

}
