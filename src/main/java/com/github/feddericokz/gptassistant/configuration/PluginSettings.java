package com.github.feddericokz.gptassistant.configuration;

import com.intellij.openapi.components.*;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

@Service
@State(
        name = "GPTAssistantSettings",
        storages = {@Storage("GPTAssistantSettings.xml")}
)
public final class PluginSettings implements PersistentStateComponent<PluginSettings.GPTAssistantPluginState> {

    private GPTAssistantPluginState myState = new GPTAssistantPluginState();

    public static class GPTAssistantPluginState {
        String apiKey = "";
        String gpt3Model = "gpt-3.5-turbo-1106";
        String gpt4Model = "gpt-4-1106-preview";
        boolean enableReformatProcessedCode = true;
        String assistantId = "";
    }

    public static PluginSettings getInstance() {
        return ServiceManager.getService(PluginSettings.class);
    }

    @Override
    public GPTAssistantPluginState getState() {
        return myState;
    }

    @Override
    public void loadState(GPTAssistantPluginState state) {
        myState = state;
    }

    public void setApiKey(String apiKey) {
        myState.apiKey = apiKey;
    }

    public String getApiKey() {
        return myState.apiKey;
    }

    public void setGpt3Model(String model) {
        myState.gpt3Model = model;
    }

    public String getGpt3Model() {
        return myState.gpt3Model;
    }

    public void setGpt4Model(String model) {
        myState.gpt4Model = model;
    }

    public String getGpt4Model() {
        return myState.gpt4Model;
    }

    public void setEnableReformatProcessedCode(boolean value) {
        this.myState.enableReformatProcessedCode = value;
    }

    public boolean getEnableReformatProcessedCode() {
        return this.myState.enableReformatProcessedCode;
    }

    public String getAssistantId() {
        return myState.assistantId;
    }

    public void setAssistantId(String id) {
        this.myState.assistantId = id;
    }

}
