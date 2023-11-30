package com.github.feddericokz.gptassistant.configuration;

import com.intellij.openapi.components.*;

import static com.github.feddericokz.gptassistant.configuration.Prompts.*;

@Service
@State(
        name = "GPTAssistantSettings",
        storages = {@Storage("GPTAssistantSettings.xml")}
)
public final class GPTAssistantPluginSettings implements PersistentStateComponent<GPTAssistantPluginSettings.GPTAssistantPluginState> {

    private GPTAssistantPluginState myState = new GPTAssistantPluginState();

    public static class GPTAssistantPluginState {
        String apiKey = "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";
        String gpt3Model = "gpt-3.5-turbo-1106";
        String gpt4Model = "";
        boolean enableReformatProcessedCode = true;
        String seniorDevBehaviorSystemPrompt = DEFAULT_SR_DEV_BEHAVIOR_SYSTEM_PROMPT;
        String assistantBehaviorSystemPrompt =  DEFAULT_AI_ASSISTANT_SYSTEM_PROMPT;
        String importsUserPrompt = DEFAULT_IMPORTS_USER_PROMPT;
    }

    public static GPTAssistantPluginSettings getInstance() {
        return ServiceManager.getService(GPTAssistantPluginSettings.class);
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

    public void setSeniorDevBehaviorSystemPrompt(String prompt) {
        myState.seniorDevBehaviorSystemPrompt = prompt;
    }

    public String getSeniorDevBehaviorSystemPrompt() {
        return myState.seniorDevBehaviorSystemPrompt;
    }

    public void setImportsUserPrompt(String prompt) {
        myState.importsUserPrompt = prompt;
    }

    public String getImportsUserPrompt() {
        return myState.importsUserPrompt;
    }

    public void setAssistantBehaviorSystemPrompt(String prompt) {
        myState.seniorDevBehaviorSystemPrompt = prompt;
    }

    public String getAssistantBehaviorSystemPrompt() {
        return myState.seniorDevBehaviorSystemPrompt;
    }
}
