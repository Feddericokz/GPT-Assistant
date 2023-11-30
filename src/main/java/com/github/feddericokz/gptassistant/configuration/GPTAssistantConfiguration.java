package com.github.feddericokz.gptassistant.configuration;

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
        GPTAssistantPluginSettings settingsService = GPTAssistantPluginSettings.getInstance();
        boolean modified = !settingsService.getApiKey().equals(settingsComponent.getApiKey());
        modified = modified || !settingsService.getGpt3Model().equals(settingsComponent.getGpt3Model());
        modified = modified || !settingsService.getGpt4Model().equals(settingsComponent.getGpt4Model());
        modified = modified || !settingsService.getEnableReformatProcessedCode() == settingsComponent.getEnableReformatProcessedCode();
        modified = modified || !settingsService.getSeniorDevBehaviorSystemPrompt().equals(settingsComponent.getSeniorDevBehaviorSystemPrompt());
        modified = modified || !settingsService.getImportsUserPrompt().equals(settingsComponent.getImportsUserPrompt());
        modified = modified || !settingsService.getAssistantBehaviorSystemPrompt().equals(settingsComponent.getAssistantBehaviorSystemPrompt());
        return modified;
    }

    @Override
    public void apply() throws ConfigurationException {
        GPTAssistantPluginSettings settingsService = GPTAssistantPluginSettings.getInstance();
        settingsService.setApiKey(settingsComponent.getApiKey());
        settingsService.setGpt3Model(settingsComponent.getGpt3Model());
        settingsService.setGpt4Model(settingsComponent.getGpt4Model());
        settingsService.setEnableReformatProcessedCode(settingsComponent.getEnableReformatProcessedCode());
        settingsService.setSeniorDevBehaviorSystemPrompt(settingsComponent.getSeniorDevBehaviorSystemPrompt());
        settingsService.setImportsUserPrompt(settingsComponent.getImportsUserPrompt());
        settingsService.setAssistantBehaviorSystemPrompt(settingsComponent.getAssistantBehaviorSystemPrompt());
    }

    @Override
    public void reset() {
        GPTAssistantPluginSettings settingsService = GPTAssistantPluginSettings.getInstance();
        settingsComponent.setApiKey(settingsService.getApiKey());
        settingsComponent.setGpt3Model(settingsService.getGpt3Model());
        settingsComponent.setGpt4Model(settingsService.getGpt4Model());
        settingsComponent.setEnableReformatProcessedCode(settingsService.getEnableReformatProcessedCode());
        settingsComponent.setSeniorDevBehaviorSystemPrompt(settingsService.getSeniorDevBehaviorSystemPrompt());
        settingsComponent.setImportsUserPrompt(settingsService.getImportsUserPrompt());
        settingsComponent.setAssistantBehaviorSystemPrompt(settingsService.getAssistantBehaviorSystemPrompt());
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }

}
