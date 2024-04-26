package com.github.feddericokz.gptassistant.ui.components.context_selector;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckboxListItem {

    private final String label;
    private boolean isSelected;

    public CheckboxListItem(String label) {
        this.label = label;
        this.isSelected = false;
    }

    @Override
    public String toString() {
        return label;
    }

}
