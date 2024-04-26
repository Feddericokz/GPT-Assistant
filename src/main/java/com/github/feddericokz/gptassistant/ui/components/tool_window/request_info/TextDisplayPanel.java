package com.github.feddericokz.gptassistant.ui.components.tool_window.request_info;


import lombok.Getter;

import javax.swing.*;
import java.awt.*;

@Getter
public class TextDisplayPanel extends JPanel {

    private final JTextArea textArea;

    public TextDisplayPanel(String text) {
        this.setLayout(new BorderLayout());
        textArea = new JTextArea(text);
        textArea.setEditable(false);
        this.add(textArea, BorderLayout.CENTER);
    }

}

