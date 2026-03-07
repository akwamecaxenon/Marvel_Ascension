import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Maps extends JPanel {
    private final GameGUI mainFrame;
    private final List<MapData> allMaps = new ArrayList<>();
    private final Map<Integer, MapData> secretMapsMap = new HashMap<>();
    private MapData selectedMap;
    private JLabel selectedLabel;

    public Maps(GameGUI frame) {
        this.mainFrame = frame;
        initializeMapData();
        setupLayout();
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(20, 20, 20));

        // --- Header + Random ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel header = new JLabel("SELECT BATTLE STAGE", SwingConstants.CENTER);
        header.setFont(new Font("Verdana", Font.BOLD, 40));
        header.setForeground(new Color(255, 215, 0));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        topPanel.add(header, BorderLayout.NORTH);

        JPanel randomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        randomPanel.setOpaque(false);
        JButton randomBtn = createMapButton(new MapData("RANDOM", "Random stage will be selected at battle start.", true));
        randomBtn.setPreferredSize(new Dimension(280, 70));
        randomBtn.setFont(new Font("Verdana", Font.BOLD, 22));
        randomPanel.add(randomBtn);
        topPanel.add(randomPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // --- Map Grid ---
        JPanel grid = new JPanel(new GridLayout(2, 4, 15, 15));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        for (MapData data : allMaps) {
            if (!data.isRandom) grid.add(createMapButton(data));
        }
        add(grid, BorderLayout.CENTER);

        // --- Selected Stage Display ---
        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(5, 20, 15, 20));

        selectedLabel = new JLabel("<html><center>SELECT A STAGE OR USE RANDOM</center></html>", SwingConstants.CENTER);
        selectedLabel.setFont(new Font("Consolas", Font.BOLD, 16));
        selectedLabel.setForeground(new Color(0, 255, 255));
        selectedLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 0), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        bottom.add(selectedLabel, BorderLayout.CENTER);

        // --- Footer Buttons ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        footer.setOpaque(false);

        JButton backBtn = new JButton("BACK TO SELECTOR");
        backBtn.setFont(new Font("Arial", Font.BOLD, 16));
        backBtn.addActionListener(e -> mainFrame.navigateTo("selector"));

        JTextField secretInput = new JTextField(8);
        JButton unlockBtn = new JButton("ACCESS SECRET STAGE");
        unlockBtn.addActionListener(e -> {
            try {
                int code = Integer.parseInt(secretInput.getText().trim());
                if (secretMapsMap.containsKey(code)) {
                    selectedMap = secretMapsMap.get(code);
                    updateSelectedLabel();
                    highlightSelectedButton(null);
                    JOptionPane.showMessageDialog(this, "Secret stage unlocked: " + selectedMap.name);
                } else {
                    JOptionPane.showMessageDialog(this, "Access Denied: Protocol Unauthorized");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter numeric access code");
            }
        });

        JButton confirmBtn = new JButton("START BATTLE");
        confirmBtn.setFont(new Font("Arial", Font.BOLD, 18));
        confirmBtn.setForeground(new Color(255, 215, 0));
        confirmBtn.addActionListener(e -> confirmSelection());

        footer.add(backBtn);
        JLabel codeLabel = new JLabel("SECURE CODE:");
        codeLabel.setForeground(Color.WHITE);
        footer.add(codeLabel);
        footer.add(secretInput);
        footer.add(unlockBtn);
        footer.add(confirmBtn);
        bottom.add(footer, BorderLayout.SOUTH);

        add(bottom, BorderLayout.SOUTH);
    }

    private JButton createMapButton(MapData data) {
        JButton btn = new JButton(data.name.toUpperCase());
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(new Color(35, 35, 40));
        btn.setForeground(data.isRandom ? new Color(255, 100, 100) : Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2));
                btn.setBackground(new Color(55, 55, 65));
                selectedMap = data;
                updateSelectedLabel();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(35, 35, 40));
                if (selectedMap != data || lastHighlighted != btn) {
                    btn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
                }
            }
        });

        btn.addActionListener(e -> {
            selectedMap = data;
            updateSelectedLabel();
            highlightSelectedButton(btn);
        });

        return btn;
    }

    private JButton lastHighlighted;

    private void highlightSelectedButton(JButton selected) {
        if (lastHighlighted != null) {
            lastHighlighted.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        }
        lastHighlighted = selected;
        if (selected != null) {
            selected.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2));
        }
    }

    private void updateSelectedLabel() {
        if (selectedMap == null) return;
        String text = selectedMap.isRandom
                ? "RANDOM — Stage will be chosen when battle begins."
                : "<html><center><b>" + selectedMap.name.toUpperCase() + "</b><br>" + selectedMap.description + "</center></html>";
        selectedLabel.setText(text);
    }

    private void confirmSelection() {
        MapData map = selectedMap;
        if (map == null) {
            JOptionPane.showMessageDialog(this, "Select a stage or choose Random.");
            return;
        }
        if (map.isRandom) {
            map = allMaps.get(new Random().nextInt(allMaps.size()));
        }
        String hero = mainFrame.getSelectedHeroName();
        String msg = hero != null
                ? "Mission approved. " + hero + " will deploy to " + map.name + "."
                : "Stage set: " + map.name + ". Ready for battle.";
        JOptionPane.showMessageDialog(this, msg, "Stage Locked In", JOptionPane.INFORMATION_MESSAGE);
        // Future: mainFrame.navigateTo("battle") or start actual game
    }

    private void initializeMapData() {
        allMaps.add(new MapData("Avengers Tower", "New York. Stark's HQ and Avengers base.", false));
        allMaps.add(new MapData("Wakanda", "The Golden City. Advanced tech meets tradition.", false));
        allMaps.add(new MapData("Sokovia", "Fallen city. Remnants of Ultron's assault.", false));
        allMaps.add(new MapData("New York", "Battle of New York. Chitauri invasion site.", false));
        allMaps.add(new MapData("Titan", "Thanos' ruined homeworld. Desolate battleground.", false));
        allMaps.add(new MapData("Asgard", "Realm Eternal. Halls of Odin.", false));

        // --- Secret / Hidden Maps ---
        secretMapsMap.put(616, new MapData("CIT-U Basketball Court", "CIT-U's main basketball court, a place of competition and camaraderie.", false));
        secretMapsMap.put(199999, new MapData("Jollibee", "A random Jollibee location. What? A Jollibee? This cannot be real!", false));
        secretMapsMap.put(42, new MapData("Nyan Cat", "Seriously, a Nyan Cat map? This is too much!", false));      /* 3 secret maps */
    }

    private static class MapData {
        String name;
        String description;
        boolean isRandom;

        MapData(String name, String description, boolean isRandom) {
            this.name = name;
            this.description = description;
            this.isRandom = isRandom;
        }
    }
}
