package com.github.feddericokz.gptassistant.configuration;

import com.intellij.openapi.components.*;

@Service
@State(
        name = "GPTAssistantSettings",
        storages = {@Storage("GPTAssistantSettings.xml")}
)
public final class GPTAssistantPluginService implements PersistentStateComponent<GPTAssistantPluginService.GPTAssistantPluginState> {

    private GPTAssistantPluginState myState = new GPTAssistantPluginState();

    public static class GPTAssistantPluginState {
        String apiKey = "";
        String gpt3Model = "gpt-3.5-turbo-1106";
        String gpt4Model = "";
    }

    public static GPTAssistantPluginService getInstance() {
        return ServiceManager.getService(GPTAssistantPluginService.class);
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
}
