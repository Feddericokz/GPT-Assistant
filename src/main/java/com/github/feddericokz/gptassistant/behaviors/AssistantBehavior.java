package com.github.feddericokz.gptassistant.behaviors;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AssistantBehavior {

    private String systemPrompt;

    @Singular
    private List<FollowUpHandler> followUpHandlers;

}
