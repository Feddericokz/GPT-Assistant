package com.github.feddericokz.gptassistant.behaviors;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Prompt {

    private String promptId;

    private String promptString;

}
