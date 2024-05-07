package com.github.feddericokz.gptassistant.configuration;

import com.github.feddericokz.gptassistant.context.ContextItem;
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
import java.util.Objects;
import java.util.function.Consumer;

@Service
@State(
        name = "GPTAssistantSettings",
        storages = {@Storage("GPTAssistantSettings.xml")}
)
public final class PluginSettings implements PersistentStateComponent<PluginSettings.GPTAssistantPluginState> {

    private GPTAssistantPluginState pluginState = new GPTAssistantPluginState();

    private Consumer<ContextItem> updateContextItemsDisplayFunction;

    public static class GPTAssistantPluginState {
        public String apiKey = "";
        public boolean enableReformatProcessedCode = true;
        public List<Assistant> availableAssistants;
        public Assistant selectedAssistant;
        public Integer tokensThreshold;
        public Integer openIARequestTimeoutSeconds;
        private final List<ContextItem> contextItems = new ArrayList<>();
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
        OpenAIServiceCache.getInstance().clearService();
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

    public void addContextItem(ContextItem contextItem) {

        // Ensure no duplicates based on `contexPath`. If a duplicate is found, the method will return early.
        for (ContextItem item : pluginState.contextItems) {
            if (Objects.equals(item.contexPath(), contextItem.contexPath())) {
                return; // Exit function if a duplicate is found.
            }
        }

        pluginState.contextItems.add(contextItem);
        if (updateContextItemsDisplayFunction != null) {
            updateContextItemsDisplayFunction.accept(contextItem);
        }
    }

    public void removeContextItem(String path) {
        List<ContextItem> toRemoveList = pluginState.contextItems.stream()
                .filter(contextItem -> contextItem.contexPath().equals(path))
                .toList();
        pluginState.contextItems.removeAll(toRemoveList);
    }

    public List<ContextItem> getContextItems() {
        return pluginState.contextItems;
    }

    public void clearContextItems() {
        pluginState.contextItems.clear();
        // TODO function to clear UI
    }

    public void setUpdateContextItemsDisplayFunction(Consumer<ContextItem> updateContextItemsDisplayFunction) {
        this.updateContextItemsDisplayFunction = updateContextItemsDisplayFunction;
    }

    public int getTokenThreshold() {
        return pluginState.tokensThreshold == null ? 1000 : pluginState.tokensThreshold;
    }

    public void setTokenThreshold(Integer newThreshold) {
        pluginState.tokensThreshold = newThreshold;
    }

    public int getOpenIARequestTimeoutSeconds() {
        return pluginState.openIARequestTimeoutSeconds == null ? 60 : pluginState.openIARequestTimeoutSeconds;
    }

    public void setOpenIARequestTimeoutSeconds(Integer newTimeout) {
        pluginState.openIARequestTimeoutSeconds = newTimeout;

        // We need to rebuild the OpenAI service when this changes, so we clean the current instance from the cache.
        OpenAIServiceCache.getInstance().clearService();
    }
}
