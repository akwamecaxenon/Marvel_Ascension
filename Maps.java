import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Maps extends JPanel {

    private final GameGUI mainFrame;

    private static final String[][] MAP_DATA = {
        { "Asgard",          "asgardgamebg"        },
        { "Avengers Tower",  "avengerstowercover"   },
        { "Avengers HQ",     "avengerstowerinside"  },
        { "City Court",      "citubballcourt"       },
        { "Jollibee Arena",  "jollibeeinside"       },
        { "Nyan Realm",      "nyanmap"              },
        { "Random Stage",    "randompicture"        },
        { "Sokovia",         "sokoviagamemap"       },
        { "Titan",           "titangame"            },
        { "Wakanda",         "wakandacover"         },
        { "Wakanda Inside",  "wakandainside"        },
    };

    private String  selectedMapName = null;
    private String  selectedMapKey  = null;
    private JButton selectedButton  = null;
    private final java.util.List<JButton> allMapButtons = new java.util.ArrayList<>();

    private JLabel statusBar;
    private JLabel toastLabel;
    private Timer  toastTimer;

    public Maps(GameGUI frame) {
        this.mainFrame = frame;
        setupLayout();
    }

    public void resetSelection() {
        selectedMapName = null;
        selectedMapKey  = null;
        selectedButton  = null;
        for (JButton b : allMapButtons)
            b.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80), 2));
        updateStatusBar();
    }

    public void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(20, 20, 20));

        // ── Header ─────────────────────────────────────────────────────────
        JLabel header = new JLabel("SELECT YOUR BATTLEFIELD", SwingConstants.CENTER);
        header.setFont(new Font("Verdana", Font.BOLD, 38));
        header.setForeground(new Color(255, 215, 0));
        header.setBorder(BorderFactory.createEmptyBorder(16, 0, 4, 0));
        add(header, BorderLayout.NORTH);

        // ── Full-width grid in JLayeredPane (for toast) ─────────────────────
        JLayeredPane layered = new JLayeredPane();

        JPanel grid = new JPanel(new GridLayout(3, 4, 15, 15)) {
            public Dimension getPreferredSize() { return layered.getSize(); }
        };
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        for (String[] map : MAP_DATA)
            grid.add(createMapButton(map[0], map[1]));
        grid.setBounds(0, 0, 1, 1);
        layered.add(grid, JLayeredPane.DEFAULT_LAYER);

        toastLabel = new JLabel("", SwingConstants.CENTER);
        toastLabel.setFont(new Font("Impact", Font.PLAIN, 28));
        toastLabel.setForeground(Color.WHITE);
        toastLabel.setOpaque(true);
        toastLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2),
            BorderFactory.createEmptyBorder(10, 24, 10, 24)
        ));
        toastLabel.setVisible(false);
        toastLabel.setBounds(0, 0, 1, 1);
        layered.add(toastLabel, JLayeredPane.POPUP_LAYER);

        layered.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int w = layered.getWidth(), h = layered.getHeight();
                grid.setBounds(0, 0, w, h);
                toastLabel.setBounds(0, (h - 60) / 2, w, 60);
            }
        });

        add(layered, BorderLayout.CENTER);

        // ── South: status bar + footer ──────────────────────────────────────
        JPanel south = new JPanel(new BorderLayout(4, 4));
        south.setOpaque(false);

        statusBar = new JLabel("", SwingConstants.CENTER);
        statusBar.setFont(new Font("Arial", Font.BOLD, 14));
        statusBar.setForeground(Color.WHITE);
        statusBar.setOpaque(true);
        statusBar.setBackground(new Color(25, 25, 25));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(255, 215, 0)),
            BorderFactory.createEmptyBorder(5, 0, 5, 0)
        ));
        updateStatusBar();
        south.add(statusBar, BorderLayout.NORTH);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 10));
        footer.setOpaque(false);

        JButton backBtn    = new JButton("◀  BACK");
        JButton confirmBtn = new JButton("CONFIRM STAGE  ▶");
        confirmBtn.setFont(new Font("Impact", Font.PLAIN, 20));
        confirmBtn.setForeground(new Color(255, 215, 0));
        confirmBtn.setBackground(new Color(40, 80, 40));
        confirmBtn.setFocusPainted(false);

        JTextField secretInput = new JTextField(8);
        JButton    unlockBtn   = new JButton("UNLOCK SECRET STAGE");

        backBtn.addActionListener(e -> {
            resetSelection();
            mainFrame.navigateTo("selector");
        });

        confirmBtn.addActionListener(e -> confirmStage());

        unlockBtn.addActionListener(e -> {
            try {
                int code = Integer.parseInt(secretInput.getText().trim());
                if (code == 9999) {
                    selectedMapName = "Random Stage";
                    selectedMapKey  = "randompicture";
                    updateStatusBar();
                    showToast("SECRET STAGE UNLOCKED!", new Color(100, 0, 150));
                } else {
                    JOptionPane.showMessageDialog(this, "Access Denied: Invalid Code");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter numeric access code");
            }
        });

        footer.add(backBtn);
        JLabel codeLabel = new JLabel("SECURE CODE:");
        codeLabel.setForeground(Color.WHITE);
        footer.add(codeLabel);
        footer.add(secretInput);
        footer.add(unlockBtn);
        footer.add(confirmBtn);
        south.add(footer, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);
    }

    private JButton createMapButton(String displayName, String fileKey) {
        final Image img = loadFromDisk(fileKey);

        final String initials = java.util.Arrays.stream(displayName.split(" "))
            .map(w -> String.valueOf(w.charAt(0)).toUpperCase())
            .collect(java.util.stream.Collectors.joining());

        JButton btn = new JButton() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (img != null) {
                    g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(30, 30, 45));
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(new Color(255, 215, 0));
                    Font f = new Font("Impact", Font.PLAIN, Math.min(getWidth(), getHeight()) / 3);
                    g.setFont(f);
                    FontMetrics fm = g.getFontMetrics();
                    g.drawString(initials, (getWidth() - fm.stringWidth(initials)) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                }
                g.setColor(new Color(0, 0, 0, 200));
                g.fillRect(0, getHeight() - 28, getWidth(), 28);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 11));
                FontMetrics fm = g.getFontMetrics();
                int tw = fm.stringWidth(displayName.toUpperCase());
                g.drawString(displayName.toUpperCase(), (getWidth() - tw) / 2, getHeight() - 9);
            }
        };
        btn.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80), 2));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != selectedButton)
                    btn.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
            }
            public void mouseExited(MouseEvent e) {
                if (btn != selectedButton)
                    btn.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80), 2));
            }
        });

        btn.addActionListener(e -> {
            if (selectedButton != null)
                selectedButton.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80), 2));
            selectedButton  = btn;
            selectedMapName = displayName;
            selectedMapKey  = fileKey;
            btn.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 3));
            updateStatusBar();
            showToast("STAGE LOCKED — " + displayName.toUpperCase() + "!", new Color(20, 100, 20));
        });

        allMapButtons.add(btn);
        return btn;
    }

    private void confirmStage() {
        if (selectedMapName == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a stage first!", "No Stage Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        String mode     = mainFrame.getCurrentMode();
        String heroName = mainFrame.getSelectedHeroName();
        if ("PVP".equals(mode))       mainFrame.startPvpBattle(selectedMapName);
        else if ("AI".equals(mode))   mainFrame.startGauntletBattle(heroName, selectedMapName);
        else JOptionPane.showMessageDialog(this, "ASCENSION mode coming soon!");
    }

    private void updateStatusBar() {
        if (selectedMapName != null)
            statusBar.setText("<html><center>Stage: <font color='#FFD700'><b>"
                + selectedMapName + "</b></font></center></html>");
        else
            statusBar.setText("<html><center><font color='#888888'>No stage selected</font></center></html>");
    }

    private void showToast(String message, Color bg) {
        toastLabel.setText(message);
        toastLabel.setBackground(bg);
        toastLabel.setVisible(true);
        toastLabel.getParent().repaint();
        if (toastTimer != null && toastTimer.isRunning()) toastTimer.stop();
        toastTimer = new Timer(1800, e -> toastLabel.setVisible(false));
        toastTimer.setRepeats(false);
        toastTimer.start();
    }

    private Image loadFromDisk(String fileKey) {
        java.io.File png = new java.io.File("background/" + fileKey + ".png");
        if (png.exists()) return new ImageIcon(png.getPath()).getImage();
        java.io.File jpg = new java.io.File("background/" + fileKey + ".jpg");
        if (jpg.exists()) return new ImageIcon(jpg.getPath()).getImage();
        return null;
    }

    public String getSelectedMapKey() { return selectedMapKey; }
}