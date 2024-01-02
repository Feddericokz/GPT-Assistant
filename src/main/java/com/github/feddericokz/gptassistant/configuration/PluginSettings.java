package com.github.feddericokz.gptassistant.configuration;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.theokanning.openai.assistants.Assistant;
import org.jetbrains.annotations.NotNull;

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
        public String apiKey = "";
        public boolean enableReformatProcessedCode = true;
        public List<Assistant> availableAssistants;
        public Assistant selectedAssistant;
    }

    public static PluginSettings getInstance() {
        return ApplicationManager.getApplication().getService(PluginSettings.class);
    }

    @Override
    public GPTAssistantPluginState getState() {
        return pluginState;
    }

    public void loadState(@NotNull GPTAssistantPluginState state) {
        pluginState = state;
    }

    public void setApiKey(String apiKey) {
        pluginState.apiKey = apiKey;
        OpenAIServiceCache.getInstance().cleanService();
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

    public List<Assistant> getAvailableAssistants() {
        createAvailableListIfNotExists();
        return pluginState.availableAssistants;
    }

    private void createAvailableListIfNotExists() {
        if (pluginState.availableAssistants == null) {
            pluginState.availableAssistants = new ArrayList<>();
        }
    }

    public void setAvailableAssistants(List<Assistant> assistantList) {
        pluginState.availableAssistants = assistantList;
    }

    public void addAvailableAssistant(Assistant assistant) {
        createAvailableListIfNotExists();
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
