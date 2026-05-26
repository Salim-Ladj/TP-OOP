import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AlertManagementPanel extends JPanel {

    // ── Theme colours ──
    private static final Color BG_DARK      = new Color(0x0A, 0x16, 0x28);
    private static final Color CARD_BG      = new Color(0x0D, 0x21, 0x37);
    private static final Color CARD_BG_ALT  = new Color(0x11, 0x22, 0x44);
    private static final Color ACCENT_BLUE  = new Color(0x15, 0x65, 0xC0);
    private static final Color ACCENT_GREEN = new Color(0x00, 0xE6, 0x76);
    private static final Color ACCENT_AMBER = new Color(0xFF, 0xC1, 0x07);
    private static final Color ACCENT_RED   = new Color(0xF4, 0x43, 0x36);
    private static final Color TEXT_WHITE    = Color.WHITE;
    private static final Color TEXT_GRAY     = new Color(0x99, 0xAA, 0xBB);
    private static final Color ROW_CRIT     = new Color(0x2E, 0x0F, 0x0A);
    private static final Color ROW_WARN     = new Color(0x2E, 0x25, 0x0A);

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter PARSE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final Farm farm;

    // Stats labels
    private JLabel critLabel, warnLabel, totalLabel;

    // Active alerts table
    private DefaultTableModel activeModel;
    private JTable activeTable;

    // History table
    private DefaultTableModel histModel;
    private JTable histTable;
    private JComboBox<String> hZoneCombo, hSeverityCombo;
    private JTextField hSensorField, hStartField, hEndField;

    // Overview labels
    private JLabel ovCritical, ovWarning, ovNormal;
    private JPanel ovCritBar, ovWarnBar, ovNormBar;

    public AlertManagementPanel(Farm farm) {
        this.farm = farm;
        setLayout(new BorderLayout(10, 10));
        setBackground(BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // North: stats + overview
        JPanel northPanel = new JPanel(new BorderLayout(10, 0));
        northPanel.setOpaque(false);
        northPanel.add(buildStatsBar(), BorderLayout.CENTER);
        northPanel.add(buildOverviewPanel(), BorderLayout.EAST);
        add(northPanel, BorderLayout.NORTH);

        // Center: tabbed pane with Active & History
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(CARD_BG);
        tabs.setForeground(TEXT_WHITE);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 12));

        // Active tab
        JPanel activePanel = new JPanel(new BorderLayout(0, 8));
        activePanel.setBackground(BG_DARK);
        activePanel.add(buildActiveTable(), BorderLayout.CENTER);
        activePanel.add(buildActiveActionBar(), BorderLayout.SOUTH);
        tabs.addTab("Active Alerts", activePanel);

        // History tab
        tabs.addTab("History", buildHistoryPanel());

        add(tabs, BorderLayout.CENTER);

        refreshAll();
    }

    // ═══════════════════════════ A) STATS BAR ═══════════════════════════

    private JPanel buildStatsBar() {
        JPanel bar = new JPanel(new GridLayout(1, 3, 12, 0));
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 80));

        critLabel  = new JLabel("0", SwingConstants.CENTER);
        warnLabel  = new JLabel("0", SwingConstants.CENTER);
        totalLabel = new JLabel("0", SwingConstants.CENTER);

        bar.add(makeStatCard(critLabel,  "Critical Alerts", new Color(0x3A, 0x0A, 0x0A)));
        bar.add(makeStatCard(warnLabel,  "Warning Alerts",  new Color(0x3A, 0x2D, 0x00)));
        bar.add(makeStatCard(totalLabel, "Total Active",    CARD_BG));
        return bar;
    }

    private RoundedPanel makeStatCard(JLabel numLabel, String subtitle, Color bg) {
        RoundedPanel card = new RoundedPanel(bg, 16);
        card.setLayout(new BorderLayout());
        numLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        numLabel.setForeground(TEXT_WHITE);
        JLabel sub = new JLabel(subtitle, SwingConstants.CENTER);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(TEXT_GRAY);
        card.add(numLabel, BorderLayout.CENTER);
        card.add(sub, BorderLayout.SOUTH);
        card.setBorder(BorderFactory.createEmptyBorder(8, 10, 10, 10));
        return card;
    }

    // ═══════════════════════════ E) OVERVIEW PANEL ═══════════════════════════

    private JPanel buildOverviewPanel() {
        RoundedPanel panel = new RoundedPanel(CARD_BG, 16);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        panel.setPreferredSize(new Dimension(220, 80));

        JLabel title = new JLabel("Alerts Overview");
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(TEXT_WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(6));

        ovCritical = new JLabel("CRITICAL: 0");
        ovWarning  = new JLabel("WARNING: 0");
        ovNormal   = new JLabel("NORMAL: 0");

        ovCritBar = makeOverviewRow(ACCENT_RED,   ovCritical);
        ovWarnBar = makeOverviewRow(ACCENT_AMBER, ovWarning);
        ovNormBar = makeOverviewRow(ACCENT_GREEN, ovNormal);

        panel.add(ovCritBar);
        panel.add(Box.createVerticalStrut(4));
        panel.add(ovWarnBar);
        panel.add(Box.createVerticalStrut(4));
        panel.add(ovNormBar);

        return panel;
    }

    private JPanel makeOverviewRow(Color color, JLabel label) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel colorBlock = new JPanel();
        colorBlock.setBackground(color);
        colorBlock.setPreferredSize(new Dimension(12, 16));

        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(TEXT_WHITE);

        row.add(colorBlock, BorderLayout.WEST);
        row.add(label, BorderLayout.CENTER);
        return row;
    }

    // ═══════════════════════════ B) ACTIVE TABLE ═══════════════════════════

    private JPanel buildActiveTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);

        String[] cols = {"ID", "Severity", "Sensor", "Zone", "Value",
                         "Timestamp", "Message", "Status"};
        activeModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        activeTable = new JTable(activeModel);
        styleAlertTable(activeTable);
        activeTable.getColumnModel().getColumn(1).setCellRenderer(new SeverityBadgeRenderer());

        JScrollPane sp = new JScrollPane(activeTable);
        sp.getViewport().setBackground(BG_DARK);
        sp.setBorder(BorderFactory.createEmptyBorder());
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    // ═══════════════════════════ C) ACTIVE ACTION BAR ═══════════════════════════

    private JPanel buildActiveActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        bar.setBackground(CARD_BG);
        bar.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        bar.add(makeBtn("Acknowledge", ACCENT_GREEN, e -> {
            int row = activeTable.getSelectedRow();
            if (row < 0) { warn("Please select an alert first."); return; }
            int id = (int) activeModel.getValueAt(row, 0);
            String msg = farm.acknowledgeAlert(id);
            JOptionPane.showMessageDialog(this, msg);
            refreshAll();
        }));

        bar.add(makeBtn("Dismiss", ACCENT_RED, e -> {
            int row = activeTable.getSelectedRow();
            if (row < 0) { warn("Please select an alert first."); return; }
            int id = (int) activeModel.getValueAt(row, 0);
            String msg = farm.dismissAlert(id);
            JOptionPane.showMessageDialog(this, msg);
            refreshAll();
        }));

        bar.add(makeBtn("Refresh", ACCENT_BLUE, e -> refreshAll()));

        return bar;
    }

    // ═══════════════════════════ D) HISTORY PANEL ═══════════════════════════

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Filter bar
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filters.setOpaque(false);

        hZoneCombo = new JComboBox<>();
        hZoneCombo.addItem("All");
        for (Zone z : farm.getZones()) hZoneCombo.addItem(z.getCode());
        styleCombo(hZoneCombo);
        filters.add(darkLabel("Zone:")); filters.add(hZoneCombo);

        hSensorField = darkField("");
        hSensorField.setColumns(8);
        filters.add(darkLabel("Sensor:")); filters.add(hSensorField);

        hSeverityCombo = new JComboBox<>(new String[]{"All", "WARNING", "CRITICAL"});
        styleCombo(hSeverityCombo);
        filters.add(darkLabel("Severity:")); filters.add(hSeverityCombo);

        hStartField = darkField("");
        hStartField.setColumns(12);
        hStartField.setToolTipText("yyyy-MM-ddTHH:mm");
        filters.add(darkLabel("Start:")); filters.add(hStartField);

        hEndField = darkField("");
        hEndField.setColumns(12);
        hEndField.setToolTipText("yyyy-MM-ddTHH:mm");
        filters.add(darkLabel("End:")); filters.add(hEndField);

        filters.add(makeBtn("Search", ACCENT_BLUE, e -> searchHistory()));

        panel.add(filters, BorderLayout.NORTH);

        // History table
        String[] cols = {"ID", "Severity", "Sensor", "Zone", "Value",
                         "Timestamp", "Message", "Status"};
        histModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        histTable = new JTable(histModel);
        styleAlertTable(histTable);
        histTable.getColumnModel().getColumn(1).setCellRenderer(new SeverityBadgeRenderer());

        JScrollPane sp = new JScrollPane(histTable);
        sp.getViewport().setBackground(BG_DARK);
        sp.setBorder(BorderFactory.createEmptyBorder());
        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    private void searchHistory() {
        String zone = (String) hZoneCombo.getSelectedItem();
        String zoneCode = "All".equals(zone) ? null : zone;

        String sensor = hSensorField.getText().trim();
        String sensorCode = sensor.isEmpty() ? null : sensor;

        String sev = (String) hSeverityCombo.getSelectedItem();
        SeverityLevel level = null;
        if ("WARNING".equals(sev))  level = SeverityLevel.WARNING;
        if ("CRITICAL".equals(sev)) level = SeverityLevel.CRITICAL;

        LocalDateTime start = parseDate(hStartField.getText().trim());
        LocalDateTime end   = parseDate(hEndField.getText().trim());

        List<Alert> results = farm.getAlertHistory(zoneCode, sensorCode, level, start, end);
        populateAlertTable(histModel, results);
    }

    // ═══════════════════════════ REFRESH ═══════════════════════════

    public void refreshAll() {
        refreshActiveTable();
        refreshStats();
        refreshOverview();
    }

    private void refreshActiveTable() {
        List<Alert> active = AlertManager.getActiveAlertsSortedBySeverity();
        populateAlertTable(activeModel, active);
    }

    private void populateAlertTable(DefaultTableModel model, List<Alert> alerts) {
        model.setRowCount(0);
        for (Alert a : alerts) {
            model.addRow(new Object[]{
                a.getAlertId(),
                a.getSeverityLevel().name(),
                a.getSensorUniqueCode(),
                a.getZoneId(),
                String.format("%.2f", a.getReadingValue()),
                a.getAlertTimestamp().format(DISPLAY_FMT),
                a.getMessage(),
                a.isAcknowledged() ? "Acknowledged" : "Pending"
            });
        }
    }

    private void refreshStats() {
        List<Alert> active = AlertManager.getActiveAlertsSortedBySeverity();
        long crit = active.stream().filter(a -> a.getSeverityLevel() == SeverityLevel.CRITICAL).count();
        long warn = active.stream().filter(a -> a.getSeverityLevel() == SeverityLevel.WARNING).count();
        critLabel.setText(String.valueOf(crit));
        warnLabel.setText(String.valueOf(warn));
        totalLabel.setText(String.valueOf(active.size()));
    }

    private void refreshOverview() {
        long crit = AlertManager.getActiveAlertsSortedBySeverity().stream()
                .filter(a -> a.getSeverityLevel() == SeverityLevel.CRITICAL).count();
        long warn = AlertManager.getActiveAlertsSortedBySeverity().stream()
                .filter(a -> a.getSeverityLevel() == SeverityLevel.WARNING).count();
        long norm = AlertManager.getActiveAlertsSortedBySeverity().stream()
                .filter(a -> a.getSeverityLevel() == SeverityLevel.NORMAL).count();
        ovCritical.setText("CRITICAL: " + crit + " alerts");
        ovWarning.setText("WARNING: " + warn + " alerts");
        ovNormal.setText("NORMAL: " + norm + " alerts");
    }

    // ═══════════════════════════ TABLE STYLING ═══════════════════════════

    private void styleAlertTable(JTable table) {
        table.setBackground(CARD_BG);
        table.setForeground(TEXT_WHITE);
        table.setGridColor(new Color(0x1A, 0x33, 0x50));
        table.setRowHeight(32);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setSelectionBackground(ACCENT_BLUE);
        table.setSelectionForeground(TEXT_WHITE);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        JTableHeader header = table.getTableHeader();
        header.setBackground(ACCENT_BLUE);
        header.setForeground(TEXT_WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setReorderingAllowed(false);

        // Custom row colouring for severity tint
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    String severity = (String) t.getModel().getValueAt(r, 1);
                    if ("CRITICAL".equals(severity)) {
                        comp.setBackground(ROW_CRIT);
                    } else if ("WARNING".equals(severity)) {
                        comp.setBackground(ROW_WARN);
                    } else {
                        comp.setBackground(r % 2 == 0 ? CARD_BG : CARD_BG_ALT);
                    }
                    comp.setForeground(TEXT_WHITE);
                }
                return comp;
            }
        });
    }

    // ═══════════════════════════ HELPERS ═══════════════════════════

    private JButton makeBtn(String text, Color bg, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(TEXT_WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bg.brighter()); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        if (action != null) btn.addActionListener(action);
        return btn;
    }

    private JLabel darkLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_GRAY);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return l;
    }

    private JTextField darkField(String text) {
        JTextField f = new JTextField(text);
        f.setBackground(CARD_BG_ALT);
        f.setForeground(TEXT_WHITE);
        f.setCaretColor(TEXT_WHITE);
        f.setFont(new Font("SansSerif", Font.PLAIN, 12));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_BLUE, 1),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        return f;
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setBackground(CARD_BG_ALT);
        combo.setForeground(TEXT_WHITE);
        combo.setFont(new Font("SansSerif", Font.PLAIN, 12));
    }

    private LocalDateTime parseDate(String text) {
        if (text == null || text.isEmpty()) return null;
        try {
            return LocalDateTime.parse(text, PARSE_FMT);
        } catch (Exception e) {
            return null;
        }
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }
}
