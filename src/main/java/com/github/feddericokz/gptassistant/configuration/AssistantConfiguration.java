package com.github.feddericokz.gptassistant.configuration;

import com.github.feddericokz.gptassistant.ui.components.configuration.SettingsComponent;
import com.github.feddericokz.gptassistant.utils.AssistantUtils;
import com.intellij.notification.Notifications;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.theokanning.openai.ListSearchParameters;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.assistants.Assistant;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

import static com.github.feddericokz.gptassistant.notifications.Notifications.getErrorNotification;
import static com.github.feddericokz.gptassistant.notifications.Notifications.getWarningNotification;

public class AssistantConfiguration implements Configurable {

    private SettingsComponent settingsComponent;

    public AssistantConfiguration() {
        PluginSettings settingsService = PluginSettings.getInstance();
        if (!StringUtils.isBlank(settingsService.getApiKey())) { // Cannot make requests with no API keys.
            try {
                // First should query for existing assistants.
                List<Assistant> existingAssistants = queryForExistingAssistants();

                if (!existingAssistants.isEmpty()) {
                    settingsService.setAvailableAssistants(existingAssistants);

                    // Check if saved available assistant still exists and change it otherwise.
                    if (settingsService.getSelectedAssistant() != null) {
                        Assistant selectedAssistant = settingsService.getSelectedAssistant();
                        if (!existingAssistants.contains(selectedAssistant)) {
                            Notifications.Bus.notify(getWarningNotification("Unavailable assistant", "Previously selected assistant doesn't exist anymore on current account."));
                            // Setting selected assistant to null if it doesn't exist anymore.
                            settingsService.setSelectedAssistant(null);
                        }
                    }
                } else {
                    // Create a default software development assistant at start up for now.
                    Assistant defaultAssistant = AssistantUtils.createSoftwareDevelopmentAssistant();
                    settingsService.addAvailableAssistant(defaultAssistant);
                    settingsService.setSelectedAssistant(defaultAssistant);
                }
            } catch (Exception e) {
                Notifications.Bus.notify(getErrorNotification("OpenAI API error.", e.getMessage()));
            }
        }
    }

    private List<Assistant> queryForExistingAssistants() {
        OpenAiResponse<Assistant> response = OpenAIServiceCache.getInstance().getService()
                .listAssistants(new ListSearchParameters());
        return response.getData();
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "GPT Assistant Configuration.";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settingsComponent = new SettingsComponent(PluginSettings.getInstance());
        return settingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        PluginSettings settingsService = PluginSettings.getInstance();
        boolean modified = !settingsService.getApiKey().equals(settingsComponent.getApiKey());
        modified = modified || !settingsService.getEnableReformatProcessedCode() == settingsComponent.getEnableReformatProcessedCode();
        modified = modified || settingsService.getTokenThreshold() != Integer.parseInt(settingsComponent.getTokenThreshold());
        modified = modified || settingsService.getOpenIARequestTimeoutSeconds() != Integer.parseInt(settingsComponent.getOpenIARequestTimeoutSeconds());

        return modified;
    }

    @Override
    public void apply() throws ConfigurationException {
        PluginSettings settingsService = PluginSettings.getInstance();
        settingsService.setApiKey(settingsComponent.getApiKey());
        settingsService.setEnableReformatProcessedCode(settingsComponent.getEnableReformatProcessedCode());
        settingsService.setTokenThreshold(Integer.parseInt(settingsComponent.getTokenThreshold()));
        settingsService.setOpenIARequestTimeoutSeconds(Integer.parseInt(settingsComponent.getOpenIARequestTimeoutSeconds()));
    }

    @Override
    public void reset() {
        PluginSettings settingsService = PluginSettings.getInstance();
        settingsComponent.setApiKey(settingsService.getApiKey());
        settingsComponent.setEnableReformatProcessedCode(settingsService.getEnableReformatProcessedCode());
        settingsComponent.setTokenThreshold(String.valueOf(settingsService.getTokenThreshold()));
        settingsComponent.setOpenIARequestTimeoutSeconds(String.valueOf(settingsService.getOpenIARequestTimeoutSeconds()));
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }

}
