package com.github.feddericokz.gptassistant.ui.components.configuration;

import com.github.feddericokz.gptassistant.configuration.PluginSettings;
import com.github.feddericokz.gptassistant.ui.components.context_selector.CollapsiblePanel;
import com.github.feddericokz.gptassistant.utils.AssistantUtils;
import com.intellij.notification.Notifications;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.theokanning.openai.assistants.Assistant;
import com.theokanning.openai.utils.TikTokensUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static com.github.feddericokz.gptassistant.notifications.Notifications.getErrorNotification;

public class AssistantsPanel extends JPanel {

    // TODO Break this class apart.

    // List updated on May 13, 2024.
    private static final String[] MODEL_OPTIONS = {
            "gpt-4-turbo",
            "gpt-4-turbo-2024-04-09",
            "gpt-4-0125-preview",
            "gpt-4-turbo-preview",
            "gpt-4-1106-preview",
            "gpt-4-vision-preview",
            "gpt-4-1106-vision-preview",
            "gpt-4",
            "gpt-4-0613",
            "gpt-4-32k",
            "gpt-4-32k-0613",
            "gpt-3.5-turbo-0125",
            "gpt-3.5-turbo",
            "gpt-3.5-turbo-1106",
            "gpt-3.5-turbo-instruct",
            "gpt-3.5-turbo-16k",
            "gpt-3.5-turbo-0613",
            "gpt-3.5-turbo-16k-0613"
    };

    private final PluginSettings settings;

    // Map to hold a reference to the selected buttons that use a card layout.
    private final Map<String, JPanel> selectedButtonsPanelsMap = new HashMap<>();
    private final JPanel assistantsListPanel;

    private final Map<String, JPanel> idToPanelObjectMap = new HashMap<>();

    public AssistantsPanel(PluginSettings settings) {
        this.settings = settings;

        // Going to use BoxLayout for the main container.
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        assistantsListPanel = new JPanel();
        assistantsListPanel.setLayout(new BoxLayout(assistantsListPanel, BoxLayout.PAGE_AXIS));

        for (Assistant availableAssistant : settings.getAvailableAssistants()) {
            JPanel assistantPanel = createAssistantPanel(availableAssistant);
            assistantsListPanel.add(assistantPanel);
            // Save the reference so we can delete it later.
            idToPanelObjectMap.put(availableAssistant.getId(), assistantPanel);
        }

        // Need a more general actions panel.
        JPanel assistantActionsCardLayoutPanel = getAssistantActionsCardLayoutPanel();

        // Add to main
        this.add(assistantsListPanel);
        this.add(assistantActionsCardLayoutPanel);
    }

