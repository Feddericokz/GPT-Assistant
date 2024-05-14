package com.github.feddericokz.gptassistant.utils;

import com.github.feddericokz.gptassistant.common.Logger;
import com.github.feddericokz.gptassistant.configuration.PluginSettings;
import com.github.feddericokz.gptassistant.context.ContextItem;
import com.github.feddericokz.gptassistant.context.ContextItemType;
import com.github.feddericokz.gptassistant.ui.components.tool_window.log.ToolWindowLogger;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ContextUtils {

    private static final Logger logger = ToolWindowLogger.getInstance(ContextUtils.class);

    public static List<String> getGlobalContextValues() {
        List<String> returnList = new ArrayList<>();
        List<ContextItem> contextItems = PluginSettings.getInstance().getContextItems();
        for (ContextItem contextItem : contextItems) {
            if (contextItem.itemType().equals(ContextItemType.DIRECTORY)) {
                // Loop through the directory recursively to find all class files
                List<String> classesInDirectory = findAllFilesInDirectory(contextItem.contexPath());
                returnList.addAll(classesInDirectory);
            } else if (contextItem.itemType().equals(ContextItemType.FILE)) {
                // Add the class directly to the return list
                returnList.add(contextItem.contexPath());
            }
        }
        return returnList;
    }

    private static List<String> findAllFilesInDirectory(String directoryPathString) {
        List<String> filePaths = new ArrayList<>();
        try {
            URI directoryPathURI = new URI(directoryPathString);
            Path directoryPath = Paths.get(directoryPathURI);
            try (Stream<Path> walkStream = Files.walk(directoryPath)) {
                walkStream.filter(Files::isRegularFile)
                        .forEach(path -> {
                            filePaths.add(String.valueOf(path.toUri()));
                        });
            }
        } catch (Exception e) {
            logger.error("Error while resolving files for directory url: " + directoryPathString, e);
        }
        return filePaths;
    }

}
