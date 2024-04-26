package com.github.feddericokz.gptassistant.ui.components.tool_window.request_info;

import javax.swing.*;
import java.awt.*;

public class SwitchableTextDisplayPanel extends JPanel {
    private TextDisplayPanel displayPanel;  // Attribute to hold the TextDisplayPanel instance


    public SwitchableTextDisplayPanel() {
        this.setLayout(new BorderLayout());
        displayPanel = new TextDisplayPanel("");
        this.add(displayPanel, BorderLayout.CENTER); // Initially empty display
    }


    public void updateText(String text){
        this.remove(displayPanel);  // Remove the old display
        displayPanel = new TextDisplayPanel(text);  // Create a new display with the new text
        this.add(displayPanel,BorderLayout.CENTER);  // Add the new display panel
        this.repaint();  // Refresh the UserRequestPanel to display the new text
        this.validate(); // Revalidate the layout
    }

}
