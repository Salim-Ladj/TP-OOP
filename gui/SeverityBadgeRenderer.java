import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Custom TableCellRenderer that displays SeverityLevel values as colored badges.
 * CRITICAL → red, WARNING → yellow/amber, NORMAL → green.
 */
public class SeverityBadgeRenderer extends DefaultTableCellRenderer {

    private static final Color CRITICAL_COLOR = new Color(0xF4, 0x43, 0x36);
    private static final Color WARNING_COLOR  = new Color(0xFF, 0xC1, 0x07);
    private static final Color NORMAL_COLOR   = new Color(0x00, 0xE6, 0x76);
    private static final Color BADGE_BG_CRITICAL = new Color(0x4A, 0x0E, 0x0A);
    private static final Color BADGE_BG_WARNING  = new Color(0x4E, 0x39, 0x00);
    private static final Color BADGE_BG_NORMAL   = new Color(0x00, 0x3D, 0x20);

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
                case "CRITICAL":
                    label.setForeground(CRITICAL_COLOR);
                    label.setBackground(BADGE_BG_CRITICAL);
                    break;
                case "WARNING":
                    label.setForeground(WARNING_COLOR);
                    label.setBackground(BADGE_BG_WARNING);
                    break;
                case "NORMAL":
                    label.setForeground(NORMAL_COLOR);
                    label.setBackground(BADGE_BG_NORMAL);
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
