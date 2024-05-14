package com.github.feddericokz.gptassistant.context;

public enum ContextItemType {
    // If the context item type is DIRECTORY, the whole contents of the directory will be scanned and added to the context.
    DIRECTORY,
    // Used for files that are not Java classes.
    FILE
}
