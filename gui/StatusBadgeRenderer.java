import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Custom TableCellRenderer that displays SensorStatus values as colored badges.
 * ACTIVE → green, SUSPENDED → amber/yellow, FAULTY → red.
 */
public class StatusBadgeRenderer extends DefaultTableCellRenderer {

    private static final Color ACTIVE_COLOR    = new Color(0x00, 0xE6, 0x76);
    private static final Color SUSPENDED_COLOR = new Color(0xFF, 0xC1, 0x07);
    private static final Color FAULTY_COLOR    = new Color(0xF4, 0x43, 0x36);
    private static final Color BADGE_BG_ACTIVE    = new Color(0x00, 0x3D, 0x20);
    private static final Color BADGE_BG_SUSPENDED = new Color(0x4E, 0x39, 0x00);
    private static final Color BADGE_BG_FAULTY    = new Color(0x4A, 0x0E, 0x0A);

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
                    label.setForeground(ACTIVE_COLOR);
                    label.setBackground(BADGE_BG_ACTIVE);
                    break;
                case "SUSPENDED":
                    label.setForeground(SUSPENDED_COLOR);
                    label.setBackground(BADGE_BG_SUSPENDED);
                    break;
                case "FAULTY":
                    label.setForeground(FAULTY_COLOR);
                    label.setBackground(BADGE_BG_FAULTY);
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
