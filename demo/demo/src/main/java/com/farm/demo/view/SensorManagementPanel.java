package com.farm.demo.view;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.farm.demo.model.*;

public class SensorManagementPanel extends JPanel {

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
    private static final Color GREEN_TINT   = new Color(0x0A, 0x2E, 0x1A);
    private static final Color AMBER_TINT   = new Color(0x2E, 0x25, 0x0A);
    private static final Color RED_TINT     = new Color(0x2E, 0x0F, 0x0A);

    private final Farm farm;
    private DefaultTableModel sensorTableModel;
    private JTable sensorTable;
    private final List<String[]> rowMeta = new ArrayList<>(); // [zoneCode, sensorCode]

    // Stats labels
    private JLabel totalLabel, activeLabel, faultyLabel;

    // Dashboard
    private JComboBox<String> zoneDashCombo;
    private JPanel dashboardCards;
    private final List<String> zoneCodesList = new ArrayList<>();

    public SensorManagementPanel(Farm farm) {
        this.farm = farm;
        setLayout(new BorderLayout(10, 10));
        setBackground(BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildStatsBar(), BorderLayout.NORTH);

        // Center: table + dashboard in a split
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildTablePanel(), buildDashboardPanel());
        split.setResizeWeight(0.65);
        split.setDividerSize(6);
        split.setBackground(BG_DARK);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        add(buildActionBar(), BorderLayout.SOUTH);

