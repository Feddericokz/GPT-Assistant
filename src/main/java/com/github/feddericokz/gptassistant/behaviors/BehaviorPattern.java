package com.github.feddericokz.gptassistant.behaviors;

import lombok.*;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BehaviorPattern {

    private Prompt systemPrompt;

    @Singular
    private Map<Prompt, FollowUpHandler> followUpPrompts;

}
