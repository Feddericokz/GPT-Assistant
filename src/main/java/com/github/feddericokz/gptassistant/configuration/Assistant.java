package com.github.feddericokz.gptassistant.configuration;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Assistant {

    private String assistantId;

    private String gptModel;

}
