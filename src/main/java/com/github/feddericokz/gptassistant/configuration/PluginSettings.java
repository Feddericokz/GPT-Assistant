package com.github.feddericokz.gptassistant.configuration;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Service
@State(
        name = "GPTAssistantSettings",
        storages = {@Storage("GPTAssistantSettings.xml")}
)
public final class PluginSettings implements PersistentStateComponent<PluginSettings.GPTAssistantPluginState> {

    private GPTAssistantPluginState pluginState = new GPTAssistantPluginState();

    public static class GPTAssistantPluginState {
        String apiKey = "";
        boolean enableReformatProcessedCode = true;
        List<Assistant> availableAssistants;
        Assistant selectedAssistant;
    }

    public static PluginSettings getInstance() {
        return ServiceManager.getService(PluginSettings.class); // TODO This is deprecated, update.
    }

    @Override
    public GPTAssistantPluginState getState() {
        return pluginState;
    }

    @Override
    public void loadState(GPTAssistantPluginState state) {
        pluginState = state;
    }

    public void setApiKey(String apiKey) {
        pluginState.apiKey = apiKey;
    }

    public String getApiKey() {
        return pluginState.apiKey;
    }

    public void setEnableReformatProcessedCode(boolean value) {
        this.pluginState.enableReformatProcessedCode = value;
    }

    public boolean getEnableReformatProcessedCode() {
        return this.pluginState.enableReformatProcessedCode;
    }

    @Nullable
    public List<Assistant> getAvailableAssistants() {
        return pluginState.availableAssistants;
    }

    public void setAvailableAssistants(List<Assistant> assistantList) {
        pluginState.availableAssistants = assistantList;
    }

    public void addAvailableAssistant(Assistant assistant) {
        if (pluginState.availableAssistants == null) {
            pluginState.availableAssistants = new ArrayList<>();
        }
        pluginState.availableAssistants.add(assistant);
    }

    @Nullable
    public Assistant getSelectedAssistant() {
        return pluginState.selectedAssistant;
    }

    public void setSelectedAssistant(Assistant assistant) {
        pluginState.selectedAssistant = assistant;
    }

}
