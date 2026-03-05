import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CharacterSelector extends JPanel {
    private final GameGUI mainFrame;
    private final Map<Integer, CharacterData> heroDataMap = new HashMap<>();
    private JTextArea storyDisplay;
    private JLabel statsLabel;
    private JLabel gifLabel;

    public CharacterSelector(GameGUI frame) {
        this.mainFrame = frame;
        initializeHeroData();
        setupLayout();
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(20, 20, 20));

        // --- Header ---
        JLabel header = new JLabel("SELECT YOUR AVENGER", SwingConstants.CENTER);
        header.setFont(new Font("Verdana", Font.BOLD, 40));
        header.setForeground(new Color(255, 215, 0));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        // --- Hero Selection Grid ---
        JPanel grid = new JPanel(new GridLayout(2, 4, 15, 15));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        for (int i = 1; i <= 8; i++) {
            CharacterData data = heroDataMap.get(i);
            if (data != null) grid.add(createHeroButton(data));
        }
        add(grid, BorderLayout.CENTER);

        // --- Hero Dossier Sidebar ---
        JPanel dossier = new JPanel(new BorderLayout(5, 5));
        dossier.setPreferredSize(new Dimension(350, 0));
        dossier.setBackground(new Color(35, 35, 35));
        dossier.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        gifLabel = new JLabel();
        gifLabel.setPreferredSize(new Dimension(250, 250));
        gifLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dossier.add(gifLabel, BorderLayout.NORTH);

        storyDisplay = new JTextArea("Hover over a hero to read their classified dossier...");
        storyDisplay.setEditable(false);
        storyDisplay.setLineWrap(true);
        storyDisplay.setWrapStyleWord(true);
        storyDisplay.setBackground(new Color(35, 35, 35));
        storyDisplay.setForeground(Color.WHITE);
        storyDisplay.setFont(new Font("Consolas", Font.PLAIN, 13));

        statsLabel = new JLabel("<html><center>SYSTEM READY<br>WAITING FOR INPUT</center></html>", SwingConstants.CENTER);
        statsLabel.setForeground(new Color(0, 255, 255));
        statsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statsLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        dossier.add(new JScrollPane(storyDisplay), BorderLayout.CENTER);
        dossier.add(statsLabel, BorderLayout.SOUTH);
        add(dossier, BorderLayout.EAST);

        // --- Footer ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        footer.setOpaque(false);

        JButton backBtn = new JButton("BACK TO MENU");
        JTextField secretInput = new JTextField(8);
        JButton unlockBtn = new JButton("ACCESS SECRET FILES");

        backBtn.addActionListener(e -> mainFrame.navigateTo("main"));
        unlockBtn.addActionListener(e -> {
            try {
                int code = Integer.parseInt(secretInput.getText().trim());
                if(heroDataMap.containsKey(code)) startGame(heroDataMap.get(code));
                else JOptionPane.showMessageDialog(this, "Access Denied: Protocol Unauthorized");
            } catch(NumberFormatException ex) { 
                JOptionPane.showMessageDialog(this, "Enter numeric access code"); 
            }
        });

        footer.add(backBtn);
        JLabel codeLabel = new JLabel("SECURE CODE:");
        codeLabel.setForeground(Color.WHITE);
        footer.add(codeLabel);
        footer.add(secretInput);
        footer.add(unlockBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private JButton createHeroButton(CharacterData data) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon icon = new ImageIcon("images/" + data.name.replace(" ", "").toLowerCase() + ".png");
                Image img = icon.getImage();
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);

                int boxHeight = 30;
                g.setColor(new Color(0, 0, 0, 180));
                g.fillRect(0, getHeight() - boxHeight, getWidth(), boxHeight);

                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 14));
                FontMetrics fm = g.getFontMetrics();
                int textWidth = fm.stringWidth(data.name.toUpperCase());
                g.drawString(data.name.toUpperCase(), (getWidth() - textWidth) / 2, getHeight() - 10);
            }
        };

        btn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
                storyDisplay.setText(String.join("\n\n", data.storyLines));
                
                String statsHtml = "<html><body style='text-align: center; color: white;'>" +
                    "<font color='yellow'>HP: " + data.hp + " | ATK: " + data.attack + "</font><br><br>" +
                    "<b>SKILLS:</b><br>" +
                    data.skill1 + " • " + data.skill2 + " • " + data.skill3 + "<br>" +
                    "<font color='#FF4500'>ULTIMATE: " + data.ultimate + "</font></body></html>";
                statsLabel.setText(statsHtml);

                ImageIcon gifIcon = new ImageIcon("gifs/" + data.name.replace(" ", "").toLowerCase() + ".gif");
                if (gifIcon.getImage() != null) {
                    Image gifImg = gifIcon.getImage().getScaledInstance(250, 250, Image.SCALE_DEFAULT);
                    gifLabel.setIcon(new ImageIcon(gifImg));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            }
        });

        btn.addActionListener(e -> startGame(data));
        return btn;
    }

    private void startGame(CharacterData hero) {
        int choice = JOptionPane.showConfirmDialog(this, "Initiate mission with " + hero.name + "?", "Mission Briefing", JOptionPane.YES_NO_OPTION);
        if(choice == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, "Objective Loaded. Good luck, " + hero.name + ".");
        }
    }

    private void initializeHeroData() {
        // --- Core Roster ---
        heroDataMap.put(1, new CharacterData("Iron Man", 110, 18, 
            new String[]{"Tony Stark is a billionaire genius who forged a high-tech suit to escape captivity and protect the world.", "Driven by a desire to rectify his past as a weapons manufacturer, he now leads the Avengers with unmatched technological prowess."},
            "Repulsor Blast", "Micro-Missiles", "Shield Flare", "Unibeam Overload"));

        heroDataMap.put(2, new CharacterData("Captain America", 130, 15, 
            new String[]{"Once a frail volunteer, Steve Rogers was transformed by the Super-Soldier Serum into the ultimate symbol of liberty.", "After being frozen for 70 years, he leads the modern world with an unbreakable shield and an even stronger moral compass."},
            "Shield Throw", "Vibranium Bash", "Tactical Command", "Avengers Assemble"));

        heroDataMap.put(3, new CharacterData("Thor", 150, 22, 
            new String[]{"The God of Thunder and Crown Prince of Asgard, Thor wields the mystical hammer Mjolnir to command the elements.", "He fights to prove his worthiness to his father Odin while serving as the cosmic protector of both Earth and the Nine Realms."},
            "Hammer Toss", "Lightning Strike", "Thunder Clap", "God Blast"));

        heroDataMap.put(4, new CharacterData("Spider-Man", 100, 14, 
            new String[]{"Bitten by a radioactive spider, Peter Parker balances the struggles of youth with the weight of great responsibility.", "Using his genius-level intellect and spider-sense, he protects the streets of New York as a friendly neighborhood hero."},
            "Web Snare", "Spider-Sense Dodge", "Swing Kick", "Maximum Spider"));

        heroDataMap.put(5, new CharacterData("Hulk", 200, 25, 
            new String[]{"Exposure to gamma radiation cursed Dr. Bruce Banner with a monstrous alter-ego that surfaces during times of stress and anger.", "As the strongest Avenger, the Hulk possesses near-limitless physical strength that increases the more enraged he becomes."},
            "Gamma Punch", "Thunderclap", "Ground Smash", "Worldbreaker Slam"));

        heroDataMap.put(6, new CharacterData("Black Widow", 95, 20, 
            new String[]{"A former KGB assassin trained in the infamous Red Room, Natasha Romanoff is the world's most elite spy and infiltration expert.", "Having cleared the red from her ledger, she now uses her lethal skills to tackle threats that require a shadow's touch."},
            "Widow's Bite", "Dual Pistols", "Staff Strike", "Lullaby Takedown"));

        heroDataMap.put(7, new CharacterData("Ant-Man", 105, 13, 
            new String[]{"Master thief turned hero Scott Lang uses the Pym Particle to manipulate his size while retaining immense physical density.", "Whether leading an army of ants or growing to the size of a skyscraper, he proves that size isn't everything in battle."},
            "Size Shift", "Ant Swarm", "Pym Disk", "Giant-Man Stomp"));

        heroDataMap.put(8, new CharacterData("The Falcon", 110, 16, 
            new String[]{"Sam Wilson is a veteran pararescueman who takes to the skies using a high-tech winged flight suit of his own design.", "With his loyal drone Redwing and an unwavering sense of duty, he provides essential aerial support to the Avengers roster."},
            "Wing Shield", "Redwing Strike", "Aerial Dive", "Flight Form Alpha"));

        // --- Secret Unlocks ---
        heroDataMap.put(7355608, new CharacterData("Thanos", 500, 50, 
            new String[]{"The Mad Titan seeks the Infinity Stones to achieve his twisted vision of universal balance through mass eradication.", "Armed with the Infinity Gauntlet, he possesses the power to rewrite the very laws of space, time, and reality."},
            "Titan Punch", "Energy Beam", "Reality Warp", "The Snap"));

        heroDataMap.put(69, new CharacterData("Jan Clark", 120, 15, 
            new String[]{"A legendary architect of code, Jan Clark manipulates the game's logic as if it were a physical weapon.", "Known as the Developer's Hero, they can debug any enemy out of existence with a single precise keystroke."},
            "Code Injection", "Debug Strike", "Compile Error", "System Overwrite"));

        heroDataMap.put(420, new CharacterData("Reuben", 120, 15, 
            new String[]{"Reuben is a master of visual reality, bringing sketches to life with a brush that can paint the path to victory.", "The Artist's Hero utilizes color and shadow to confuse foes and inspire allies in the heat of combat."},
            "Ink Splash", "Canvas Shield", "Vivid Stroke", "Masterpiece Finale"));

        heroDataMap.put(1337, new CharacterData("Justine", 120, 15, 
            new String[]{"Justine weaves the narrative of battle, ensuring that every victory is written in the stars before the fight even begins.", "As the Writer's Hero, she can alter the fate of her teammates by rewriting their story in real-time."},
            "Plot Armor", "Script Revision", "Dramatic Hook", "The Final Chapter"));
    }

    private static class CharacterData {
        String name; 
        int hp; 
        int attack; 
        String[] storyLines;
        String skill1, skill2, skill3, ultimate;

        CharacterData(String n, int h, int a, String[] s, String s1, String s2, String s3, String ult) {
            this.name = n; 
            this.hp = h; 
            this.attack = a; 
            this.storyLines = s;
            this.skill1 = s1;
            this.skill2 = s2;
            this.skill3 = s3;
            this.ultimate = ult;
        }
    }
}
