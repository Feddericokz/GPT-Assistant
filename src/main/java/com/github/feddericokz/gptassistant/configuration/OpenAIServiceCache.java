package com.github.feddericokz.gptassistant.configuration;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.theokanning.openai.service.OpenAiService;

import java.time.Duration;

@Service
public final class OpenAIServiceCache {

    private OpenAiService service;

    public static OpenAIServiceCache getInstance() {
        return ApplicationManager.getApplication().getService(OpenAIServiceCache.class);
    }

    public OpenAiService getService() {
        if (service == null) {
            service = new OpenAiService(PluginSettings.getInstance().getApiKey(),
                    Duration.ofSeconds(PluginSettings.getInstance().getOpenIARequestTimeoutSeconds()));
        }
        return service;
    }

    public void clearService() {
        service = null;
    }

}
