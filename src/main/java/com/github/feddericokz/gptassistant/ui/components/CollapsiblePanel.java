package com.github.feddericokz.gptassistant.ui.components;

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import lombok.Getter;

import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Getter
class CollapsiblePanel extends JBPanel {

    private final JBLabel titleLabel;
    private final CheckboxList checkboxList;
    private boolean isCollapsed = false;

    public CollapsiblePanel(String title, CheckboxList checkboxList) {
        this.checkboxList = checkboxList;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(JBColor.LIGHT_GRAY)); // Panel border

        titleLabel = new JBLabel(title);
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

        add(titleLabel, BorderLayout.PAGE_START);
        JBScrollPane scrollPane = new JBScrollPane(checkboxList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5,15,5,5));
        add(scrollPane, BorderLayout.CENTER);

        updateListVisibility();
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
