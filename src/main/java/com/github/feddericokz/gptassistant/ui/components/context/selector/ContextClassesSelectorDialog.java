package com.github.feddericokz.gptassistant.ui.components.context.selector;

import com.github.feddericokz.gptassistant.configuration.PluginSettings;
import com.github.feddericokz.gptassistant.context.ContextItem;
import com.github.feddericokz.gptassistant.context.ContextItemType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.SeparatorComponent;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.awt.FlowLayout.LEFT;

public class ContextClassesSelectorDialog extends DialogWrapper {

    private final Project project;
    private final PluginSettings pluginSettings = PluginSettings.getInstance();
    private final List<CollapsiblePanel> collapsiblePanels;
    private JBCheckBox useGlobalContextClassesCheckbox;


    public ContextClassesSelectorDialog(List<List<CheckboxListItem>> contextItemsList, List<String> titles,
                                        Project project) {
        super(true);

        this.project = project;

        setTitle("Context Selector");

        collapsiblePanels = new ArrayList<>();
        for (int i = 0; i < contextItemsList.size(); i++) {
            collapsiblePanels.add(new CollapsiblePanel(titles.get(i), new CheckboxList(contextItemsList.get(i)), false));
        }

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        for (CollapsiblePanel collapsiblePanel : collapsiblePanels) {
            panel.add(collapsiblePanel);
        }

        panel.add(new JSeparator());

        useGlobalContextClassesCheckbox = new JBCheckBox("Use global context");
        JPanel globalContextClassesPanel = new JPanel();
        globalContextClassesPanel.setLayout(new FlowLayout(LEFT));
        globalContextClassesPanel.add(useGlobalContextClassesCheckbox);
        globalContextClassesPanel.add(new SeparatorComponent());
        panel.add(globalContextClassesPanel);

        return panel;
    }

    public List<List<String>> getSelectedValues() {
        List<List<String>> selectedValues = collapsiblePanels.stream()
                .map(CollapsiblePanel::getComponent)
                .map(list -> {
                    ListModel<CheckboxListItem> model = ((CheckboxList) list).getModel();
                    List<String> selectedItems = new ArrayList<>();
                    for (int i = 0; i < model.getSize(); i++) {
                        CheckboxListItem item = model.getElementAt(i);
                        if (item.isSelected()) {
                            selectedItems.add(item.toString());
                        }
                    }
                    return selectedItems;
                })
                .collect(Collectors.toList());

        if (useGlobalContextClassesCheckbox.isSelected()) {
            List<String> globalContextValues = getGlobalContextValues();
            selectedValues.add(globalContextValues);
        }

        return selectedValues;
    }


    private List<String> getGlobalContextValues() {
        List<String> returnList = new ArrayList<>();
        List<ContextItem> contextItems = pluginSettings.getContextItems();
        for (ContextItem contextItem : contextItems) {
            if (contextItem.itemType().equals(ContextItemType.DIRECTORY)) {
                // Loop through the directory recursively to find all class files
                List<String> classesInDirectory = findAllClassesInDirectory(contextItem.contexPath());
                returnList.addAll(classesInDirectory);
            } else if (contextItem.itemType().equals(ContextItemType.CLASS)) {
                // Add the class directly to the return list
                returnList.add(contextItem.contexPath());
            }
        }
        return returnList;
    }

    // TODO This is not a pretty way to do this, but works for now.
    private List<String> findAllClassesInDirectory(String directoryPath) {
        List<String> classFiles = new ArrayList<>();
        // Assuming a method exists to get files in a directory, including subdirectories
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    classFiles.addAll(findAllClassesInDirectory(file.getAbsolutePath()));
                } else if (file.getName().endsWith(".java")) {
                    VirtualFile virtualFile = VfsUtil.findFileByIoFile(file, true);
                    if (virtualFile != null) {
                        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                        if (psiFile != null) {
                            // We also need to strip ".src.main." from this.
                            String classPath = psiFile.getVirtualFile().getPath()
                                    .replace(ProjectRootManager.getInstance(project).getContentRoots()[0].getPath(), "") // Dynamically obtain the project root path
                                    .replace(File.separatorChar, '.') // Replace system-dependent file separator with dot notation
                                    .replace(".java", "") // Remove the file extension
                                    .replace(".src.main.", "");

                            classFiles.add(classPath);
                        }
                    }
                }
            }
        }
        return classFiles;
    }

}