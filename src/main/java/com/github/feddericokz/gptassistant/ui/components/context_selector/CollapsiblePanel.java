package com.github.feddericokz.gptassistant.ui.components.context_selector;

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Getter
public class CollapsiblePanel extends JPanel {

    private final JComponent component;
    private boolean isCollapsed;

    public CollapsiblePanel(String title, JComponent component, boolean startCollapsed) {
        // Saving reference to be able to use a Getter.
        this.component = component;
        this.isCollapsed = startCollapsed;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(JBColor.LIGHT_GRAY));

        JComponent titleLabel = getTitleLabel(title);

        add(titleLabel, BorderLayout.PAGE_START);
        add(component, BorderLayout.CENTER);

        updateListVisibility();
    }

    @NotNull
    private JBLabel getTitleLabel(String title) {
        JBLabel titleLabel = new JBLabel(title);
        titleLabel.setOpaque(true);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Title padding
        titleLabel.setIcon(isCollapsed ? AllIcons.General.ArrowDown : AllIcons.General.ArrowRight);
        titleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleListVisibility();
                titleLabel.setIcon(isCollapsed ? AllIcons.General.ArrowDown : AllIcons.General.ArrowRight);
            }
        });
        return titleLabel;
    }

    private void toggleListVisibility() {
        isCollapsed = !isCollapsed;
        updateListVisibility();
    }

    private void updateListVisibility() {
        getComponent(1).setVisible(!isCollapsed); // The list is the second component (index 1)
        revalidate();
        repaint();
    }

}
