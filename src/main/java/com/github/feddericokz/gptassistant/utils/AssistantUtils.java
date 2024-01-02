package com.github.feddericokz.gptassistant.utils;

import com.github.feddericokz.gptassistant.configuration.OpenAIServiceCache;
import com.github.feddericokz.gptassistant.configuration.Prompts;
import com.theokanning.openai.DeleteResult;
import com.theokanning.openai.assistants.Assistant;
import com.theokanning.openai.assistants.AssistantRequest;

public class AssistantUtils {

    public static Assistant createSoftwareDevelopmentAssistant() {
        return createAssistant("GPT Software development assistant.", "gpt-4-0125-preview",
                "A GPT powered coding assistant that understands your code.",
                Prompts.SOFTWARE_DEVELOPMENT_ASSISTANT_PROMPT);
    }

    public static Assistant createAssistant(String name, String model, String description, String instructions) {
        AssistantRequest assistantRequest = new AssistantRequest();
        assistantRequest.setName(name);
        assistantRequest.setDescription(description);
        assistantRequest.setInstructions(instructions);
        assistantRequest.setModel(model);

        return OpenAIServiceCache.getInstance().getService().createAssistant(assistantRequest);
    }

    public static boolean deleteAssistant(Assistant assistant) {
        DeleteResult deleteResult = OpenAIServiceCache.getInstance().getService().deleteAssistant(assistant.getId());
        return deleteResult.isDeleted();
    }

}
