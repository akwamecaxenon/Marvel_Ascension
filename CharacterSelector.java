import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CharacterSelector extends JPanel {
    private final GameGUI mainFrame;
    private final Map<Integer, CharacterData> heroDataMap = new HashMap<>();
    private JTextArea storyDisplay;
    private JLabel statsLabel;

    public CharacterSelector(GameGUI frame) {
        this.mainFrame = frame;
        initializeHeroData();
        setupLayout();
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(30, 30, 30));

        // Title
        JLabel header = new JLabel("SELECT YOUR AVENGER", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 36));
        header.setForeground(new Color(255, 215, 0));
        add(header, BorderLayout.NORTH);

        // Hero Selection Grid
        JPanel grid = new JPanel(new GridLayout(2, 4, 15, 15));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        for (int i = 1; i <= 8; i++) {
            CharacterData data = heroDataMap.get(i);
            grid.add(createHeroButton(data));
        }
        add(grid, BorderLayout.CENTER);

        // Hero Dossier Sidebar
        JPanel dossier = new JPanel(new BorderLayout());
        dossier.setPreferredSize(new Dimension(300, 0));
        dossier.setBackground(new Color(45, 45, 45));
        dossier.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), "HERO DOSSIER", 0, 0, null, Color.WHITE));

        storyDisplay = new JTextArea("Hover over a hero to read their story...");
        storyDisplay.setEditable(false);
        storyDisplay.setLineWrap(true);
        storyDisplay.setWrapStyleWord(true);
        storyDisplay.setBackground(new Color(45, 45, 45));
        storyDisplay.setForeground(Color.LIGHT_GRAY);
        storyDisplay.setFont(new Font("Arial", Font.ITALIC, 14));

        statsLabel = new JLabel("Stats: ???", SwingConstants.CENTER);
        statsLabel.setForeground(Color.YELLOW);
        statsLabel.setFont(new Font("Arial", Font.BOLD, 16));

        dossier.add(new JScrollPane(storyDisplay), BorderLayout.CENTER);
        dossier.add(statsLabel, BorderLayout.SOUTH);
        add(dossier, BorderLayout.EAST);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        footer.setOpaque(false);

        JButton backBtn = new JButton("BACK TO MENU");
        backBtn.addActionListener(e -> mainFrame.navigateTo("main"));

        JTextField secretInput = new JTextField(10);
        JButton unlockBtn = new JButton("Unlock Secret");
        unlockBtn.addActionListener(e -> {
            try {
                int code = Integer.parseInt(secretInput.getText());
                if(heroDataMap.containsKey(code)) startGame(heroDataMap.get(code));
                else JOptionPane.showMessageDialog(this, "Access Denied: Invalid Code");
            } catch(Exception ex) { JOptionPane.showMessageDialog(this, "Enter numeric code"); }
        });

        footer.add(backBtn);
        footer.add(new JLabel("Secret Code: ")); footer.setForeground(Color.WHITE);
        footer.add(secretInput);
        footer.add(unlockBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private JButton createHeroButton(CharacterData data) {
        JButton btn = new JButton(data.name);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setBackground(new Color(60, 60, 60));
        btn.setForeground(Color.WHITE);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                storyDisplay.setText(String.join("\n", data.storyLines).replace("\t", ""));
                statsLabel.setText("HP: " + data.hp + " | ATK: " + data.attack);
            }
        });

        btn.addActionListener(e -> startGame(data));
        return btn;
    }

    private void startGame(CharacterData hero) {
        int choice = JOptionPane.showConfirmDialog(this, "Deploy " + hero.name + "?", "Confirm Selection", JOptionPane.YES_NO_OPTION);
        if(choice == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, "Hero " + hero.name + " is ready for battle!");
        }
    }

    private void initializeHeroData() {
        heroDataMap.put(1, new CharacterData("Iron Man", 110, 15, new String[]{"Tony Stark: Genius billionaire armored hero."}));
        heroDataMap.put(2, new CharacterData("Captain America", 110, 15, new String[]{"Steve Rogers: The First Avenger."}));
        heroDataMap.put(3, new CharacterData("Thor", 110, 15, new String[]{"God of Thunder from Asgard."}));
        heroDataMap.put(4, new CharacterData("Spider-Man", 110, 15, new String[]{"Peter Parker: Your friendly neighborhood hero."}));
        heroDataMap.put(5, new CharacterData("Hulk", 110, 15, new String[]{"Bruce Banner: The strongest one there is."}));
        heroDataMap.put(6, new CharacterData("Black Widow", 100, 15, new String[]{"Natasha Romanoff: Expert assassin and spy."}));
        heroDataMap.put(7, new CharacterData("Ant-Man", 100, 15, new String[]{"Scott Lang: Small hero, big heart."}));
        heroDataMap.put(8, new CharacterData("The Falcon", 100, 15, new String[]{"Sam Wilson: Taking flight for justice."}));
        
        // Secret Unlocks
        heroDataMap.put(7355608, new CharacterData("Thanos", 500, 200, new String[]{"The Mad Titan."}));
        heroDataMap.put(69, new CharacterData("Jan Clark", 120, 15, new String[]{"The developer's ultimate hero."}));
        heroDataMap.put(420, new CharacterData("Reuben", 120, 15, new String[]{"The artist's ultimate hero."}));
        heroDataMap.put(1337, new CharacterData("Justine", 120, 15, new String[]{"The writer's ultimate hero."}));
        
       /* Add Other secret characters, like Micoh, Jaffe, & Justine*/
    }

    private static class CharacterData {
        String name; int hp; int attack; String[] storyLines;
        CharacterData(String n, int h, int a, String[] s) {
            this.name = n; this.hp = h; this.attack = a; this.storyLines = s;
        }
    }
}