    @NotNull
    private JPanel createAssistantPanel(Assistant availableAssistant) {
        // A general container for this specific assistant.
        JPanel assistantPanelContainer = new JPanel();
        assistantPanelContainer.setLayout(new BoxLayout(assistantPanelContainer, BoxLayout.PAGE_AXIS));
        assistantPanelContainer.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, JBColor.GRAY));

        // Panel with info and actions.
        JPanel assistantPanel = new JPanel();
        assistantPanel.setLayout(new BoxLayout(assistantPanel, BoxLayout.X_AXIS));
        assistantPanel.setBorder(JBUI.Borders.empty(5));

        // Create info panel
        JPanel assistantInfoPanel = createAssistantInfoPanel(availableAssistant);

        // Create actions panel
        JPanel assistantActionPanel = new JPanel();
        assistantActionPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        // Add actions to the panel
        // Delete button
        JButton deleteButton = getDeleteButton(settings, availableAssistant, assistantsListPanel);
        assistantActionPanel.add(deleteButton);

        // Selected button
        JPanel selectButtonPanel = getSelectButtonPanel(settings, availableAssistant, selectedButtonsPanelsMap);
        assistantActionPanel.add(selectButtonPanel);

        // Add everything to the assistantPanel.
        assistantPanel.add(assistantInfoPanel);
        assistantPanel.add(Box.createHorizontalGlue());
        assistantPanel.add(assistantActionPanel);

        // The panel containing the instructions.
        JPanel assistantInstructionsPanel = getAssistantInstructionsPanel(availableAssistant);

        assistantPanelContainer.add(assistantPanel);
        assistantPanelContainer.add(new JSeparator());
        assistantPanelContainer.add(assistantInstructionsPanel);
        return assistantPanelContainer;
    }

    @NotNull
    private static JPanel getSelectButtonPanel(PluginSettings settings, Assistant availableAssistant,
                                               Map<String, JPanel> selectedButtonsPanelsMap) {

        JPanel selectButtonPanel = new SelectButtonPanel(settings, availableAssistant, selectedButtonsPanelsMap);

        // Save them in the map.
        selectedButtonsPanelsMap.put(availableAssistant.getId(), selectButtonPanel);

        return selectButtonPanel;
    }

    public static class SelectButtonPanel extends JPanel {

        CardLayout cardLayout;

        public SelectButtonPanel(PluginSettings settings, Assistant availableAssistant,
                                 Map<String, JPanel> selectedButtonsPanelsMap) {
            cardLayout = new CardLayout();
            this.setLayout(cardLayout);

            JButton setSelectedButton = getSetSelectedButton(settings, availableAssistant, selectedButtonsPanelsMap);
            JBLabel selectedLabel = new JBLabel("Selected");

            this.add(setSelectedButton, "selectAssistant");
            this.add(selectedLabel, "selected");

            if (settings.getSelectedAssistant() != null) {
                if (availableAssistant.getId().equals(settings.getSelectedAssistant().getId())) {
                    cardLayout.show(this, "selected");
                } else {
                    cardLayout.show(this, "selectAssistant");
                }
            } else {
                cardLayout.show(this, "selectAssistant");
            }
        }

        @NotNull
        private JButton getSetSelectedButton(PluginSettings settings, Assistant availableAssistant,
                                             Map<String, JPanel> selectedButtonsPanelsMap) {
            JButton setSelectedButton = new JButton("Set Selected");
            setSelectedButton.addActionListener(e -> {
                // Set button on the previous selected.
                Assistant previousSelected = settings.getSelectedAssistant();
                if (previousSelected != null) {
                    JPanel panel = selectedButtonsPanelsMap.get(previousSelected.getId());
                    CardLayout cLayout = (CardLayout) panel.getLayout();
                    cLayout.show(panel, "selectAssistant");
                }

                // Set current as selected.
                settings.setSelectedAssistant(availableAssistant);
                this.setSelected();
            });
            return setSelectedButton;
        }

        public void setSelected() {
            cardLayout.show(this, "selected");
        }
    }

    @NotNull
    private static JPanel createAssistantInfoPanel(Assistant availableAssistant) {
        JPanel assistantInfoPanel = new JPanel();
        assistantInfoPanel.setBorder(JBUI.Borders.empty(5));
        assistantInfoPanel.setLayout(new GridLayout(0,3));

        JPanel labelsPanel = new JPanel();
        labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.PAGE_AXIS));

        JBLabel nameLabel = new JBLabel("Name: " + availableAssistant.getName());
        nameLabel.setBorder(JBUI.Borders.empty(1));
        labelsPanel.add(nameLabel);

        JBLabel idLabel = new JBLabel("Assistant ID: " + availableAssistant.getId());
        idLabel.setBorder(JBUI.Borders.empty(1));
        labelsPanel.add(idLabel);

        JBLabel modelLabel = new JBLabel("GPT model: " + availableAssistant.getModel());
        modelLabel.setBorder(JBUI.Borders.empty(1));
        labelsPanel.add(modelLabel);

        // Add info to the panel
        assistantInfoPanel.add(labelsPanel);
        return assistantInfoPanel;
    }

    @NotNull
    private JButton getDeleteButton(PluginSettings settings, Assistant assistant, JPanel assistantsList) {
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> {

            int result = Messages.showOkCancelDialog(
                    "This action will delete the assistant on OpenIA servers.",
                    "Delete Assistant",
                    Messages.getOkButton(),
                    Messages.getCancelButton(),
                    Messages.getQuestionIcon()
            );

            if (result == Messages.OK && AssistantUtils.deleteAssistant(assistant)) {
                // Get the actual object to remove.
                JPanel removePanel = idToPanelObjectMap.get(assistant.getId());

                // Remove it from existence.
                settings.getAvailableAssistants().remove(assistant);
                idToPanelObjectMap.remove(assistant.getId());
                assistantsList.remove(removePanel);

                // Select another one automatically.
                if (!settings.getAvailableAssistants().isEmpty()) {
                    // Naive approach, select the first element on the list.
                    settings.setSelectedAssistant(settings.getAvailableAssistants().get(0));

                    // Need to mark the assistant as selected in the UI.
                    JComponent selectedAssistantButtonPanel
                            = selectedButtonsPanelsMap.get(settings.getSelectedAssistant().getId());
                    if (selectedAssistantButtonPanel instanceof SelectButtonPanel) {
                        ((SelectButtonPanel) selectedAssistantButtonPanel).setSelected();
                    }
                }
            }
        });
        return deleteButton;
    }

    @NotNull
    private static JPanel getAssistantInstructionsPanel(Assistant availableAssistant) {
        // Panel to display assistant instructions.
        JPanel assistantInstructionsPanel = new JPanel();
        assistantInstructionsPanel.setLayout(new BorderLayout());

        // Configure text area.
        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(JBUI.insets(10));
        textArea.append(availableAssistant.getInstructions());

        assistantInstructionsPanel.add(textArea, BorderLayout.CENTER);
        assistantInstructionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Title padding

        JComponent collapsibleInstructionsComponent = new CollapsiblePanel("GPT Instructions", assistantInstructionsPanel, true);

        // A container to make it take as much space as it wants.
        JPanel assistantInstructionsContainer = new JPanel();
        assistantInstructionsContainer.setLayout(new BorderLayout());
        assistantInstructionsContainer.add(collapsibleInstructionsComponent, BorderLayout.CENTER);

        return assistantInstructionsContainer;
    }

    @NotNull
    private JPanel getAssistantActionsCardLayoutPanel() {
        JPanel assistantActionsCardLayoutPanel = new JPanel();
        CardLayout actionsCardLayout = new CardLayout();
        assistantActionsCardLayoutPanel.setLayout(actionsCardLayout);
        assistantActionsCardLayoutPanel.setBorder(JBUI.Borders.empty(10));

        // Where buttons will be
        JPanel assistantsPanelActionsPanel = new JPanel();
        assistantsPanelActionsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        assistantsPanelActionsPanel.setBorder(JBUI.Borders.empty(10));

        // Add actions to this new actions panel.
        JButton addAssistantButton = new JButton("Create Assistant");
        addAssistantButton.addActionListener(e -> {
            actionsCardLayout.show(assistantActionsCardLayoutPanel, "createAssistantPanel");
        });
        assistantsPanelActionsPanel.add(addAssistantButton);

        // Panel to invoke when calling the create-assistant action.
        JPanel createAssistantPanel = getCreateAssistantPanel(actionsCardLayout, assistantActionsCardLayoutPanel);

        assistantActionsCardLayoutPanel.add(createAssistantPanel, "createAssistantPanel");
        assistantActionsCardLayoutPanel.add(assistantsPanelActionsPanel, "actions");

        // Actions is default.
        actionsCardLayout.show(assistantActionsCardLayoutPanel, "actions");

        return assistantActionsCardLayoutPanel;
    }

    @NotNull
    private JPanel getCreateAssistantPanel(CardLayout actionsCardLayout, JPanel assistantActionsCardLayoutPanel) {
        JPanel createAssistantPanel = new JPanel();
        createAssistantPanel.setLayout(new BoxLayout(createAssistantPanel, BoxLayout.PAGE_AXIS));

        // Panel for info components
        JPanel infoComponentsPanel = new JPanel();
        infoComponentsPanel.setLayout(new BoxLayout(infoComponentsPanel, BoxLayout.PAGE_AXIS));
        infoComponentsPanel.setBorder(JBUI.Borders.empty(10));

            // Title
            JPanel titlePanel = new JPanel();
            titlePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            JBLabel titleLabel = new JBLabel("Please complete assistant details.");
            titlePanel.add(titleLabel);

            // Name
            JPanel assistantNamePanel = new JPanel();
            assistantNamePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            assistantNamePanel.add(new JBLabel("Assistant name:"));
            JBTextField nameTextField = new JBTextField();
            nameTextField.setColumns(20);
            assistantNamePanel.add(nameTextField);

            // Description
            JPanel assistantDescriptionPanel = new JPanel();
            assistantDescriptionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            assistantDescriptionPanel.add(new JBLabel("Assistant description:"));
            JBTextField descriptionTextField = new JBTextField();
            descriptionTextField.setColumns(20);
            assistantDescriptionPanel.add(descriptionTextField);

            // Model
            JPanel assistantModelPanel = new JPanel();
            assistantModelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            assistantModelPanel.add(new JBLabel("Assistant model:"));
            JComboBox<String> modelComboBox = new ComboBox<>(MODEL_OPTIONS);
            assistantModelPanel.add(modelComboBox);

            // Instructions label
            JPanel instructionsLabelPanel = new JPanel();
            instructionsLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            JBLabel instructionsLabel = new JBLabel("Assistant instructions:");
            instructionsLabelPanel.add(instructionsLabel);

            // Prompt
            JPanel assistantPromptPanel = new JPanel();
            assistantPromptPanel.setLayout(new BorderLayout());
            assistantPromptPanel.setBorder(JBUI.Borders.empty(5, 5, 10, 5));
            JTextArea promptTextArea = new JBTextArea();
            promptTextArea.setRows(20);
            promptTextArea.setMargin(JBUI.insets(10));
            assistantPromptPanel.add(promptTextArea, BorderLayout.CENTER);

        infoComponentsPanel.add(titlePanel);
        infoComponentsPanel.add(new JSeparator());
        infoComponentsPanel.add(assistantNamePanel);
        infoComponentsPanel.add(assistantDescriptionPanel);
        infoComponentsPanel.add(assistantModelPanel);
        infoComponentsPanel.add(instructionsLabelPanel);
        infoComponentsPanel.add(assistantPromptPanel);

        // Panel for buttons
        JPanel buttonsComponent = new JPanel();
        buttonsComponent.setLayout(new FlowLayout());

            // Create button
            JButton createButton = new JButton("Create");
            createButton.addActionListener(e -> {

                try {
                    Assistant createdAssistant = AssistantUtils.createAssistant(nameTextField.getText(),
                            (String) modelComboBox.getSelectedItem(),
                            descriptionTextField.getText(),
                            promptTextArea.getText());

                    // Add created assistant to the list.
                    PluginSettings.getInstance().addAvailableAssistant(createdAssistant);

                    // Create a panel for the new assistant
                    JPanel newAssistantPanel = createAssistantPanel(createdAssistant);
                    assistantsListPanel.add(newAssistantPanel);

                    // Save the reference so we can delete it later.
                    idToPanelObjectMap.put(createdAssistant.getId(), newAssistantPanel);

                    // Go back to actions
                    actionsCardLayout.show(assistantActionsCardLayoutPanel, "actions");

                    // Clean fields.
                    nameTextField.setText("");
                    modelComboBox.setSelectedIndex(0);
                    descriptionTextField.setText("");
                    promptTextArea.setText("");
                } catch (Exception ex) {
                    Notifications.Bus.notify(getErrorNotification("OpenAI API error.", ex.getMessage()));
                }
            });

            buttonsComponent.add(createButton);

            // Cancel button
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> actionsCardLayout.show(assistantActionsCardLayoutPanel,"actions"));
            buttonsComponent.add(cancelButton);

        // Add everything to the create-assistant panel.
        createAssistantPanel.add(infoComponentsPanel);
        createAssistantPanel.add(buttonsComponent);

        return createAssistantPanel;
    }

}
