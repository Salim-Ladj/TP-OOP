package com.farm.demo.view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Renders sensor status values (ACTIVE, SUSPENDED, FAULTY) as colored badges
 * inside a JTable cell.
 */
public class StatusBadgeRenderer extends DefaultTableCellRenderer {

    private static final Color ACTIVE_FG   = new Color(0x00, 0xE6, 0x76);
    private static final Color SUSPENDED_FG= new Color(0xFF, 0xC1, 0x07);
    private static final Color FAULTY_FG   = new Color(0xF4, 0x43, 0x36);
    private static final Color BG_ACTIVE   = new Color(0x00, 0x3D, 0x20);
    private static final Color BG_SUSPEND  = new Color(0x4E, 0x39, 0x00);
    private static final Color BG_FAULT    = new Color(0x4A, 0x0E, 0x0A);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        JLabel label = new JLabel();
        label.setOpaque(true);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        String text = (value != null) ? value.toString() : "";
        label.setText(text);

        if (isSelected) {
            label.setBackground(new Color(0x15, 0x65, 0xC0));
            label.setForeground(Color.WHITE);
        } else {
            switch (text) {
                case "ACTIVE":
                    label.setForeground(ACTIVE_FG);
                    label.setBackground(BG_ACTIVE);
                    break;
                case "SUSPENDED":
                    label.setForeground(SUSPENDED_FG);
                    label.setBackground(BG_SUSPEND);
                    break;
                case "FAULTY":
                    label.setForeground(FAULTY_FG);
                    label.setBackground(BG_FAULT);
                    break;
                default:
                    label.setForeground(Color.WHITE);
                    label.setBackground(new Color(0x0D, 0x21, 0x37));
                    break;
            }
        }

        return label;
    }
}