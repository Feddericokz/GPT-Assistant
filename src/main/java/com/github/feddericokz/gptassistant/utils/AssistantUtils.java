package com.github.feddericokz.gptassistant.utils;

import com.github.feddericokz.gptassistant.configuration.OpenAIServiceCache;
import com.github.feddericokz.gptassistant.configuration.PluginSettings;
import com.github.feddericokz.gptassistant.configuration.Prompts;
import com.github.feddericokz.gptassistant.utils.exceptions.AssistantNotSelectedException;
import com.theokanning.openai.DeleteResult;
import com.theokanning.openai.assistants.Assistant;
import com.theokanning.openai.assistants.AssistantRequest;
import com.theokanning.openai.messages.Message;
import com.theokanning.openai.messages.MessageRequest;
import com.theokanning.openai.runs.CreateThreadAndRunRequest;
import com.theokanning.openai.runs.Run;
import com.theokanning.openai.threads.ThreadRequest;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

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

        return OpenAIServiceCache.getInstance()
                .getService()
                .createAssistant(assistantRequest);
    }

    public static boolean deleteAssistant(Assistant assistant) {
        DeleteResult deleteResult = OpenAIServiceCache.getInstance()
                .getService()
                .deleteAssistant(assistant.getId());
        return deleteResult.isDeleted();
    }

    public static Run createAssistantThreadAndRun(List<String> stringMessages) throws AssistantNotSelectedException {
        if (stringMessages == null || stringMessages.isEmpty()) {
            throw new IllegalArgumentException("stringMessages is null or empty");
        }

        List<MessageRequest> messageRequestList = stringMessages.stream()
                .map(msg -> MessageRequest.builder().content(msg).build())
                .collect(Collectors.toList());

        ThreadRequest threadRequest = ThreadRequest.builder()
                .messages(messageRequestList)
                .build();

        Assistant assistant = PluginSettings.getInstance().getSelectedAssistant();

        if (assistant != null) {
            CreateThreadAndRunRequest createThreadAndRunRequest = CreateThreadAndRunRequest.builder()
                    .assistantId(assistant.getId())
                    .thread(threadRequest)
                    .build();

            return OpenAIServiceCache.getInstance()
                    .getService()
                    .createThreadAndRun(createThreadAndRunRequest);
        } else {
            throw new AssistantNotSelectedException();
        }
    }

    public static List<String> waitUntilRunCompletesAndGetAssistantResponse(Run assistantRun) {
        Run run;
        while (true) {
            run = OpenAIServiceCache.getInstance()
                    .getService()
                    .retrieveRun(assistantRun.getThreadId(), assistantRun.getId());
            if ("completed".equals(run.getStatus()) || "failed".equals(run.getStatus())) {
                break;
            }
            try {
                //noinspection BusyWait
                sleep(PluginSettings.getInstance().getRetrieveRunInterval());
            } catch (InterruptedException e) {
                java.lang.Thread.currentThread().interrupt();
                throw new RuntimeException("The thread waiting for the run to complete was interrupted", e);
            }
        }

        if ("failed".equals(run.getStatus())) {
            throw new RuntimeException("The run failed to complete. Last error: " + run.getLastError());
        }

        List<Message> messageList = OpenAIServiceCache.getInstance()
                .getService()
                .listMessages(run.getThreadId()).getData();

        return messageList
                .stream()
                .filter(m -> "assistant".equals(m.getRole()))
                // We're expecting a single content per message.
                // TODO So far, this is working as a new thread for every request, sending the whole context everytime.
                //  It probably would be more cost effective, for working sessions where we will be using the assistant to work on the same files,
                //  to maintain a thread and send context just once.
                .map(message -> message.getContent().get(0).getText().getValue())
                .collect(Collectors.toList());
    }

}
