import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharacterSelector extends JPanel implements Interfaces.GameScreen {
    private final GameGUI mainFrame;
    private final Map<Integer, CharacterData> heroDataMap = new HashMap<>();
    private JTextArea storyDisplay;
    private JLabel    statsLabel;
    private JLabel    gifLabel;
    private JLabel    statusBar;

    // ── Selection state ────────────────────────────────────────────────────
    private CharacterData p1Selection = null;
    private CharacterData p2Selection = null;
    private JButton       p1Button    = null;
    private JButton       p2Button    = null;

    // Track every hero button so we can do a full border reset
    private final List<JButton> allHeroButtons = new ArrayList<>();

    // ── Toast banner overlay ───────────────────────────────────────────────
    // Sits on top of the grid via a layered JLayeredPane
    private JLabel   toastLabel;
    private Timer    toastTimer;

    public CharacterSelector(GameGUI frame) {
        this.mainFrame = frame;
        initializeHeroData();
        setupLayout();
    }


    public void resetSelections() {
        p1Selection = null;
        p2Selection = null;
        p1Button = null;
        p2Button = null;
        // Reset every hero button border to neutral
        for (JButton b : allHeroButtons)
            b.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        updateStatusBar();
    }

    // ── Show a toast overlay on the grid ──────────────────────────────────
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

    @Override
    public void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(20, 20, 20));

        // ── Header ─────────────────────────────────────────────────────────
        JLabel header = new JLabel("SELECT YOUR AVENGERS", SwingConstants.CENTER);
        header.setFont(new Font("Verdana", Font.BOLD, 38));
        header.setForeground(new Color(255, 215, 0));
        header.setBorder(BorderFactory.createEmptyBorder(16, 0, 4, 0));
        add(header, BorderLayout.NORTH);

        // ── Hero grid inside a JLayeredPane so the toast floats on top ─────
        JLayeredPane layered = new JLayeredPane();
        layered.setPreferredSize(new Dimension(600, 300)); // flexible; layout will stretch it

        // The actual grid
        JPanel grid = new JPanel(new GridLayout(2, 4, 15, 15)) {
            @Override public Dimension getPreferredSize() { return layered.getSize(); }
        };
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        for (int i = 1; i <= 8; i++) {
            CharacterData data = heroDataMap.get(i);
            if (data != null) grid.add(createHeroButton(data));
        }
        grid.setBounds(0, 0, 1, 1); // will be resized by componentResized
        layered.add(grid, JLayeredPane.DEFAULT_LAYER);

        // The toast label — centred over the grid, hidden by default
        toastLabel = new JLabel("", SwingConstants.CENTER);
        toastLabel.setFont(new Font("Impact", Font.PLAIN, 28));
        toastLabel.setForeground(Color.WHITE);
        toastLabel.setOpaque(true);
        toastLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2),
            BorderFactory.createEmptyBorder(10, 24, 10, 24)
        ));
        toastLabel.setVisible(false);
        toastLabel.setBounds(0, 0, 1, 1); // resized below
        layered.add(toastLabel, JLayeredPane.POPUP_LAYER);

        // Keep grid and toast sized to the layered pane
        layered.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = layered.getWidth();
                int h = layered.getHeight();
                grid.setBounds(0, 0, w, h);

                // Toast: full-width strip centred vertically
                int th = 60;
                int ty = (h - th) / 2;
                toastLabel.setBounds(0, ty, w, th);
            }
        });

        add(layered, BorderLayout.CENTER);

        // ── Right dossier sidebar ──────────────────────────────────────────
        JPanel dossier = new JPanel(new BorderLayout(5, 5));
        dossier.setPreferredSize(new Dimension(310, 0));
        dossier.setBackground(new Color(35, 35, 35));
        dossier.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        gifLabel = new JLabel();
        gifLabel.setPreferredSize(new Dimension(210, 190));
        gifLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dossier.add(gifLabel, BorderLayout.NORTH);

        storyDisplay = new JTextArea("Hover over a hero to read their classified dossier...");
        storyDisplay.setEditable(false);
        storyDisplay.setLineWrap(true);
        storyDisplay.setWrapStyleWord(true);
        storyDisplay.setBackground(new Color(35, 35, 35));
        storyDisplay.setForeground(Color.WHITE);
        storyDisplay.setFont(new Font("Consolas", Font.PLAIN, 12));

        statsLabel = new JLabel("<html><center>SYSTEM READY<br>WAITING FOR INPUT</center></html>", SwingConstants.CENTER);
        statsLabel.setForeground(new Color(0, 255, 255));
        statsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statsLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        dossier.add(new JScrollPane(storyDisplay), BorderLayout.CENTER);
        dossier.add(statsLabel, BorderLayout.SOUTH);
        add(dossier, BorderLayout.EAST);

        // ── South: status bar + footer ─────────────────────────────────────
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

        JButton backBtn    = new JButton("BACK TO MENU");
        JButton proceedBtn = new JButton("PROCEED TO MAP  ▶");
        proceedBtn.setFont(new Font("Impact", Font.PLAIN, 20));
        proceedBtn.setForeground(new Color(255, 215, 0));
        proceedBtn.setBackground(new Color(40, 80, 40));
        proceedBtn.setFocusPainted(false);

        JTextField secretInput = new JTextField(8);
        JButton    unlockBtn   = new JButton("ACCESS SECRET FILES");

        backBtn.addActionListener(e -> {
            resetSelections();
            mainFrame.navigateTo("main");
        });

        proceedBtn.addActionListener(e -> {
            String mode = mainFrame.getCurrentMode();
            if ("PVP".equals(mode)) {
                if (p1Selection == null || p2Selection == null) {
                    JOptionPane.showMessageDialog(this,
                        "<html>Both players must select a hero!<br>"
                        + "P1: click first — GREEN border<br>"
                        + "P2: click second — RED border</html>",
                        "Selection Incomplete", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                mainFrame.setSelectedHero(p1Selection.name);
                mainFrame.setSelectedHero2(p2Selection.name);
            } else {
                if (p1Selection == null) {
                    JOptionPane.showMessageDialog(this,
                        "Select a hero first!", "Selection Incomplete", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                mainFrame.setSelectedHero(p1Selection.name);
            }
            mainFrame.navigateTo("maps");
        });

        unlockBtn.addActionListener(e -> {
            try {
                int code = Integer.parseInt(secretInput.getText().trim());
                if (heroDataMap.containsKey(code)) handleHeroClick(heroDataMap.get(code), null);
                else JOptionPane.showMessageDialog(this, "Access Denied: Protocol Unauthorized");
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
        footer.add(proceedBtn);
        south.add(footer, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);
    }

    // ── Hero button ────────────────────────────────────────────────────────
    private JButton createHeroButton(CharacterData data) {
        // Pre-load cover image once — avoids reloading on every repaint
        final Image coverImage = loadCoverImage(data.name);

        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (coverImage != null) {
                    // Draw cover photo scaled to fill the button
                    g.drawImage(coverImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Fallback — solid dark background with hero initials
                    g.setColor(new Color(30, 30, 45));
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(new Color(255, 215, 0));
                    g.setFont(new Font("Impact", Font.PLAIN, 36));
                    String initials = getInitials(data.name);
                    FontMetrics fmI = g.getFontMetrics();
                    int iw = fmI.stringWidth(initials);
                    g.drawString(initials, (getWidth() - iw) / 2, getHeight() / 2 + 12);
                }

                // Dark strip at the bottom for the name label
                g.setColor(new Color(0, 0, 0, 200));
                g.fillRect(0, getHeight() - 30, getWidth(), 30);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 13));
                FontMetrics fm = g.getFontMetrics();
                int tw = fm.stringWidth(data.name.toUpperCase());
                g.drawString(data.name.toUpperCase(), (getWidth() - tw) / 2, getHeight() - 9);
            }
        };
        btn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn != p1Button && btn != p2Button)
                    btn.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
                storyDisplay.setText(String.join("\n\n", data.storyLines));
                statsLabel.setText("<html><body style='text-align:center;color:white;'>"
                    + "<font color='yellow'>HP: " + data.hp + " | ATK: " + data.attack + "</font><br><br>"
                    + "<b>SKILLS:</b><br>" + data.skill1 + " • " + data.skill2 + " • " + data.skill3 + "<br>"
                    + "<font color='#FF4500'>ULTIMATE: " + data.ultimate + "</font></body></html>");
                // Show gif if available, otherwise fall back to cover photo in sidebar
                loadSidebarPortrait(data.name);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if      (btn == p1Button) btn.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 0), 4));
                else if (btn == p2Button) btn.setBorder(BorderFactory.createLineBorder(new Color(220, 0, 0), 4));
                else                     btn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            }
        });

        btn.addActionListener(e -> handleHeroClick(data, btn));
        allHeroButtons.add(btn);
        return btn;
    }

    // ── Image helpers ──────────────────────────────────────────────────────

    /**
     * Converts a hero display name to the matching filename key.
     * Matches your actual filenames in the picture/ folder:
     *   "Iron Man"        → "ironman"
     *   "Captain America" → "captainamerica"
     *   "Spider-Man"      → "spider-man"
     *   "Ant-Man"         → "ant-man"
     */
    private String toImageKey(String heroName) {
        switch (heroName) {
            case "Iron Man":        return "ironman";
            case "Captain America": return "captainamerica";
            case "Thor":            return "thor";
            case "Spider-Man":      return "spider-man";
            case "Hulk":            return "hulk";
            case "Black Widow":     return "blackwidow";
            case "Ant-Man":         return "ant-man";
            case "The Falcon":      return "thefalcon";
            case "Thanos":          return "thanos";
            case "Jan Clark":       return "janclark";
            case "Reuben":          return "reuben";
            case "Justine":         return "justine";
            default:                return heroName.replace(" ", "").toLowerCase();
        }
    }

    /**
     * Load a cover photo for the given hero name.
     * Looks in picture/ folder — tries .png then .jpg, returns null if missing.
     */
    private Image loadCoverImage(String heroName) {
        String key = toImageKey(heroName);
        java.io.File png = new java.io.File("picture/" + key + ".png");
        if (png.exists()) return new ImageIcon("picture/" + key + ".png").getImage();
        java.io.File jpg = new java.io.File("picture/" + key + ".jpg");
        if (jpg.exists()) return new ImageIcon("picture/" + key + ".jpg").getImage();
        return null; // triggers initials fallback in paintComponent
    }

    /**
     * Load the sidebar portrait for a hero.
     * Priority: gifs/<key>.gif → picture/<key>.png → picture/<key>.jpg → placeholder
     */
    private void loadSidebarPortrait(String heroName) {
        String key = toImageKey(heroName);

        java.io.File gif = new java.io.File("gifs/" + key + ".gif");
        if (gif.exists()) {
            Image img = new ImageIcon("gifs/" + key + ".gif")
                .getImage().getScaledInstance(210, 210, Image.SCALE_DEFAULT);
            gifLabel.setIcon(new ImageIcon(img));
            gifLabel.setText("");
            return;
        }

        java.io.File png = new java.io.File("picture/" + key + ".png");
        if (png.exists()) {
            Image img = new ImageIcon("picture/" + key + ".png")
                .getImage().getScaledInstance(210, 210, Image.SCALE_SMOOTH);
            gifLabel.setIcon(new ImageIcon(img));
            gifLabel.setText("");
            return;
        }

        java.io.File jpg = new java.io.File("picture/" + key + ".jpg");
        if (jpg.exists()) {
            Image img = new ImageIcon("picture/" + key + ".jpg")
                .getImage().getScaledInstance(210, 210, Image.SCALE_SMOOTH);
            gifLabel.setIcon(new ImageIcon(img));
            gifLabel.setText("");
            return;
        }

        // No image found — show initials as placeholder in sidebar
        gifLabel.setIcon(null);
        gifLabel.setText("<html><center><font color='#FFD700' size='6'>"
            + getInitials(heroName) + "</font><br>"
            + "<font color='#888888' size='2'>No image found</font></center></html>");
        gifLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    /** Returns the first letter of each word in a hero name (e.g. "Iron Man" → "IM"). */
    private String getInitials(String name) {
        StringBuilder sb = new StringBuilder();
        for (String word : name.split(" ")) {
            if (!word.isEmpty()) sb.append(word.charAt(0));
        }
        return sb.toString().toUpperCase();
    }

    // ── Selection logic ────────────────────────────────────────────────────
    private void handleHeroClick(CharacterData data, JButton btn) {
        String mode = mainFrame.getCurrentMode();

        // Non-PVP: single selection for P1 only
        if (!"PVP".equals(mode)) {
            // Already have a pick — ask before swapping
            if (p1Selection != null && !data.name.equals(p1Selection.name)) {
                String[] opts = {
                    "Change  (was: " + p1Selection.name + ")",
                    "Cancel"
                };
                int choice = JOptionPane.showOptionDialog(this,
                    "You already selected " + p1Selection.name + ".\nSwitch to " + data.name + "?",
                    "Re-Select Hero", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, opts, opts[1]);
                if (choice != 0) return;
            }
            // Clear old button's green border before reassigning
            if (p1Button != null) p1Button.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            p1Selection = data;
            p1Button    = btn;
            if (btn != null) btn.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 0), 4));
            updateStatusBar();
            showToast("SELECTED — " + data.name + " LOCKED IN!", new Color(20, 130, 20));
            return;
        }

        // PVP: P1 first, then P2
        if (p1Selection == null) {
            p1Selection = data;
            p1Button    = btn;
            if (btn != null) btn.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 0), 4));
            updateStatusBar();
            showToast("PLAYER 1 — " + data.name + " LOCKED IN!", new Color(10, 140, 10));

        } else if (p2Selection == null) {
            p2Selection = data;
            p2Button    = btn;
            // Same button as P1? Show both colours with a split border (green wins, red outline)
            if (btn == p1Button) {
                btn.setBorder(BorderFactory.createLineBorder(new Color(220, 0, 0), 4));
            } else {
                btn.setBorder(BorderFactory.createLineBorder(new Color(220, 0, 0), 4));
            }
            updateStatusBar();
            showToast("PLAYER 2 — " + data.name + " LOCKED IN!", new Color(170, 20, 20));

        } else {
            // Both already chosen — offer re-pick
            String[] opts = {
                "Change P1  (was: " + p1Selection.name + ")",
                "Change P2  (was: " + p2Selection.name + ")",
                "Cancel"
            };
            int choice = JOptionPane.showOptionDialog(this,
                "Both slots are filled. Which do you want to change?",
                "Re-Select Hero", JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, opts, opts[2]);

            if (choice == 0) {
                // Clear old P1 button border
                // If P2 still owns that same button, restore P2's red; otherwise go dark
                if (p1Button != null) {
                    if (p1Button == p2Button)
                        p1Button.setBorder(BorderFactory.createLineBorder(new Color(220, 0, 0), 4));
                    else
                        p1Button.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
                }
                p1Selection = data; p1Button = btn;
                if (btn != null) btn.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 0), 4));
                updateStatusBar();
                showToast("PLAYER 1 — " + data.name + " LOCKED IN!", new Color(10, 140, 10));

            } else if (choice == 1) {
                // Clear old P2 button border
                // If P1 still owns that same button, restore P1's green; otherwise go dark
                if (p2Button != null) {
                    if (p2Button == p1Button)
                        p2Button.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 0), 4));
                    else
                        p2Button.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
                }
                p2Selection = data; p2Button = btn;
                if (btn != null) btn.setBorder(BorderFactory.createLineBorder(new Color(220, 0, 0), 4));
                updateStatusBar();
                showToast("PLAYER 2 — " + data.name + " LOCKED IN!", new Color(170, 20, 20));
            }
        }
    }

    /** Restore a button border to the correct state after deselection */
   

    // ── Status bar ─────────────────────────────────────────────────────────
    private void updateStatusBar() {
        String mode    = mainFrame.getCurrentMode();
        String p1Name  = (p1Selection != null) ? p1Selection.name : "---";
        String p1Color = (p1Selection != null) ? "#00dd00" : "#888888";

        if ("PVP".equals(mode)) {
            String p2Name  = (p2Selection != null) ? p2Selection.name : "---";
            String p2Color = (p2Selection != null) ? "#ee3333" : "#888888";
            statusBar.setText("<html><center>"
                + "P1: <font color='" + p1Color + "'><b>" + p1Name + "</b></font>"
                + " &nbsp;&nbsp;|&nbsp;&nbsp; "
                + "P2: <font color='" + p2Color + "'><b>" + p2Name + "</b></font>"
                + "</center></html>");
        } else {
            statusBar.setText("<html><center>Selected: <font color='"
                + p1Color + "'><b>" + p1Name + "</b></font></center></html>");
        }
    }

    // Public getters for battle screens
    public CharacterData getP1Data() { return p1Selection; }
    public CharacterData getP2Data() { return p2Selection; }

    // ── Hero data ──────────────────────────────────────────────────────────
    private void initializeHeroData() {
        heroDataMap.put(1, new CharacterData("Iron Man", 110, 18,
            new String[]{"Tony Stark is a billionaire genius who forged a high-tech suit to escape captivity and protect the world.",
                "Driven by a desire to rectify his past as a weapons manufacturer, he leads the Avengers with unmatched prowess."},
            "Repulsor Blast", "Micro-Missiles", "Shield Flare", "Unibeam Overload"));
        heroDataMap.put(2, new CharacterData("Captain America", 130, 15,
            new String[]{"Once a frail volunteer, Steve Rogers was transformed by the Super-Soldier Serum into the ultimate symbol of liberty.",
                "After being frozen for 70 years, he leads the modern world with an unbreakable shield and an even stronger moral compass."},
            "Shield Throw", "Vibranium Bash", "Tactical Command", "Avengers Assemble"));
        heroDataMap.put(3, new CharacterData("Thor", 150, 22,
            new String[]{"The God of Thunder and Crown Prince of Asgard, Thor wields Mjolnir to command the elements.",
                "He fights to prove his worthiness while serving as the cosmic protector of both Earth and the Nine Realms."},
            "Hammer Toss", "Lightning Strike", "Thunder Clap", "God Blast"));
        heroDataMap.put(4, new CharacterData("Spider-Man", 100, 14,
            new String[]{"Bitten by a radioactive spider, Peter Parker balances the struggles of youth with the weight of great responsibility.",
                "Using his genius intellect and spider-sense, he protects New York as a friendly neighborhood hero."},
            "Web Snare", "Spider-Sense Dodge", "Swing Kick", "Maximum Spider"));
        heroDataMap.put(5, new CharacterData("Hulk", 200, 25,
            new String[]{"Exposure to gamma radiation cursed Dr. Bruce Banner with a monstrous alter-ego that surfaces under stress.",
                "As the strongest Avenger, the Hulk possesses near-limitless strength that increases the more enraged he becomes."},
            "Gamma Punch", "Thunderclap", "Ground Smash", "Worldbreaker Slam"));
        heroDataMap.put(6, new CharacterData("Black Widow", 95, 20,
            new String[]{"A former KGB assassin trained in the Red Room, Natasha Romanoff is the world's most elite spy.",
                "She now uses her lethal skills to tackle threats that require a shadow's touch."},
            "Widow's Bite", "Dual Pistols", "Staff Strike", "Lullaby Takedown"));
        heroDataMap.put(7, new CharacterData("Ant-Man", 105, 13,
            new String[]{"Master thief turned hero Scott Lang uses Pym Particles to manipulate his size while retaining immense density.",
                "Whether shrinking or growing to skyscraper size, he proves size isn't everything in battle."},
            "Size Shift", "Ant Swarm", "Pym Disk", "Giant-Man Stomp"));
        heroDataMap.put(8, new CharacterData("The Falcon", 110, 16,
            new String[]{"Sam Wilson is a veteran pararescueman who takes to the skies with a high-tech winged flight suit.",
                "With his drone Redwing and unwavering duty, he provides essential aerial support to the Avengers."},
            "Wing Shield", "Redwing Strike", "Aerial Dive", "Flight Form Alpha"));
        // Secrets
        heroDataMap.put(7355608, new CharacterData("Thanos", 500, 50,
            new String[]{"The Mad Titan seeks the Infinity Stones to achieve his twisted vision of balance.",
                "Armed with the Infinity Gauntlet, he can rewrite the very laws of space, time, and reality."},
            "Titan Punch", "Energy Beam", "Reality Warp", "The Snap"));
        heroDataMap.put(69, new CharacterData("Jan Clark", 120, 15,
            new String[]{"A legendary architect of code who manipulates game logic as a physical weapon.",
                "Known as the Developer's Hero — can debug any enemy out of existence with a keystroke."},
            "Code Injection", "Debug Strike", "Compile Error", "System Overwrite"));
        heroDataMap.put(420, new CharacterData("Reuben", 120, 15,
            new String[]{"Reuben is a master of visual reality, bringing sketches to life with a brush that paints the path to victory.",
                "The Artist's Hero utilizes color and shadow to confuse foes and inspire allies."},
            "Ink Splash", "Canvas Shield", "Vivid Stroke", "Masterpiece Finale"));
        heroDataMap.put(1337, new CharacterData("Justine", 120, 15,
            new String[]{"Justine weaves the narrative of battle, ensuring every victory is written in the stars.",
                "As the Writer's Hero, she can alter the fate of teammates by rewriting their story in real-time."},
            "Plot Armor", "Script Revision", "Dramatic Hook", "The Final Chapter"));
    }

    // ── Data class ─────────────────────────────────────────────────────────
    static class CharacterData {
        String name; int hp, attack; String[] storyLines;
        String skill1, skill2, skill3, ultimate;
        CharacterData(String n, int h, int a, String[] s,
                      String s1, String s2, String s3, String ult) {
            name=n; hp=h; attack=a; storyLines=s;
            skill1=s1; skill2=s2; skill3=s3; ultimate=ult;
        }
    }
}