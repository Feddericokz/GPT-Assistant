package com.github.feddericokz.gptassistant.notifications;

public class CheckboxListItem {
    private String label;
    private boolean isSelected;

    public CheckboxListItem(String label) {
        this.label = label;
        this.isSelected = false;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    @Override
    public String toString() {
        return label;
    }
}
