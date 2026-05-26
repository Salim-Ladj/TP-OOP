import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Main application window for the Smart Farm GUI.
 * Uses a left sidebar navigation and a CardLayout content area.
 */
public class SmartFarmGUI extends JFrame {

    private static final Color BG_DARK     = new Color(0x0A, 0x16, 0x28);
    private static final Color SIDEBAR_BG  = new Color(0x07, 0x10, 0x1E);
    private static final Color ACCENT_BLUE = new Color(0x15, 0x65, 0xC0);
    private static final Color HOVER_BG    = new Color(0x0D, 0x21, 0x37);
    private static final Color TEXT_WHITE   = Color.WHITE;
    private static final Color TEXT_GRAY    = new Color(0x77, 0x88, 0x99);

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private JButton activeSidebarBtn = null;

    public SmartFarmGUI(Farm farm) {
        setTitle("Smart Farm — Management Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        // ── Sidebar ──
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Logo / title area
        JLabel logo = new JLabel("  \uD83C\uDF3E Smart Farm", SwingConstants.LEFT);
        logo.setFont(new Font("SansSerif", Font.BOLD, 18));
        logo.setForeground(TEXT_WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(14, 16, 20, 10));
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        sidebar.add(logo);

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x1A, 0x33, 0x50));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(10));

        // Navigation buttons
        JButton sensorsBtn = makeSidebarButton("\uD83D\uDCE1  Sensors", "sensors");
        JButton alertsBtn  = makeSidebarButton("\u26A0\uFE0F  Alerts",  "alerts");

        sidebar.add(sensorsBtn);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(alertsBtn);
        sidebar.add(Box.createVerticalGlue());

        // Version label at bottom
        JLabel version = new JLabel("  v1.0.0");
        version.setFont(new Font("SansSerif", Font.PLAIN, 10));
        version.setForeground(TEXT_GRAY);
        version.setAlignmentX(Component.LEFT_ALIGNMENT);
        version.setBorder(BorderFactory.createEmptyBorder(0, 16, 8, 0));
        sidebar.add(version);

        add(sidebar, BorderLayout.WEST);

        // ── Content area ──
        contentPanel.setBackground(BG_DARK);

        // Welcome panel
        JPanel welcome = new JPanel(new GridBagLayout());
        welcome.setBackground(BG_DARK);
        JLabel wLabel = new JLabel("Select a panel from the sidebar");
        wLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        wLabel.setForeground(TEXT_GRAY);
        welcome.add(wLabel);

        SensorManagementPanel sensorPanel = new SensorManagementPanel(farm);
        AlertManagementPanel  alertPanel  = new AlertManagementPanel(farm);

        contentPanel.add(welcome,     "welcome");
        contentPanel.add(sensorPanel, "sensors");
        contentPanel.add(alertPanel,  "alerts");

        add(contentPanel, BorderLayout.CENTER);

        // Select sensors by default
        cardLayout.show(contentPanel, "sensors");
        setActiveButton(sensorsBtn);
    }

    private JButton makeSidebarButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(TEXT_GRAY);
        btn.setBackground(SIDEBAR_BG);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn != activeSidebarBtn) btn.setBackground(HOVER_BG);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn != activeSidebarBtn) btn.setBackground(SIDEBAR_BG);
            }
        });

        btn.addActionListener(e -> {
            cardLayout.show(contentPanel, cardName);
            setActiveButton(btn);
        });

        return btn;
    }

    private void setActiveButton(JButton btn) {
        if (activeSidebarBtn != null) {
            activeSidebarBtn.setBackground(SIDEBAR_BG);
            activeSidebarBtn.setForeground(TEXT_GRAY);
        }
        activeSidebarBtn = btn;
        btn.setBackground(ACCENT_BLUE);
        btn.setForeground(TEXT_WHITE);
    }

    // ═══════════════════════════ MAIN ═══════════════════════════

    public static void main(String[] args) {
        // Set up a demo farm with sample data
        try {
            Farm farm = new Farm("FARM-001", "ESI Green Valley");

            CropZone cropZone = new CropZone("CZ-01", "North Crop Field");
            LivestockZone livestockZone = new LivestockZone("LZ-01", "East Livestock Area");
            AquacultureZone aquaZone = new AquacultureZone("AZ-01", "South Aquaculture Tank");

            farm.addZone(cropZone);
            farm.addZone(livestockZone);
            farm.addZone(aquaZone);

            TemperatureSensor tempSensor = new TemperatureSensor("TEMP-CZ01", "CZ-01", 10.0, 35.0, "°C");
            HumiditySensor humiditySensor = new HumiditySensor("HUMI-CZ01", "CZ-01", 30.0, 80.0, "%");
            SoilSensor soilSensor = new SoilSensor("SOIL-CZ01", "CZ-01", 5.5, 7.5, "pH");
            BiometricSensor bioSensor = new BiometricSensor("BIO-LZ01", "LZ-01", 36.0, 39.5, "°C");
            GPSCollarSensor gpsSensor = new GPSCollarSensor("GPS-LZ01", "LZ-01", 33.0, 35.0, 2.0, 4.0, "°");
            WaterTemperatureSensor waterTempSensor = new WaterTemperatureSensor("WTEMP-AZ01", "AZ-01", 18.0, 28.0, "°C");
            DissolvedOxygenSensor dissolvedOxygenSensor = new DissolvedOxygenSensor("DOX-AZ01", "AZ-01", 6.0, 12.0, "mg/L");

            cropZone.addSensor(tempSensor);
            cropZone.addSensor(humiditySensor);
            cropZone.addSensor(soilSensor);
            livestockZone.addSensor(bioSensor);
            livestockZone.addSensor(gpsSensor);
            aquaZone.addSensor(waterTempSensor);
            aquaZone.addSensor(dissolvedOxygenSensor);

            // Add some readings to generate alerts
            tempSensor.addReading(22.0);
            humiditySensor.addReading(55.0);
            soilSensor.addReading(6.5);
            bioSensor.addReading(38.0);
            waterTempSensor.addReading(23.0);
            dissolvedOxygenSensor.addReading(8.0);
            gpsSensor.addGPSReading(34.0, 3.0);

            // Warning readings
            tempSensor.addReading(38.0);
            humiditySensor.addReading(25.0);

            // Critical readings
            bioSensor.addReading(50.0);
            waterTempSensor.addReading(5.0);
            dissolvedOxygenSensor.addReading(0.5);
            gpsSensor.addGPSReading(10.0, 20.0);

            SwingUtilities.invokeLater(() -> {
                SmartFarmGUI gui = new SmartFarmGUI(farm);
                gui.setVisible(true);
            });

        } catch (ThresholdException e) {
            e.printStackTrace();
        }
    }
}
