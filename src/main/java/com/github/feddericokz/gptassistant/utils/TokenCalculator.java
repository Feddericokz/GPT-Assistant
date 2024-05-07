package com.github.feddericokz.gptassistant.utils;

import com.github.feddericokz.gptassistant.configuration.PluginSettings;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.*;
import com.theokanning.openai.assistants.Assistant;

public class TokenCalculator {
    public int calculateTokens(String message) {
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        EncodingType encoding = getEncodingForCurrentModel();
        Encoding enc = registry.getEncoding(encoding);
        IntArrayList encoded = enc.encode(message);
        return encoded.size();
    }

    private EncodingType getEncodingForCurrentModel() {
        Assistant selectedAssistant = PluginSettings.getInstance().getSelectedAssistant();
        if (selectedAssistant != null) {
            String selectedModel = selectedAssistant.getModel();
            // <nlp> Logic to decide the encoding based on model. </nlp>
        }
        return EncodingType.CL100K_BASE;
    }

}
