package com.github.feddericokz.gptassistant.utils;

import com.github.feddericokz.gptassistant.configuration.PluginSettings;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.*;
import com.theokanning.openai.assistants.Assistant;

import java.util.Optional;

public class TokenCalculator {
    public int calculateTokens(String message) {
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        Encoding encoding = getEncodingForCurrentModel();
        IntArrayList encoded = encoding.encode(message);
        return encoded.size();
    }

    private Encoding getEncodingForCurrentModel() {
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        Assistant selectedAssistant = PluginSettings.getInstance().getSelectedAssistant();
        if (selectedAssistant != null) {
            String selectedModel = selectedAssistant.getModel();

            // Or get the tokenizer corresponding to a specific OpenAI model
            Optional<Encoding> encoding = registry.getEncodingForModel(selectedModel);
            if (encoding.isPresent()) {
                return encoding.get();
            }
        }

        // Default encoding if no model is selected
        return registry.getEncoding(EncodingType.CL100K_BASE);
    }

}