        refreshAll();
    }

    // ═══════════════════════════ A) STATS BAR ═══════════════════════════

    private JPanel buildStatsBar() {
        JPanel bar = new JPanel(new GridLayout(1, 3, 12, 0));
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 80));

        totalLabel  = new JLabel("0", SwingConstants.CENTER);
        activeLabel = new JLabel("0", SwingConstants.CENTER);
        faultyLabel = new JLabel("0", SwingConstants.CENTER);

        bar.add(makeStatCard(totalLabel,  "Total Sensors", CARD_BG));
        bar.add(makeStatCard(activeLabel, "Active",        CARD_BG));
        bar.add(makeStatCard(faultyLabel, "Faulty / Suspended", CARD_BG));
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

    // ═══════════════════════════ B) SENSOR TABLE ═══════════════════════════

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);

        String[] cols = {"Sensor Code", "Zone", "Type", "Unit",
                "Min Threshold", "Max Threshold", "Last Value", "Status"};
        sensorTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        sensorTable = new JTable(sensorTableModel);
        styleTable(sensorTable);
        sensorTable.getColumnModel().getColumn(7).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane sp = new JScrollPane(sensorTable);
        sp.getViewport().setBackground(BG_DARK);
        sp.setBorder(BorderFactory.createEmptyBorder());
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    // ═══════════════════════════ C) ACTION BAR ═══════════════════════════

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        bar.setBackground(CARD_BG);
        bar.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        bar.add(makeBtn("Suspend",    ACCENT_AMBER, e -> changeStatus(SensorStatus.SUSPENDED)));
        bar.add(makeBtn("Reactivate", ACCENT_GREEN, e -> changeStatus(SensorStatus.ACTIVE)));
        bar.add(makeBtn("Mark Faulty",ACCENT_RED,   e -> changeStatus(SensorStatus.FAULTY)));
        bar.add(makeBtn("Update Thresholds", ACCENT_BLUE, e -> openThresholdDialog()));
        bar.add(makeBtn("Add Reading",       ACCENT_BLUE, e -> openAddReadingDialog()));
        bar.add(makeBtn("View History",      ACCENT_BLUE, e -> openHistoryDialog()));
        return bar;
    }

    // ═══════════════════════════ D) DASHBOARD ═══════════════════════════

    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        JLabel title = new JLabel("  Readings Dashboard");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setForeground(TEXT_WHITE);

        zoneDashCombo = new JComboBox<>();
        zoneDashCombo.setBackground(CARD_BG);
        zoneDashCombo.setForeground(TEXT_WHITE);
        zoneDashCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        zoneDashCombo.addActionListener(e -> refreshDashboard());

        JPanel top = new JPanel(new BorderLayout(6, 0));
        top.setOpaque(false);
        top.add(title, BorderLayout.WEST);
        top.add(zoneDashCombo, BorderLayout.CENTER);
        panel.add(top, BorderLayout.NORTH);

        dashboardCards = new JPanel(new GridLayout(0, 2, 8, 8));
        dashboardCards.setOpaque(false);
        JScrollPane sp = new JScrollPane(dashboardCards);
        sp.getViewport().setBackground(BG_DARK);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    // ═══════════════════════════ ACTIONS ═══════════════════════════

    private void changeStatus(SensorStatus newStatus) {
        int row = sensorTable.getSelectedRow();
        if (row < 0) { warn("Please select a sensor first."); return; }
        String zoneCode   = rowMeta.get(row)[0];
        String sensorCode = rowMeta.get(row)[1];
        String msg = farm.changeSensorStatus(zoneCode, sensorCode, newStatus);
        JOptionPane.showMessageDialog(this, msg);
        refreshAll();
    }

    private void openThresholdDialog() {
        int row = sensorTable.getSelectedRow();
        if (row < 0) { warn("Please select a sensor first."); return; }
        String zoneCode   = rowMeta.get(row)[0];
        String sensorCode = rowMeta.get(row)[1];

        Optional<Zone> zOpt = farm.getZoneByCode(zoneCode);
        if (zOpt.isEmpty()) return;
        Optional<Sensor> sOpt = zOpt.get().getSensorByUniqueCode(sensorCode);
        if (sOpt.isEmpty()) return;
        Sensor sensor = sOpt.get();
        boolean isGPS = sensor instanceof GPSCollarSensor;

        JDialog dlg = darkDialog("Update Thresholds", 360, isGPS ? 300 : 200);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setOpaque(false);

        JTextField minF = darkField(String.valueOf(sensor.getMinThreshold()));
        JTextField maxF = darkField(String.valueOf(sensor.getMaxThreshold()));
        form.add(darkLabel("Min Threshold:")); form.add(minF);
        form.add(darkLabel("Max Threshold:")); form.add(maxF);

        JTextField minLong = null, maxLong = null;
        if (isGPS) {
            GPSCollarSensor gps = (GPSCollarSensor) sensor;
            minLong = darkField(String.valueOf(gps.getMinThresLong()));
            maxLong = darkField(String.valueOf(gps.getMaxThresLong()));
            form.add(darkLabel("Min Longitude:")); form.add(minLong);
            form.add(darkLabel("Max Longitude:")); form.add(maxLong);
        }

        JButton save = makeBtn("Save", ACCENT_BLUE, null);
        JTextField fMinLong = minLong, fMaxLong = maxLong;
        save.addActionListener(ev -> {
            try {
                double mn = Double.parseDouble(minF.getText().trim());
                double mx = Double.parseDouble(maxF.getText().trim());
                Double mnL = null, mxL = null;
                if (isGPS) {
                    mnL = Double.parseDouble(fMinLong.getText().trim());
                    mxL = Double.parseDouble(fMaxLong.getText().trim());
                }
                String msg = farm.updateSensorThresholds(zoneCode, sensorCode, mn, mx, mnL, mxL);
                JOptionPane.showMessageDialog(dlg, msg);
                dlg.dispose();
                refreshAll();
            } catch (ThresholdException ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Threshold Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Please enter valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(wrapButton(save), BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void openAddReadingDialog() {
        int row = sensorTable.getSelectedRow();
        if (row < 0) { warn("Please select a sensor first."); return; }
        String zoneCode   = rowMeta.get(row)[0];
        String sensorCode = rowMeta.get(row)[1];

        Optional<Zone> zOpt = farm.getZoneByCode(zoneCode);
        if (zOpt.isEmpty()) return;
        Optional<Sensor> sOpt = zOpt.get().getSensorByUniqueCode(sensorCode);
        if (sOpt.isEmpty()) return;
        boolean isGPS = sOpt.get() instanceof GPSCollarSensor;

        JDialog dlg = darkDialog("Add Reading", 340, isGPS ? 200 : 160);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setOpaque(false);

        JTextField valF = darkField("");
        form.add(darkLabel(isGPS ? "Latitude:" : "Value:")); form.add(valF);

        JTextField lonF = null;
        if (isGPS) {
            lonF = darkField("");
            form.add(darkLabel("Longitude:")); form.add(lonF);
        }

        JButton submit = makeBtn("Submit", ACCENT_BLUE, null);
        JTextField fLon = lonF;
        submit.addActionListener(ev -> {
            try {
                double v = Double.parseDouble(valF.getText().trim());
                String msg;
                if (isGPS) {
                    double lon = Double.parseDouble(fLon.getText().trim());
                    msg = farm.addSensorReading(zoneCode, sensorCode, v, lon);
                } else {
                    msg = farm.addSensorReading(zoneCode, sensorCode, v);
                }
                JOptionPane.showMessageDialog(dlg, msg);
                dlg.dispose();
                refreshAll();
            } catch (ThresholdException ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Threshold Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Please enter valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(wrapButton(submit), BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void openHistoryDialog() {
        int row = sensorTable.getSelectedRow();
        if (row < 0) { warn("Please select a sensor first."); return; }
        String zoneCode   = rowMeta.get(row)[0];
        String sensorCode = rowMeta.get(row)[1];

        JDialog dlg = darkDialog("Sensor Reading History", 520, 420);
        JPanel form = new JPanel(new GridLayout(1, 4, 6, 0));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JTextField startF = darkField("yyyy-MM-ddTHH:mm");
        JTextField endF   = darkField("yyyy-MM-ddTHH:mm");
        form.add(darkLabel("Start:")); form.add(startF);
        form.add(darkLabel("End:"));   form.add(endF);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setBackground(CARD_BG);
        area.setForeground(TEXT_WHITE);
        area.setFont(new Font("SansSerif", Font.PLAIN, 12));
        area.setCaretColor(TEXT_WHITE);
        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(BorderFactory.createEmptyBorder());

        JButton search = makeBtn("Search", ACCENT_BLUE, ev -> {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime start = null, end = null;
            try {
                String st = startF.getText().trim();
                if (!st.isEmpty() && !st.equals("yyyy-MM-ddTHH:mm"))
                    start = LocalDateTime.parse(st, fmt);
                String en = endF.getText().trim();
                if (!en.isEmpty() && !en.equals("yyyy-MM-ddTHH:mm"))
                    end = LocalDateTime.parse(en, fmt);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid date format. Use yyyy-MM-ddTHH:mm", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String result = farm.browseSensorReadingHistory(zoneCode, sensorCode, start, end);
            area.setText(result);
        });

        JPanel top = new JPanel(new BorderLayout(6, 0));
        top.setOpaque(false);
        top.add(form, BorderLayout.CENTER);
        top.add(search, BorderLayout.EAST);

        dlg.add(top, BorderLayout.NORTH);
        dlg.add(sp, BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    // ═══════════════════════════ REFRESH ═══════════════════════════

    public void refreshAll() {
        refreshTable();
        refreshStats();
        refreshZoneCombo();
        refreshDashboard();
    }

    private void refreshTable() {
        sensorTableModel.setRowCount(0);
        rowMeta.clear();
        for (Zone zone : farm.getZones()) {
            for (Sensor s : zone.getSensors()) {
                String lastVal = s.getLastReadingValue()
                        .map(v -> String.format("%.2f", v))
                        .orElse("No reading yet");
                sensorTableModel.addRow(new Object[]{
                        s.getUniqueCode(), zone.getCode(),
                        s.getClass().getSimpleName(), s.getUnitOfMeasurement(),
                        s.getMinThreshold(), s.getMaxThreshold(),
                        lastVal, s.getStatus().name()
                });
                rowMeta.add(new String[]{zone.getCode(), s.getUniqueCode()});
            }
        }
    }

    private void refreshStats() {
        int total = 0, active = 0, faulty = 0;
        for (Zone z : farm.getZones()) {
            for (Sensor s : z.getSensors()) {
                total++;
                if (s.getStatus() == SensorStatus.ACTIVE) active++;
                else faulty++;
            }
        }
        totalLabel.setText(String.valueOf(total));
        activeLabel.setText(String.valueOf(active));
        faultyLabel.setText(String.valueOf(faulty));
    }

    private void refreshZoneCombo() {
        zoneDashCombo.removeAllItems();
        zoneCodesList.clear();
        for (Zone z : farm.getZones()) {
            zoneDashCombo.addItem(z.getName() + " (" + z.getCode() + ")");
            zoneCodesList.add(z.getCode());
        }
    }

    private void refreshDashboard() {
        dashboardCards.removeAll();
        int idx = zoneDashCombo.getSelectedIndex();
        if (idx < 0 || idx >= zoneCodesList.size()) { dashboardCards.revalidate(); dashboardCards.repaint(); return; }
        String zc = zoneCodesList.get(idx);
        Optional<Zone> zOpt = farm.getZoneByCode(zc);
        if (zOpt.isEmpty()) return;

        for (Sensor s : zOpt.get().getSensors()) {
            Color tint = CARD_BG;
            Optional<Double> lastVal = s.getLastReadingValue();
            String valText = "No reading yet";
            if (lastVal.isPresent()) {
                double v = lastVal.get();
                valText = String.format("%.2f %s", v, s.getUnitOfMeasurement());
                Object[] st = s.getReadingStatus(v);
                String level = (String) st[1];
                if ("Critical".equals(level)) tint = RED_TINT;
                else if ("Warning".equals(level)) tint = AMBER_TINT;
                else tint = GREEN_TINT;
            }

            RoundedPanel card = new RoundedPanel(tint, 14);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

            JLabel code = new JLabel(s.getUniqueCode());
            code.setFont(new Font("SansSerif", Font.BOLD, 12));
            code.setForeground(TEXT_WHITE);
            code.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel val = new JLabel(valText);
            val.setFont(new Font("SansSerif", Font.PLAIN, 11));
            val.setForeground(TEXT_GRAY);
            val.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel badge = new JLabel(s.getStatus().name());
            badge.setFont(new Font("SansSerif", Font.BOLD, 10));
            badge.setOpaque(true);
            badge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            badge.setAlignmentX(Component.LEFT_ALIGNMENT);
            switch (s.getStatus()) {
                case ACTIVE:    badge.setForeground(ACCENT_GREEN); badge.setBackground(new Color(0x00,0x3D,0x20)); break;
                case SUSPENDED: badge.setForeground(ACCENT_AMBER); badge.setBackground(new Color(0x4E,0x39,0x00)); break;
                case FAULTY:    badge.setForeground(ACCENT_RED);   badge.setBackground(new Color(0x4A,0x0E,0x0A)); break;
            }

            card.add(code); card.add(Box.createVerticalStrut(4));
            card.add(val);  card.add(Box.createVerticalStrut(4));
            card.add(badge);
            dashboardCards.add(card);
        }
        dashboardCards.revalidate();
        dashboardCards.repaint();
    }

    // ═══════════════════════════ HELPERS ═══════════════════════════

    private void styleTable(JTable table) {
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

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                                                           boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    comp.setBackground(r % 2 == 0 ? CARD_BG : CARD_BG_ALT);
                    comp.setForeground(TEXT_WHITE);
                }
                return comp;
            }
        });
    }

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

    private JDialog darkDialog(String title, int w, int h) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dlg.setSize(w, h);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BG_DARK);
        dlg.setLayout(new BorderLayout(10, 10));
        ((JPanel) dlg.getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return dlg;
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

    private JPanel wrapButton(JButton btn) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setOpaque(false);
        p.add(btn);
        return p;
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }
}